@file:OptIn(ExperimentalLayoutApi::class) // Cần thiết cho FlowRow

package com.example.numease.presentation.student.exercise

import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.LocalSoundEnabled
import com.example.numease.data.model.CalculationContent
import com.example.numease.data.model.ComparingContent
import com.example.numease.data.model.CountingContent
import com.example.numease.data.model.DragDropContent
import com.example.numease.presentation.component.CalculationUI
import com.example.numease.presentation.component.ComparingUI
import com.example.numease.presentation.component.DragDropUI
import com.example.numease.utils.getEmojiForObject
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ExerciseScreen(
    categoryId: Int,
    level: Int,
    levelId: Int,
    viewModel: ExerciseViewModel = hiltViewModel(),
    onPauseAndExit: () -> Unit,
    onSessionComplete: (earnedStars: Int, currentLevelId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    // --- CÀI ĐẶT TEXT-TO-SPEECH ---
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("vi", "VN")
        }
        tts = textToSpeech
        viewModel.startSession(categoryId, level)
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val isSoundEnabled = LocalSoundEnabled.current

    val playAudio = { message: String ->
        if (isSoundEnabled) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    when (val state = uiState) {
        is ExerciseUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is ExerciseUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message) }
        is ExerciseUiState.Finished -> {
            LaunchedEffect(state.earnedStars) { onSessionComplete(state.earnedStars, levelId) }
        }
        is ExerciseUiState.Playing -> {
            val currentExercise = state.exercises[state.currentIndex]

            var feedbackState by remember { mutableStateOf<Boolean?>(null) }
            var pendingAnswer by remember { mutableStateOf<Any?>(null) }

            LaunchedEffect(state.currentIndex) {
                val text = when (val content = currentExercise.content) {
                    is CountingContent -> content.instruction.text
                    is DragDropContent -> content.instruction.text
                    is ComparingContent -> content.instruction.text
                    is CalculationContent -> content.instruction.text
                }
                delay(300)
                playAudio(text)
            }

            // Dùng Surface làm nền để tự động đổi màu theo Theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ExerciseTopBar(state.currentIndex, state.exercises.size, onPauseAndExit)

                        Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)) {
                            key(state.currentIndex) {
                                when (val content = currentExercise.content) {
                                    is CountingContent -> {
                                        CountingUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = { answer ->
                                                // UI con đã chạy xong hiệu ứng và báo về, chỉ việc nộp đáp án!
                                                viewModel.submitAnswer(answer)
                                            }
                                        )
                                    }
                                    is DragDropContent -> {
                                        DragDropUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = { answer ->
                                                // DragDropUI trả về 1 khi bé ghép xong tất cả
                                                viewModel.submitAnswer(answer)
                                            }
                                        )
                                    }
                                    is ComparingContent -> {
                                        ComparingUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = { answer ->
                                                viewModel.submitAnswer(answer)
                                            }
                                        )
                                    }
                                    is CalculationContent -> {
                                        CalculationUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = { answer ->
                                                viewModel.submitAnswer(answer)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountingUI(
    content: CountingContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // MỚI: State lưu đáp án bé ĐÃ bấm sai để làm mờ nút đó đi
    var wrongAnswers by remember { mutableStateOf(setOf<Int>()) }

    // MỚI: State lưu trạng thái khi bé BẤM ĐÚNG (để chạy hiệu ứng chờ qua câu)
    var isCorrectlyAnswered by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Khung đề bài + Âm thanh
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.VolumeUp, "Nghe lại", tint = colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Vật thể
        val emoji = getEmojiForObject(content.objectType)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            repeat(content.count) {
                Text(
                    text = emoji,
                    fontSize = 72.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nút đáp án
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.options.forEach { option ->

                // Logic xác định trạng thái của nút
                val isCorrectAnswer = option == content.correctAnswer
                val isWronglyPressed = wrongAnswers.contains(option)
                val showSuccessEffect = isCorrectlyAnswered && isCorrectAnswer

                // Hiệu ứng màu sắc
                val buttonColor by animateColorAsState(
                    targetValue = when {
                        showSuccessEffect -> Color(0xFF4CAF50) // Xanh lá cây (Đúng)
                        isWronglyPressed -> colorScheme.surfaceVariant // Xám (Sai)
                        else -> colorScheme.primary // Màu mặc định
                    },
                    animationSpec = tween(300)
                )

                val contentColor = if (showSuccessEffect) Color.White else if (isWronglyPressed) colorScheme.outline else colorScheme.onPrimary

                // Hiệu ứng phóng to khi bấm đúng
                val scale by animateFloatAsState(
                    targetValue = if (showSuccessEffect) 1.15f else 1f,
                    animationSpec = tween(500)
                )

                Button(
                    onClick = {
                        if (isCorrectlyAnswered || isWronglyPressed) return@Button // Block spam click

                        if (isCorrectAnswer) {
                            // 1. TRƯỜNG HỢP ĐÚNG
                            isCorrectlyAnswered = true
                            onPlayAudio("Đúng rồi, giỏi quá!")
                        } else {
                            // 2. TRƯỜNG HỢP SAI
                            wrongAnswers = wrongAnswers + option
                            onPlayAudio("Chưa chính xác, thử lại nhé.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = contentColor,
                        disabledContainerColor = buttonColor,
                        disabledContentColor = contentColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    // Khóa nút nếu bé đã trả lời xong hoặc nút này đã bị bấm sai
                    enabled = !isCorrectlyAnswered && !isWronglyPressed,
                    modifier = Modifier
                        .height(90.dp)
                        .widthIn(min = 90.dp)
                        .scale(scale) // Áp dụng hiệu ứng phóng to
                        .shadow(if (isWronglyPressed) 0.dp else 6.dp, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = option.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                        // Hiện dấu tick nhỏ kế bên số nếu đúng
                        if (showSuccessEffect) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✅", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }

    // --- XỬ LÝ CHUYỂN CÂU KHI BÉ LÀM ĐÚNG ---
    LaunchedEffect(isCorrectlyAnswered) {
        if (isCorrectlyAnswered) {
            delay(1500) // Đợi 1.5s để bé nhìn thấy dấu Tick xanh và nghe Audio khen

            // Báo lên cho ExerciseScreen biết là Đã Xong để chuyển qua câu mới
            onAnswerSelected(content.correctAnswer)

            // Reset trạng thái để chuẩn bị cho câu tiếp theo (nếu view được tái sử dụng)
            isCorrectlyAnswered = false
            wrongAnswers = emptySet()
        }
    }
}
@Composable
fun ExerciseTopBar(currentIndex: Int, totalQuestions: Int, onClose: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Thoát/Tạm dừng (Dùng SurfaceVariant)
        IconButton(
            onClick = onClose,
            modifier = Modifier.background(colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = colorScheme.onSurfaceVariant)
        }

        // Hạt tiến độ
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            for (i in 0 until totalQuestions) {
                val dotColor = when {
                    i < currentIndex -> colorScheme.primary // Đã làm (Xanh lá - Primary)
                    i == currentIndex -> colorScheme.tertiary // Đang làm (Vàng - Tertiary)
                    else -> colorScheme.outlineVariant // Chưa làm (Xám nhạt)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(12.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }

        Box(modifier = Modifier.size(48.dp))
    }
}

