package com.example.numease.presentation.admin.question



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.CalculationContent
import com.example.numease.data.model.ComparingContent
import com.example.numease.data.model.CountingContent
import com.example.numease.data.model.DragDropContent
import com.example.numease.presentation.admin.content.getCategoryStyling
import com.example.numease.utils.getEmojiForObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageQuestionsScreen(
    categoryId: Int,
    categoryCode: String,
    level: Int,
    viewModel: ManageQuestionsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAddQuestion: () -> Unit
) {
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val (icon, color) = getCategoryStyling(categoryCode) // Dùng lại hàm ở phần trước

    LaunchedEffect(categoryId, level) {
        viewModel.loadQuestions(categoryId, level)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Level $level", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddQuestion, containerColor = color) {
                Icon(Icons.Default.Add, contentDescription = "Thêm câu hỏi", tint = Color.White)
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = color) }
        } else if (questions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chưa có câu hỏi nào.", color = Color.Gray) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(questions) { exercise ->
                    // Dùng when thay cho if/else
                    when (val content = exercise.content) {
                        is CountingContent -> {
                            CountingQuestionCard(content = content, color = color)
                        }
                        is ComparingContent -> {
                            ComparingQuestionCard(content = content, color = color)
                        }
                        is CalculationContent -> {
                            // Tạm thời hiển thị một dòng chữ để biết là nó có tồn tại
                            CalculationQuestionCard(content = content, color = color)
                        }
                        is DragDropContent -> {
                            DragDropQuestionCard(content = content, color = color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountingQuestionCard(content: CountingContent, color: Color) {
    Surface(
        color = Color.White, shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Đề bài: ${content.instruction.text}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Lấy Emoji dựa vào tên tiếng anh (apple, cat...)
            val emoji = getEmojiForObject(content.objectType)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Vật thể: $emoji (${content.objectType})", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Số lượng: ${content.count}", fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Các lựa chọn: ${content.options.joinToString(", ")}", fontSize = 14.sp)
            Text(text = "Đáp án đúng: ${content.correctAnswer}", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
    }
}

// THÊM COMPONENT NÀY XUỐNG DƯỚI CÙNG FILE
@Composable
fun ComparingQuestionCard(content: ComparingContent, color: Color) {
    Surface(
        color = Color.White, shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Đề bài: ${content.instruction.text}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Hiển thị dạng phép tính:  3  [ ? ]  5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "${content.leftValue}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(24.dp))

                // Ô chứa đáp án
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = content.correctAnswer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = color,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))
                Text(text = "${content.rightValue}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DragDropQuestionCard(content: DragDropContent, color: Color) {
    Surface(
        color = Color.White, shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Đề bài: ${content.instruction.text}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Các cặp tương ứng:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

            // Lặp qua Map correctMapping để in ra sự nối kết
            content.correctMapping.forEach { (dragId, zoneId) ->
                val dragLabel = content.draggables.find { it.id == dragId }?.label ?: "?"
                val zoneLabel = content.dropZones.find { it.id == zoneId }?.label ?: "?"

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cục kéo (Draggable)
                    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(dragLabel, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }

                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.padding(horizontal = 12.dp))

                    // Giỏ thả (DropZone)
                    Surface(color = Color(0xFFF5F7FA), shape = RoundedCornerShape(8.dp)) {
                        Text(zoneLabel, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationQuestionCard(content: CalculationContent, color: Color) {
    Surface(
        color = Color.White, shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Đề bài: ${content.instruction.text}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị trực quan phép tính: 5 + 3 = ?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${content.leftValue}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = content.operator, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "${content.rightValue}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "=", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))

                // Ô Đáp án đúng nổi bật
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "${content.correctAnswer}",
                        fontSize = 24.sp, fontWeight = FontWeight.Black, color = color,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Lựa chọn: ${content.options.joinToString("  |  ")}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}