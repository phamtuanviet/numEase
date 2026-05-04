package com.example.numease.presentation.admin.question

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.*
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
    val colorScheme = MaterialTheme.colorScheme
    val (_, categoryThemeColor) = getCategoryStyling(categoryCode)

    LaunchedEffect(categoryId, level) {
        viewModel.loadQuestions(categoryId, level)
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Màn chơi $level", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddQuestion,
                containerColor = categoryThemeColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm câu hỏi")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = categoryThemeColor)
            }
        } else if (questions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có câu hỏi nào.", color = colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(questions) { exercise ->
                    when (val content = exercise.content) {
                        is CountingContent -> CountingQuestionCard(content, categoryThemeColor)
                        is ComparingContent -> ComparingQuestionCard(content, categoryThemeColor)
                        is CalculationContent -> CalculationQuestionCard(content, categoryThemeColor)
                        is DragDropContent -> DragDropQuestionCard(content, categoryThemeColor)
                    }
                }
            }
        }
    }
}

// --- THÀNH PHẦN CARD DÙNG CHUNG ---
@Composable
fun QuestionCardContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colorScheme.outlineVariant)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Đề bài: $title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CountingQuestionCard(content: CountingContent, color: Color) {
    val colorScheme = MaterialTheme.colorScheme
    QuestionCardContainer(title = content.instruction.text) {
        val emoji = getEmojiForObject(content.objectType)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Vật thể: $emoji (${content.objectType})", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Số lượng: ${content.count}", style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Lựa chọn: ${content.options.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        Text(text = "Đáp án đúng: ${content.correctAnswer}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ComparingQuestionCard(content: ComparingContent, color: Color) {
    QuestionCardContainer(title = content.instruction.text) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${content.leftValue}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(16.dp))
            Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                Text(text = content.correctAnswer, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "${content.rightValue}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CalculationQuestionCard(content: CalculationContent, color: Color) {
    val colorScheme = MaterialTheme.colorScheme
    QuestionCardContainer(title = content.instruction.text) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${content.leftValue} ${content.operator} ${content.rightValue} =", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                Text(text = "${content.correctAnswer}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Lựa chọn: ${content.options.joinToString(" | ")}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DragDropQuestionCard(content: DragDropContent, color: Color) {
    val colorScheme = MaterialTheme.colorScheme
    QuestionCardContainer(title = content.instruction.text) {
        content.correctMapping.forEach { (dragId, zoneId) ->
            val dragLabel = content.draggables.find { it.id == dragId }?.label ?: "?"
            val zoneLabel = content.dropZones.find { it.id == zoneId }?.label ?: "?"
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(dragLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 8.dp).size(16.dp))
                Surface(color = colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Text(zoneLabel, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}