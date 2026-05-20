package com.example.numease.presentation.parent.manage

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.StudySession

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ManageChildrenScreen(
    viewModel: ManageChildrenViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToStats: (String) -> Unit
) {
    val children by viewModel.children.collectAsState()
    val recentStats by viewModel.currentChildStats.collectAsState()
    val showFormDialog by viewModel.showFormDialog.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    // 💡 GIẢI QUYẾT MINOR: Bỏ thẻ "Thêm bé" khỏi Pager, số trang chỉ bằng đúng số lượng bé
    val pageCount = children.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Lắng nghe sự kiện lướt Pager. Khi currentPage thay đổi -> Tải lại thống kê
    LaunchedEffect(pagerState.currentPage, children) {
        if (children.isNotEmpty() && pagerState.currentPage < children.size) {
            viewModel.loadStatsForChild(children[pagerState.currentPage].id)
        } else {
            viewModel.loadStatsForChild(null)
        }
    }

    if (showFormDialog) {
        // 💡 GIẢI QUYẾT CRITICAL: Thêm cấu hình để Dialog tương thích với bàn phím (IME)
        Dialog(
            onDismissRequest = { viewModel.closeForm() },
            properties = DialogProperties(
                decorFitsSystemWindows = false, // Cho phép nội dung di chuyển khi bàn phím đẩy lên
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .imePadding() // Quan trọng: Đẩy nội dung lên tránh bàn phím
            ) {
                ChildFormContent(viewModel = viewModel)
            }
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        // 💡 GIẢI QUYẾT MINOR: Nút "Thêm hồ sơ" được chuyển thành FAB nổi bật
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openForm(null) },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm hồ sơ")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================================
            // 1. KHU VỰC THẺ LƯỚT NGANG (HORIZONTAL PAGER)
            // ==========================================
            if (children.isEmpty()) {
                // Xử lý khi chưa có hồ sơ nào
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chưa có hồ sơ. Hãy bấm nút + để thêm!",
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val child = children[page]
                    ChildProfileCard(
                        child = child,
                        onEditClicked = { viewModel.openForm(child) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 2. CHẤM TRÒN PHÂN TRANG (PAGINATION DOTS)
                // ==========================================
                if (pageCount > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pageCount) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateFloatAsState(if (isSelected) 24f else 8f, label = "dot_width")
                            val dotColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(width.dp)
                                    .background(dotColor, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================================
            // 3. KHU VỰC BIỂU ĐỒ THỐNG KÊ BÊN DƯỚI
            // ==========================================
            if (children.isNotEmpty() && pagerState.currentPage < children.size) {
                val currentChildId = children[pagerState.currentPage].id

                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(1f)
                        .clickable { currentChildId?.let { onNavigateToStats(it) } }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kết quả 10 bài gần nhất",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Xem chi tiết",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (recentStats.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Bé chưa có dữ liệu học tập.", color = colorScheme.onSurfaceVariant)
                            }
                        } else {
                            RecentStatsBarChart(sessions = recentStats)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- THẺ THÔNG TIN BÉ ---
@Composable
fun ChildProfileCard(child: ChildProfile, onEditClicked: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    val (avatar, containerColor, contentColor) = when (child.gender?.uppercase()) {
        "MALE" -> Triple("👦", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        "FEMALE" -> Triple("👧", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
        else -> Triple("🦊", colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
    }

    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onEditClicked,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(colorScheme.surface.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = contentColor)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(avatar, fontSize = 64.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
                Text(
                    text = "Giới tính: ${if(child.gender=="MALE") "Nam" else if(child.gender=="FEMALE") "Nữ" else "Khác"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// --- THẺ THÊM BÉ ---
@Composable
fun AddChildCard(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Thêm",
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Thêm hồ sơ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
fun RecentStatsBarChart(sessions: List<StudySession>) {
    val colorScheme = MaterialTheme.colorScheme

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelMedium.copy(
        color = colorScheme.onSurfaceVariant
    )

    val starData = sessions.map { session ->
        when {
            session.totalQuestions == 0 -> 0f
            session.correctAnswers == session.totalQuestions -> 3f
            session.correctAnswers >= session.totalQuestions * 0.7 -> 2f
            session.correctAnswers > 0 -> 1f
            else -> 0f
        }
    }

    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(sessions) {
        animationProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000)
        ) { value, _ -> animationProgress = value }
    }

    val gridLineColor = colorScheme.outlineVariant
    val color3Star = colorScheme.primary
    val color2Star = colorScheme.tertiary
    val color1Star = colorScheme.error
    val color0Star = colorScheme.surfaceVariant

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxStars = 3f
        val maxBars = 10

        // 1. DÀNH KHOẢNG TRỐNG (PADDING) CHO TRỤC X VÀ Y
        val yAxisPadding = 80.dp.toPx() // Khoảng trống bên trái cho Trục Y (0⭐, 1⭐,...)
        val xAxisPadding = 60.dp.toPx() // Khoảng trống bên dưới cho Trục X (Lần 1, 2,...)

        // Không gian thực tế để vẽ cột (đã trừ padding)
        val drawableWidth = size.width - yAxisPadding
        val drawableHeight = size.height - xAxisPadding

        val barWidth = (drawableWidth / maxBars) * 0.6f
        val spacing = drawableWidth / maxBars

        // 2. VẼ TRỤC Y (SỐ SAO) VÀ LƯỚI
        for (i in 0..3) {
            val y = drawableHeight - (i / maxStars) * drawableHeight

            // Vẽ lưới đứt nét ngang (bỏ qua đường ở mốc 0)
            if (i > 0) {
                drawLine(
                    color = gridLineColor,
                    start = Offset(yAxisPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // Vẽ chữ trục Y (VD: "1 ⭐")
            val yLabel = "$i ⭐"
            val textLayoutResult = textMeasurer.measure(text = yLabel, style = textStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = yAxisPadding - textLayoutResult.size.width - 24f, // Lùi về bên trái 24px để cách đường lưới
                    y = y - textLayoutResult.size.height / 2f // Căn giữa theo chiều dọc
                )
            )
        }

        // Vẽ đường gạch ngang đậm nét làm trục X (Mốc 0)
        drawLine(
            color = gridLineColor,
            start = Offset(yAxisPadding, drawableHeight),
            end = Offset(size.width, drawableHeight),
            strokeWidth = 4f
        )

        // 3. VẼ CỘT VÀ TRỤC X (SỐ THỨ TỰ LẦN CHƠI)
        starData.forEachIndexed { index, stars ->
            val barHeight = (stars / maxStars) * drawableHeight * animationProgress

            // Tọa độ X của cột phải cộng thêm yAxisPadding để dịch sang phải
            val x = yAxisPadding + (index * spacing) + (spacing - barWidth) / 2
            val y = drawableHeight - barHeight

            val barColor = when (stars) {
                3f -> color3Star
                2f -> color2Star
                1f -> color1Star
                else -> color0Star
            }

            // Vẽ Cột
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // Vẽ chữ trục X (Chỉ số lần chơi, VD: 1, 2, 3...)
            val xLabel = "${index + 1}"
            val textLayoutResult = textMeasurer.measure(text = xLabel, style = textStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x + barWidth / 2f - textLayoutResult.size.width / 2f, // Căn giữa ngay dưới cột
                    y = drawableHeight + 24f // Dịch xuống dưới trục ngang 24px
                )
            )
        }
    }
}

// --- FORM DIALOG THÊM/SỬA ---
@Composable
fun ChildFormContent(viewModel: ManageChildrenViewModel) {
    val colorScheme = MaterialTheme.colorScheme

    val isEditing = viewModel.editingChildId.collectAsState().value != null
    val name by viewModel.formName.collectAsState()
    val gender by viewModel.formGender.collectAsState()
    val age by viewModel.formAge.collectAsState()
    val isLoading by viewModel.isFormLoading.collectAsState()

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // 💡 GIẢI QUYẾT CRITICAL: Cho phép cuộn form
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditing) "Sửa thông tin" else "Thêm bé mới",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (gender == "MALE") "👦" else "👧",
            fontSize = 64.sp,
            modifier = Modifier
                .background(colorScheme.surfaceVariant, CircleShape)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 💡 GIẢI QUYẾT MAJOR: Tùy chỉnh màu sắc để TextField nổi bật (có màu nền + viền)
        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.formName.value = it },
            label = { Text("Tên của bé") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedBorderColor = colorScheme.outline,
                focusedBorderColor = colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GenderOptionCard(
                label = "Nam",
                isSelected = gender == "MALE",
                onClick = { viewModel.formGender.value = "MALE" },
                modifier = Modifier.weight(1f)
            )
            GenderOptionCard(
                label = "Nữ",
                isSelected = gender == "FEMALE",
                onClick = { viewModel.formGender.value = "FEMALE" },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 💡 GIẢI QUYẾT MAJOR: Tương tự với ô nhập Tuổi
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.formAge.value = it },
            label = { Text("Tuổi của bé") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedBorderColor = colorScheme.outline,
                focusedBorderColor = colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(
                    onClick = { viewModel.closeForm() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hủy", color = colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { viewModel.saveChild() },
                    enabled = name.isNotBlank() && age.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Lưu")
                }
            }
        }
    }
}
@Composable
fun GenderOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant
    val contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}