package com.example.numease.presentation.student.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.numease.presentation.theme.NumEaseTheme
import androidx.compose.foundation.lazy.rememberLazyListState // Nhớ import thư viện này
import androidx.compose.runtime.LaunchedEffect // Nhớ import thư viện này

// ==========================================
// 1. DATA CLASSES
// ==========================================
enum class NodeState { COMPLETED, CURRENT, LOCKED }

data class MapNodeUI(
    val levelId: Int,
    val zoneName: String? = null,
    val state: NodeState,
    val stars: Int = 0
)

// ==========================================
// 2. MÀN HÌNH CHÍNH
// ==========================================


// ==========================================
// 2. MÀN HÌNH CHÍNH
// ==========================================
@Composable
fun MapScreen(
    totalStars: Int,
    nodes: List<MapNodeUI>,
    onBack: () -> Unit,
    onLevelSelected: (levelId: Int) -> Unit
) {
    // MỚI: Khởi tạo biến lưu trạng thái cuộn của danh sách
    val listState = rememberLazyListState()

    // MỚI: Chạy hiệu ứng cuộn ngay khi danh sách nodes vừa load xong
    LaunchedEffect(nodes) {
        // Tìm vị trí của cửa đang chơi (Con Cáo)
        val currentIndex = nodes.indexOfFirst { it.state == NodeState.CURRENT }

        if (currentIndex != -1) {
            // Cuộn ngay lập tức đến cửa hiện tại
            // scrollOffset = -400 giúp đẩy con cáo lên giữa màn hình thay vì dính sát ở đáy
            listState.scrollToItem(
                index = currentIndex,
                scrollOffset = -400
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Tự động chuyển Nền Xanh Nhạt <-> Nền Đen
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {

        // A. Đường nối mờ phía sau bản đồ
        val pathColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawLine(
                color = pathColor,
                start = androidx.compose.ui.geometry.Offset(x = canvasWidth / 2, y = 0f),
                end = androidx.compose.ui.geometry.Offset(x = canvasWidth / 2, y = canvasHeight),
                strokeWidth = 20f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 40f), 0f)
            )
        }

        // B. Bản đồ cuộn ngược
        LazyColumn(
            state = listState, // MỚI: Gắn trạng thái cuộn vào LazyColumn
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            contentPadding = PaddingValues(top = 120.dp, bottom = 40.dp)
        ) {
            itemsIndexed(nodes) { index, node ->

                val alignment = when (index % 2) {
                    0 -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }

                val paddingStart = if (alignment == Alignment.CenterStart) 48.dp else 0.dp
                val paddingEnd = if (alignment == Alignment.CenterEnd) 48.dp else 0.dp

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = paddingStart, end = paddingEnd),
                        contentAlignment = alignment
                    ) {
                        MapNodeItem(
                            node = node,
                            onClick = {
                                if (node.state != NodeState.LOCKED) {
                                    onLevelSelected(node.levelId)
                                }
                            }
                        )
                    }

                    if (node.zoneName != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = node.zoneName,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // C. Header nổi trên cùng
        Box(modifier = Modifier.systemBarsPadding()) {
            MapHeader(totalStars = totalStars, onBack = onBack)
        }
    }
}

// ==========================================
// 3. COMPONENT: HEADER
// ==========================================
@Composable
fun MapHeader(totalStars: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Back
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .size(56.dp)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }

        // Khối hiển thị Sao
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer, // Nền khối sao
            shadowElevation = 4.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "$totalStars",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("⭐", fontSize = 24.sp)
            }
        }
    }
}

// ==========================================
// 4. COMPONENT: NÚT LEVEL BẢN ĐỒ
// ==========================================
@Composable
fun MapNodeItem(node: MapNodeUI, onClick: () -> Unit) {
    when (node.state) {
        NodeState.COMPLETED -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Xanh lá
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp), // Xóa padding mặc định của Button
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(6.dp, CircleShape)
                ) {
                    Text(
                        text = node.levelId.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,          // Đảm bảo text chỉ hiển thị trên 1 dòng
                        softWrap = false       // Tắt tính năng tự động ngắt dòng
                    )
                }
                Row(modifier = Modifier.offset(y = (-10).dp)) {
                    for (i in 1..3) {
                        val alpha = if (i <= node.stars) 1f else 0.3f
                        Text(
                            text = "⭐",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .alpha(alpha)
                        )
                    }
                }
            }
        }
        NodeState.CURRENT -> {
            Box(contentAlignment = Alignment.TopCenter) {
                Text("🦊", fontSize = 56.sp, modifier = Modifier
                    .offset(y = (-40).dp)
                    .zIndex(2f))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary, // Vàng / Cam
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp), // Thêm dòng này để xóa viền đệm mặc định
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(12.dp, CircleShape)
                        .zIndex(1f)
                ) {
                    Text(
                        text = node.levelId.toString(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,        // Ép hiển thị 1 dòng
                        softWrap = false     // Cấm ngắt dòng bậy bạ
                    )
                }
            }
        }
        NodeState.LOCKED -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape) // Xám dịu
                    .shadow(1.dp, CircleShape)
            ) {
                Text("🔒", fontSize = 32.sp)
            }
        }
    }
}

// ==========================================
// 5. PREVIEW
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun MapScreenPreview() {
    val dummyNodes = listOf(
        MapNodeUI(1, "🌲 Rừng Đếm Số", NodeState.COMPLETED, 3),
        MapNodeUI(2, null, NodeState.COMPLETED, 2),
        MapNodeUI(3, null, NodeState.COMPLETED, 3),
        MapNodeUI(4, "🥕 Trại Kéo Thả", NodeState.CURRENT, 0),
        MapNodeUI(5, null, NodeState.LOCKED, 0),
        MapNodeUI(6, null, NodeState.LOCKED, 0)
    )

    // Nhớ bọc Preview bằng Theme của bạn để test màu
    NumEaseTheme(darkTheme = false) {
        MapScreen(
            totalStars = 15,
            nodes = dummyNodes,
            onBack = {},
            onLevelSelected = {}
        )
    }
}

