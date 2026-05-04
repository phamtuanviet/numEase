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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.numease.data.model.DragDropContent
import com.example.numease.data.model.DraggableItem
import kotlin.math.roundToInt

@Composable
fun DragDropUI(
    content: DragDropContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var dropZoneBounds by remember { mutableStateOf(mapOf<String, Rect>()) }
    var placedItems by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(placedItems) {
        if (placedItems.size == content.correctMapping.size) {
            kotlinx.coroutines.delay(500)
            onAnswerSelected(1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Đề bài & Nút âm thanh (Dùng Secondary Theme cho Trại Kéo Thả)
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
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Nghe lại",
                    tint = colorScheme.onSecondaryContainer
                )
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

        // 2. KHU VỰC GIỎ CHỨA (Drop Zones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content.dropZones.forEach { zone ->
                val placedDraggableId = placedItems.entries.find { it.value == zone.id }?.key
                val placedDraggable = content.draggables.find { it.id == placedDraggableId }

                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(140.dp)
                        .onGloballyPositioned { coordinates ->
                            dropZoneBounds = dropZoneBounds.toMutableMap().apply {
                                put(zone.id, coordinates.boundsInRoot())
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Hiển thị đồ vật trong giỏ (VD: 🥕🥕)
                        Text(
                            text = zone.label,
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Khung rỗng / Vị trí đặt số
                        val holeColor = if (placedDraggable != null) colorScheme.primary else colorScheme.surfaceVariant

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .background(holeColor, CircleShape)
                        ) {
                            if (placedDraggable != null) {
                                Text(
                                    text = placedDraggable.label,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colorScheme.onPrimary
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
                if (!placedItems.containsKey(draggable.id)) {
                    DraggableNumberItem(
                        item = draggable,
                        onDragEnd = { finalPosition ->
                            var matchedZoneId: String? = null
                            for ((zoneId, rect) in dropZoneBounds) {
                                if (rect.contains(finalPosition)) {
                                    matchedZoneId = zoneId
                                    break
                                }
                            }

                            if (matchedZoneId != null) {
                                if (content.correctMapping[draggable.id] == matchedZoneId) {
                                    placedItems = placedItems.toMutableMap().apply { put(draggable.id, matchedZoneId) }
                                    onPlayAudio("Giỏi quá!")
                                } else {
                                    onPlayAudio("Chưa đúng giỏ rồi bé ơi.")
                                }
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.size(80.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

// --- COMPONENT XỬ LÝ GESTURE KÉO THẢ ---
@Composable
fun DraggableNumberItem(
    item: DraggableItem,
    onDragEnd: (Offset) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var globalPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .zIndex(if (isDragging) 10f else 1f)
            .onGloballyPositioned { coordinates ->
                globalPosition = coordinates.boundsInRoot().center
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd(globalPosition)
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .size(80.dp)
            // Hiệu ứng đổ bóng mạnh hơn khi bé chạm và kéo để tạo cảm giác "nhấc" vật thể lên
            .shadow(if (isDragging) 16.dp else 4.dp, CircleShape)
            .background(colorScheme.primary, CircleShape)

    ) {
        Text(
            text = item.label,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.onPrimary
        )
    }
}