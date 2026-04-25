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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.StudySession

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ManageChildrenScreen(
    viewModel: ManageChildrenViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToStats: (String) -> Unit
//    onNavigateToAddChild: () -> Unit,
//    onNavigateToEditChild: (childId: String) -> Unit
) {
    val children by viewModel.children.collectAsState()
    val recentStats by viewModel.currentChildStats.collectAsState()
    val showFormDialog by viewModel.showFormDialog.collectAsState()



    // PagerState: Số lượng trang = Số bé + 1 (Trang cuối cùng để Thêm Bé)
    val pageCount = children.size + 1
    val pagerState = rememberPagerState(pageCount = { pageCount })



    // Lắng nghe sự kiện lướt Pager. Khi currentPage thay đổi -> Tải lại thống kê
    LaunchedEffect(pagerState.currentPage, children) {
        if (pagerState.currentPage < children.size) {
            viewModel.loadStatsForChild(children[pagerState.currentPage].id)
        } else {
            viewModel.loadStatsForChild(null) // Đang ở thẻ "Thêm bé" thì xóa biểu đồ
        }
    }

    if (showFormDialog) {
        Dialog(onDismissRequest = { viewModel.closeForm() }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            ) {
                ChildFormContent(viewModel = viewModel)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA), // Nền xám rất nhạt, sang trọng
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 48.dp), // Để lòi mép thẻ 2 bên ra một chút
                pageSpacing = 16.dp
            ) { page ->
                if (page < children.size) {
                    val child = children[page]
                    ChildProfileCard(
                        child = child,
                        onEditClicked = { viewModel.openForm(child) }
                    )
                } else {
                    AddChildCard(onClick = { viewModel.openForm(null) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2. CHẤM TRÒN PHÂN TRANG (PAGINATION DOTS)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateFloatAsState(if (isSelected) 24f else 8f)
                    val color = if (isSelected) Color(0xFF1976D2) else Color.LightGray

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width.dp)
                            .background(color, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================================
            // 3. KHU VỰC BIỂU ĐỒ THỐNG KÊ BÊN DƯỚI
            // ==========================================
            if (pagerState.currentPage < children.size) {
                val currentChildId = children[pagerState.currentPage].id

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        // THÊM: Khi nhấn vào vùng biểu đồ sẽ chuyển màn hình
                        .clickable {
                            currentChildId?.let { onNavigateToStats(it) }
                        }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kết quả 10 bài gần nhất",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F)
                            )
                            // Thêm một icon nhỏ để gợi ý người dùng có thể nhấn vào
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (recentStats.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Bé chưa có dữ liệu học tập.", color = Color.Gray)
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
    // Phân tích Avatar dựa trên giới tính
    val (avatar, bgColor) = when (child.gender?.uppercase()) {
        "MALE" -> Pair("👦", Color(0xFFE3F2FD)) // Xanh dương
        "FEMALE" -> Pair("👧", Color(0xFFFCE4EC)) // Hồng
        else -> Pair("🐻", Color(0xFFFFF3E0)) // Cam (Trung tính)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(6.dp, RoundedCornerShape(32.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Nút sửa thông tin ở góc trên phải
            IconButton(
                onClick = onEditClicked,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF1565C0))
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
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF37474F)
                )
                // Hiển thị Tuổi hoặc Giới tính nếu có
                Text(
                    text = "Giới tính: ${if(child.gender=="MALE") "Nam" else if(child.gender=="FEMALE") "Nữ" else "Khác"}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

// --- THẺ THÊM BÉ ---
@Composable
fun AddChildCard(onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() }
    ) {
        // Viền đứt nét (Tùy chọn: Dùng Canvas vẽ nét đứt nếu muốn, ở đây dùng Box cho nhẹ)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Thêm hồ sơ", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

// --- BIỂU ĐỒ CỘT (BAR CHART) TÙY CHỈNH ---
@Composable
fun RecentStatsBarChart(sessions: List<StudySession>) {
    // Tính sao cho từng bài
    val starData = sessions.map { session ->
        when {
            session.totalQuestions == 0 -> 0f
            session.correctAnswers == session.totalQuestions -> 3f
            session.correctAnswers >= session.totalQuestions * 0.7 -> 2f
            session.correctAnswers > 0 -> 1f
            else -> 0f
        }
    }

    // Hiệu ứng mọc cột từ dưới lên
    var animationProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(sessions) {
        animationProgress = 0f // Reset khi đổi thẻ
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000)
        ) { value, _ -> animationProgress = value }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxStars = 3f
        val maxBars = 10
        val width = size.width
        val height = size.height

        // Tính toán khoảng cách
        val barWidth = (width / maxBars) * 0.6f // Chiều rộng cột
        val spacing = width / maxBars // Khoảng cách giữa tâm các cột

        // Vẽ 3 đường vạch ngang đứt nét (Mốc 1, 2, 3 sao)
        for (i in 1..3) {
            val y = height - (i / maxStars) * height
            drawLine(
                color = Color(0xFFEEEEEE),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        }

        // Vẽ các cột (Tối đa 10 cột)
        starData.forEachIndexed { index, stars ->
            val barHeight = (stars / maxStars) * height * animationProgress
            val x = (index * spacing) + (spacing - barWidth) / 2
            val y = height - barHeight

            // Chọn màu: 3 sao -> Xanh lá, 2 sao -> Vàng, 1 sao -> Cam, 0 sao -> Xám
            val barColor = when (stars) {
                3f -> Color(0xFF4CAF50)
                2f -> Color(0xFFFFCA28)
                1f -> Color(0xFFFF7043)
                else -> Color(0xFFE0E0E0)
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(16f, 16f) // Bo tròn đỉnh cột
            )
        }
    }
}

@Composable
fun ChildFormContent(viewModel: ManageChildrenViewModel) {
    val isEditing = viewModel.editingChildId.collectAsState().value != null
    val name by viewModel.formName.collectAsState()
    val gender by viewModel.formGender.collectAsState()
    val age by viewModel.formAge.collectAsState()
    val isLoading by viewModel.isFormLoading.collectAsState()

    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditing) "Sửa thông tin" else "Thêm bé mới",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar vui nhộn dựa trên giới tính
        Text(
            text = if (gender == "MALE") "👦" else "👧",
            fontSize = 64.sp,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).padding(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.formName.value = it },
            label = { Text("Tên của bé") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Nút chọn Giới tính
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

        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.formAge.value = it },
            label = { Text("Tuổi của bé") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Nút Lưu và Hủy
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(
                    onClick = { viewModel.closeForm() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hủy", color = Color.Gray)
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
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

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