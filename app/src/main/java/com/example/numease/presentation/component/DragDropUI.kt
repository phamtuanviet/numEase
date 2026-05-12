package com.example.numease.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DragDropUI(
    content: DragDropContent,
    onPlayAudio: (String) -> Unit,
    onAnswerSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var dropZoneBounds by remember { mutableStateOf(mapOf<String, Rect>()) }
    var placedItems by remember { mutableStateOf(mapOf<String, String>()) }
    var isAllCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(placedItems) {
        if (placedItems.size == content.correctMapping.size && content.correctMapping.isNotEmpty()) {
            isAllCompleted = true
            onPlayAudio("Giỏi quá! Bé ghép đúng hết rồi.")
            delay(1500)
            onAnswerSelected(1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Đề bài & Nút âm thanh
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPlayAudio(content.instruction.text) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Nghe lại",
                    tint = colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content.instruction.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 2. KHU VỰC GIỎ CHỨA (Drop Zones)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max), // FIX 1: Giúp tất cả các thẻ trong Row có cùng chiều cao với thẻ cao nhất
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content.dropZones.forEach { zone ->
                val placedDraggableId = placedItems.entries.find { it.value == zone.id }?.key
                val placedDraggable = content.draggables.find { it.id == placedDraggableId }
                val isFilled = placedDraggable != null

                val cardElevation by animateFloatAsState(if (isFilled) 2f else 6f, label = "elevation")
                val holeColor by animateColorAsState(
                    targetValue = if (isFilled) Color(0xFF4CAF50) else colorScheme.surfaceVariant,
                    label = "holeColor"
                )

                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = cardElevation.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight() // FIX 2: Bắt buộc thẻ giãn hết cỡ theo chiều cao Max của Row
                        .defaultMinSize(minHeight = 200.dp) // FIX 3: Thay height cứng thành chiều cao tối thiểu để tự động nở ra khi có > 10 xe
                        .onGloballyPositioned { coordinates ->
                            dropZoneBounds = dropZoneBounds.toMutableMap().apply {
                                put(zone.id, coordinates.boundsInRoot())
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Vật phẩm trong giỏ (Dùng Box để căn giữa ô tô)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp), // Thêm chút khoảng trống để ô tô không sát vào ô thả số
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = zone.label,
                                fontSize = 24.sp,
                                lineHeight = 30.sp, // FIX 4: Ép lineHeight nhỏ lại một chút để 10 ô tô không làm thẻ dài quá khổ màn hình
                                textAlign = TextAlign.Center
                            )
                        }

                        // Khung hứng số
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .background(holeColor, CircleShape)
                        ) {
                            if (isFilled) {
                                Text(
                                    text = placedDraggable!!.label,
                                    fontSize = 26.sp,
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
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content.draggables.forEach { draggable ->
                if (!placedItems.containsKey(draggable.id)) {
                    DraggableNumberItem(
                        item = draggable,
                        onDragEnd = { finalPosition ->
                            if (isAllCompleted) return@DraggableNumberItem false

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
                                    onPlayAudio("Đúng rồi!")
                                    return@DraggableNumberItem true
                                } else {
                                    onPlayAudio("Chưa đúng giỏ rồi bé ơi.")
                                }
                            }
                            return@DraggableNumberItem false
                        }
                    )
                } else {
                    Box(modifier = Modifier.size(70.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Component xử lý Kéo/Thả giữ nguyên
@Composable
fun DraggableNumberItem(
    item: DraggableItem,
    onDragEnd: (Offset) -> Boolean
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
                        val isSuccess = onDragEnd(globalPosition)
                        if (!isSuccess) {
                            offsetX = 0f
                            offsetY = 0f
                        }
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
            .size(70.dp)
            .scale(if (isDragging) 1.15f else 1f)
            .shadow(if (isDragging) 16.dp else 4.dp, CircleShape)
            .background(colorScheme.primary, CircleShape)

    ) {
        Text(
            text = item.label,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.onPrimary
        )
    }
}