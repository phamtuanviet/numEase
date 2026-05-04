@file:OptIn(ExperimentalLayoutApi::class) // Cần thiết cho FlowRow

package com.example.numease.presentation.student.exercise

import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.draw.shadow
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
    viewModel: ExerciseViewModel = hiltViewModel(),
    onPauseAndExit: () -> Unit,
    onSessionComplete: (earnedStars: Int) -> Unit
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
            LaunchedEffect(state.earnedStars) { onSessionComplete(state.earnedStars) }
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
                                                if (feedbackState == null) {
                                                    pendingAnswer = answer
                                                    val isCorrect = (answer == content.correctAnswer)
                                                    feedbackState = isCorrect

                                                    if (isCorrect) playAudio("Đúng rồi, giỏi quá!")
                                                    else playAudio("Chưa chính xác.")
                                                }
                                            }
                                        )
                                    }
                                    is DragDropContent -> {
                                        DragDropUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = {
                                                if (feedbackState == null) {
                                                    pendingAnswer = 1
                                                    feedbackState = true
                                                    playAudio("Xuất sắc luôn, bé giỏi quá!")
                                                }
                                            }
                                        )
                                    }
                                    is ComparingContent -> {
                                        ComparingUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = { answer ->
                                                if (feedbackState == null) {
                                                    pendingAnswer = answer
                                                    val isCorrect = (answer == content.correctAnswer)
                                                    feedbackState = isCorrect

                                                    if (isCorrect) playAudio("Đúng rồi, bé thật thông minh!")
                                                    else playAudio("Chưa chính xác, thử lại nhé.")
                                                }
                                            }
                                        )
                                    }
                                    is CalculationContent -> {
                                        CalculationUI(
                                            content = content,
                                            onPlayAudio = playAudio,
                                            onAnswerSelected = { answer ->
                                                if (feedbackState == null) {
                                                    pendingAnswer = answer
                                                    val isCorrect = (answer == content.correctAnswer)
                                                    feedbackState = isCorrect

                                                    if (isCorrect) playAudio("Tuyệt vời, bé tính giỏi quá!")
                                                    else playAudio("Chưa chính xác, bé đếm lại các chấm tròn nhé.")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (feedbackState != null) {
                        FeedbackOverlay(
                            isCorrect = feedbackState!!,
                            onAnimationEnd = {
                                viewModel.submitAnswer(pendingAnswer!!)
                                feedbackState = null
                                pendingAnswer = null
                            }
                        )
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Khung đề bài + Âm thanh (Dùng SecondaryContainer)
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

        // Nút đáp án (Dùng Primary color)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.options.forEach { option ->
                Button(
                    onClick = { onAnswerSelected(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    // Giảm khoảng đệm mặc định để có nhiều không gian hiển thị chữ hơn
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(90.dp) // Cố định chiều cao
                        .widthIn(min = 90.dp) // Rộng tối thiểu 90dp, sẽ tự động giãn ngang nếu chữ dài
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = option.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1, // Ép buộc chỉ hiển thị trên 1 dòng
                        softWrap = false // Tắt tính năng tự động rớt dòng
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
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

@Composable
fun FeedbackOverlay(
    isCorrect: Boolean,
    onAnimationEnd: () -> Unit
) {
    // Sử dụng màu chuẩn của MD3: primaryContainer cho Đúng, errorContainer cho Sai
    // Giảm alpha xuống 0.9f để không bị gắt quá
    val colorScheme = MaterialTheme.colorScheme
    val bgColor = if (isCorrect) {
        colorScheme.primaryContainer.copy(alpha = 0.9f)
    } else {
        colorScheme.errorContainer.copy(alpha = 0.9f)
    }

    val icon = if (isCorrect) "✅" else "❌"

    LaunchedEffect(Unit) {
        delay(1500)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 150.sp,
            modifier = Modifier.shadow(16.dp, CircleShape)
        )
    }
}