package com.example.numease.presentation.student.exercise


import android.speech.tts.TextToSpeech
import kotlinx.coroutines.delay
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val isSoundEnabled = LocalSoundEnabled.current // Gọi thẳng biến toàn cục

    val playAudio = { message: String ->
        if (isSoundEnabled) { // Chỉ phát nếu bé bật âm thanh
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

            // 1. STATE QUẢN LÝ HIỆU ỨNG CHUNG CHO TẤT CẢ DẠNG BÀI
            var feedbackState by remember { mutableStateOf<Boolean?>(null) }
            var pendingAnswer by remember { mutableStateOf<Any?>(null) }

            // 2. ĐỌC ĐỀ BÀI KHI CHUYỂN CÂU MỚI
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

            Box(modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {

                // --- PHẦN GIAO DIỆN CHÍNH ---
                Column(modifier = Modifier.fillMaxSize()) {
                    ExerciseTopBar(state.currentIndex, state.exercises.size, onPauseAndExit)

                    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)) {

                        // QUAN TRỌNG: Dùng key để ép Compose xóa giao diện cũ, vẽ lại giao diện mới khi sang câu
                        key(state.currentIndex) {
                            when (val content = currentExercise.content) {
                                is CountingContent -> {
                                    CountingUI(
                                        content = content,
                                        onPlayAudio = playAudio,
                                        onAnswerSelected = { answer ->
                                            // KHÓA BẤM NHIỀU LẦN: Nếu đang hiện hiệu ứng thì không nhận nút bấm nữa
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
                                    // ĐÃ SỬA: Thay thế Text("Drag Drop UI") bằng component thực tế
                                    DragDropUI(
                                        content = content,
                                        onPlayAudio = playAudio,
                                        onAnswerSelected = {
                                            // Vì DragDropUI chỉ gọi callback này khi bé đã ghép ĐÚNG HẾT
                                            // nên chúng ta mặc định kết quả là True.
                                            if (feedbackState == null) {
                                                pendingAnswer = 1 // Gửi giá trị 1 tượng trưng cho "Đúng"
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
                                            // Đảm bảo không cho bấm 2 lần khi đang hiện Overlay
                                            if (feedbackState == null) {
                                                pendingAnswer = answer // Lưu dấu bé đã chọn (">", "<", "=")
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

                // --- LỚP PHỦ OVERLAY ĐÚNG/SAI (Bao trùm toàn màn hình) ---
                if (feedbackState != null) {
                    FeedbackOverlay(
                        isCorrect = feedbackState!!,
                        onAnimationEnd = {
                            // Hết 1.5 giây -> Nộp bài cho ViewModel -> ViewModel tăng currentIndex
                            viewModel.submitAnswer(pendingAnswer!!)
                            // Tắt overlay
                            feedbackState = null
                            pendingAnswer = null
                        }
                    )
                }
            }
        }
    }
}
// ... (Tiếp tục trong file ExerciseScreen.kt)

@Composable
fun CountingUI(
    content: CountingContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit // Truyền đáp án ra ngoài
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Khung đề bài + Âm thanh
        Surface(
            color = Color(0xFFE3F2FD),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.VolumeUp, "Nghe lại", tint = Color(0xFF1565C0))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
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
                Button(
                    onClick = { onAnswerSelected(option) }, // Bấm phát gọi thẳng ra ngoài luôn
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(90.dp).shadow(6.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(option.toString(), fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// --- THANH TIẾN ĐỘ BẰNG HẠT (METAPHOR TRỰC QUAN) ---
@Composable
fun ExerciseTopBar(currentIndex: Int, totalQuestions: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Thoát/Tạm dừng
        IconButton(
            onClick = onClose,
            modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.Gray)
        }

        // Hạt tiến độ
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            for (i in 0 until totalQuestions) {
                val dotColor = when {
                    i < currentIndex -> Color(0xFF4CAF50) // Đã làm (Xanh lá)
                    i == currentIndex -> Color(0xFF2196F3) // Đang làm (Xanh dương)
                    else -> Color(0xFFE0E0E0) // Chưa làm (Xám)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(12.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }

        // Box rỗng để cân bằng layout
        Box(modifier = Modifier.size(48.dp))
    }
}

// --- HIỆU ỨNG PHẢN HỒI (ĐÚNG / SAI) ---
@Composable
fun FeedbackOverlay(isCorrect: Boolean) {
    val bgColor = if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color(0xFFF44336).copy(alpha = 0.8f)
    val icon = if (isCorrect) "✅" else "❌"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 150.sp,
            modifier = Modifier.shadow(24.dp, CircleShape) // Tạo bóng cho đẹp
        )
    }
}




@Composable
fun FeedbackOverlay(
    isCorrect: Boolean,
    onAnimationEnd: () -> Unit
) {
    val bgColor = if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color(0xFFF44336).copy(alpha = 0.8f)
    val icon = if (isCorrect) "✅" else "❌"

    // Tự động đếm ngược 1.5 giây rồi báo ra ngoài
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
            modifier = Modifier.shadow(24.dp, CircleShape)
        )
    }
}