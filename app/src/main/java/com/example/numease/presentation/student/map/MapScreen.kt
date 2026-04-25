package com.example.numease.presentation.student.map


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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

// ==========================================
// 1. DATA CLASSES (Dành riêng cho UI)
// ==========================================
enum class NodeState { COMPLETED, CURRENT, LOCKED }

data class MapNodeUI(
    val levelId: Int,             // Số in trên nút
    val zoneName: String? = null, // Tên biển báo (nếu có)
    val state: NodeState,         // Trạng thái (Xanh, Vàng, Xám)
    val stars: Int = 0            // Số sao đạt được
)

// ==========================================
// 2. MÀN HÌNH CHÍNH (Thuần UI, không chứa ViewModel)
// ==========================================
@Composable
fun MapScreen(
    totalStars: Int,
    nodes: List<MapNodeUI>,
    onBack: () -> Unit,
    onLevelSelected: (levelId: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF81D4FA)) // Màu xanh biển nền
    ) {
        // A. Bản đồ cuộn ngược (Từ dưới lên trên)
        // ... (Các code bên ngoài giữ nguyên)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true, // Cuộn từ dưới lên
            contentPadding = PaddingValues(top = 120.dp, bottom = 40.dp)
        ) {
            itemsIndexed(nodes) { index, node ->

                val alignment = when (index % 4) {
                    0 -> Alignment.CenterStart
                    1, 3 -> Alignment.Center
                    2 -> Alignment.CenterEnd
                    else -> Alignment.Center
                }

                val paddingStart = if (alignment == Alignment.CenterStart) 48.dp else 0.dp
                val paddingEnd = if (alignment == Alignment.CenterEnd) 48.dp else 0.dp

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. KHOẢNG CÁCH NỐI TIẾP VỚI CỬA TRÊN NÓ
                    // Đặt ở đây để Cửa 1 và Cửa 2 có khoảng cách đều nhau
                    Spacer(modifier = Modifier.height(48.dp))

                    // 2. NÚT BẤM LEVEL (Vẽ trước -> Nằm ở trên trong Column này)
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

                    // 3. BIỂN BÁO KHU VỰC (Vẽ sau -> Nằm ở dưới Nút trong Column này)
                    // Nhờ reverseLayout, nó sẽ trở thành "Cổng chào" nằm dưới cùng của Zone
                    if (node.zoneName != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = node.zoneName,
                                color = Color(0xFF0277BD),
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Spacer chừa khoảng trống ở dưới đáy bản đồ ban đầu (nhờ reverse nên nó là item đầu tiên)
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // B. Header nổi trên cùng
        MapHeader(totalStars = totalStars, onBack = onBack)
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
                .background(Color.White, CircleShape)
                .size(48.dp)
        ) {
            Icon(Icons.Default.ArrowBack, "Quay lại", tint = Color.Gray)
        }

        // Bảng hiện sao
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFFF8E1),
            shadowElevation = 4.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("$totalStars", fontWeight = FontWeight.ExtraBold, color = Color(0xFFF57F17), fontSize = 20.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("⭐", fontSize = 20.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(6.dp, CircleShape)
                ) {
                    Text(node.levelId.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                // Hiển thị 3 ngôi sao ở dưới nút
                Row(modifier = Modifier.offset(y = (-10).dp)) {
                    for (i in 1..3) {
                        val alpha = if (i <= node.stars) 1f else 0.3f
                        Text("⭐", fontSize = 16.sp, modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .alpha(alpha))
                    }
                }
            }
        }
        NodeState.CURRENT -> {
            Box(contentAlignment = Alignment.TopCenter) {
                // Mascot Gấu
                Text("🐻", fontSize = 48.sp, modifier = Modifier
                    .offset(y = (-36).dp)
                    .zIndex(2f))
                // Nút Vàng
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCA28)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(8.dp, CircleShape)
                        .zIndex(1f)
                ) {
                    Text(node.levelId.toString(), fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
        NodeState.LOCKED -> {
            // Nút Khóa Xám
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray, CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Text("🔒", fontSize = 32.sp)
            }
        }
    }
}

// ==========================================
// 5. PREVIEW - DÙNG ĐỂ XEM TRƯỚC TRONG ANDROID STUDIO
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun MapScreenPreview() {
    // Dữ liệu giả lập để vẽ lên màn hình
    val dummyNodes = listOf(
        MapNodeUI(1, "🌲 Rừng Đếm Số", NodeState.COMPLETED, 3),
        MapNodeUI(2, null, NodeState.COMPLETED, 2),
        MapNodeUI(3, null, NodeState.COMPLETED, 3),
        MapNodeUI(4, "🥕 Trại Kéo Thả", NodeState.CURRENT, 0),
        MapNodeUI(5, null, NodeState.LOCKED, 0),
        MapNodeUI(6, null, NodeState.LOCKED, 0)
    )

    MaterialTheme {
        MapScreen(
            totalStars = 15,
            nodes = dummyNodes,
            onBack = {},
            onLevelSelected = {}
        )
    }
}