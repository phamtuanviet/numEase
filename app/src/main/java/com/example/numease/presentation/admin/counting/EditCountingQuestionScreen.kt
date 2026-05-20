import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.admin.counting.EditCountingViewModel
import com.example.numease.utils.getEmojiForObject

val AVAILABLE_OBJECT_TYPES = listOf(
    "apple", "cat", "dog", "star", "candy", "flower", "ball",
    "car", "banana", "bird", "strawberry", "rabbit", "ice_cream",
    "orange", "bear", "pencil", "book", "hat", "leaf", "butterfly"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCountingQuestionScreen(
    exerciseId: String,
    categoryId: Int,
    level: Int,
    viewModel: EditCountingViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    // MỚI: Quản lý thông báo lỗi
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(exerciseId) {
        viewModel.loadQuestionData(exerciseId)
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Gắn SnackbarHost
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sửa Đếm Số", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteQuestion(exerciseId, onSavedSuccess) }) {
                        Icon(Icons.Default.DeleteSweep, "Xóa câu hỏi", tint = colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Khối 1: Đề bài & Icon
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Thông tin cơ bản", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.instructionText.value,
                            onValueChange = { viewModel.instructionText.value = it },
                            label = { Text("Câu lệnh") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = "${getEmojiForObject(viewModel.objectType.value)}  ${viewModel.objectType.value}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Loại vật thể") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                AVAILABLE_OBJECT_TYPES.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text("${getEmojiForObject(type)}  $type") },
                                        onClick = {
                                            viewModel.objectType.value = type
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Khối 2: Đáp án & Số lượng
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Dữ liệu đáp án", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = viewModel.count.value,
                                onValueChange = { viewModel.count.value = it },
                                label = { Text("Số vật thể") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.correctAnswer.value,
                                onValueChange = { viewModel.correctAnswer.value = it },
                                label = { Text("Đáp án đúng") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Khối 3: 3 Ô LỰA CHỌN RIÊNG BIỆT
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colorScheme.outlineVariant))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Các lựa chọn hiển thị", style = MaterialTheme.typography.titleSmall, color = colorScheme.primary)
                        Text("Nhập 3 con số sẽ hiện ra cho bé chọn", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.option1.value,
                                onValueChange = { viewModel.option1.value = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Số 1") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.option2.value,
                                onValueChange = { viewModel.option2.value = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Số 2") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = viewModel.option3.value,
                                onValueChange = { viewModel.option3.value = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Số 3") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Nút Cập Nhật
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.updateQuestion(exerciseId, categoryId, level, onSavedSuccess)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    // MỚI: Khóa nút nếu form chưa đầy đủ hoặc đang lưu
                    enabled = viewModel.isFormValid() && !viewModel.isSaving.value
                ) {
                    if (viewModel.isSaving.value) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Cập nhật câu hỏi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}