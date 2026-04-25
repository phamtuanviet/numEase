package com.example.numease.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.numease.data.model.DragDropContent
import com.example.numease.data.model.DraggableItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DragDropUI(
    content: DragDropContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit // Gửi 1 (Đúng) về cho ViewModel khi ghép xong hết
) {
    // Lưu trữ Tọa độ của các Giỏ (Drop Zones) trên màn hình
    var dropZoneBounds by remember { mutableStateOf(mapOf<String, Rect>()) }

    // Lưu trữ trạng thái: ID của vật thể Kéo -> ID của Giỏ (Những món đã ghép đúng)
    var placedItems by remember { mutableStateOf(mapOf<String, String>()) }

    // Kiểm tra xem đã ghép đúng hết chưa
    LaunchedEffect(placedItems) {
        if (placedItems.size == content.correctMapping.size) {
            // Đợi 0.5s cho bé nhìn thành quả rồi mới báo ViewModel chuyển câu
            kotlinx.coroutines.delay(500)
            onAnswerSelected(1) // Truyền 1 (Tượng trưng cho đúng)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Đề bài & Nút âm thanh
        Surface(
            color = Color(0xFFE8F5E9), // Màu xanh lá nhạt cho màn kéo thả
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.VolumeUp, "Nghe lại", tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 2. KHU VỰC GIỎ CHỨA (Drop Zones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content.dropZones.forEach { zone ->
                // Tìm xem có draggable nào đang được đặt trong giỏ này không
                val placedDraggableId = placedItems.entries.find { it.value == zone.id }?.key
                val placedDraggable = content.draggables.find { it.id == placedDraggableId }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(24.dp)) // Khung giỏ
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                        .onGloballyPositioned { coordinates ->
                            // Lấy tọa độ của Giỏ và lưu vào biến bounds
                            dropZoneBounds = dropZoneBounds.toMutableMap().apply {
                                put(zone.id, coordinates.boundsInRoot())
                            }
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Hiển thị đồ vật trong giỏ (VD: 🥕🥕)
                        Text(text = zone.label, fontSize = 40.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Khung trống để thả số vào
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .background(if (placedDraggable != null) Color(0xFF4CAF50) else Color.White, CircleShape)
                        ) {
                            if (placedDraggable != null) {
                                // Nếu đã có số thả vào -> Hiện số đó lên
                                Text(
                                    text = placedDraggable.label,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. KHU VỰC CÁC CON SỐ ĐỂ KÉO (Draggables)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            content.draggables.forEach { draggable ->
                // Chỉ hiển thị số ở dưới nếu nó CHƯA được đặt vào giỏ
                if (!placedItems.containsKey(draggable.id)) {
                    DraggableNumberItem(
                        item = draggable,
                        onDragEnd = { finalPosition ->
                            // Khi bé buông tay, kiểm tra xem tọa độ buông có nằm trong Giỏ nào không
                            var matchedZoneId: String? = null
                            for ((zoneId, rect) in dropZoneBounds) {
                                if (rect.contains(finalPosition)) {
                                    matchedZoneId = zoneId
                                    break
                                }
                            }

                            if (matchedZoneId != null) {
                                // Nếu thả trúng 1 giỏ -> Kiểm tra xem có đúng đáp án không
                                if (content.correctMapping[draggable.id] == matchedZoneId) {
                                    // Ghép ĐÚNG -> Lưu vào State, phát âm thanh khen
                                    placedItems = placedItems.toMutableMap().apply { put(draggable.id, matchedZoneId) }
                                    onPlayAudio("Giỏi quá!")
                                } else {
                                    // Ghép SAI -> Phát âm thanh báo sai (Nó sẽ tự nảy về nhờ Modifier)
                                    onPlayAudio("Chưa đúng giỏ rồi bé ơi.")
                                }
                            }
                        }
                    )
                } else {
                    // Chừa ra một khoảng trống (Box rỗng) để giữ nguyên bố cục khi số đã bay lên giỏ
                    Box(modifier = Modifier.size(80.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

// --- COMPONENT XỬ LÝ GESTURE KÉO THẢ TỪNG CON SỐ ---
@Composable
fun DraggableNumberItem(
    item: DraggableItem,
    onDragEnd: (Offset) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Biến để tính toán tọa độ tuyệt đối khi buông tay
    var globalPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) } // Di chuyển UI theo tọa độ
            .zIndex(if (isDragging) 10f else 1f) // Nổi lên trên cùng khi đang kéo
            .onGloballyPositioned { coordinates ->
                // Cập nhật vị trí toàn cục của ngón tay
                globalPosition = coordinates.boundsInRoot().center
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        // Báo ra ngoài tọa độ lúc buông tay
                        onDragEnd(globalPosition)
                        // Bất chấp đúng sai, reset tọa độ vật lý về 0 (nảy về chỗ cũ).
                        // Nếu đúng, UI tổng sẽ tự giấu nút này đi.
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume() // Tiêu thụ gesture để màn hình không bị cuộn
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .size(80.dp)
            .background(Color(0xFFFF9800), CircleShape)
            .shadow(if (isDragging) 12.dp else 4.dp, CircleShape)
    ) {
        Text(
            text = item.label,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}