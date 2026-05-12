package com.example.numease.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun UserGrowthLineChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val colorScheme = MaterialTheme.colorScheme

    val primaryColor = Color(0xFF2196F3)
    val softFillColor = primaryColor.copy(alpha = 0.35f)
    val labelColor = colorScheme.onSurfaceVariant
    val gridLineColor = labelColor.copy(alpha = 0.15f)

    val maxDataValue = data.maxOf { it.second }
    val maxCount = max(maxDataValue, 1).toFloat()
    val ySteps = if (maxCount <= 4) maxCount.toInt() else 4

    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(data) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val labelStyle = TextStyle(color = labelColor, fontSize = 12.sp)

        // 1. QUẢN LÝ CÁC KHOẢNG ĐỆM (PADDING)
        val yAxisTextPadding = 45.dp.toPx() // Không gian cho cột chữ số Y bên trái
        val xAxisTextPadding = 30.dp.toPx() // Không gian cho hàng ngày tháng X bên dưới

        // KHOẢNG ĐỆM NỘI BỘ (Giải quyết lỗi dính trục): Thụt các điểm đồ thị vào trong
        val innerPaddingX = 16.dp.toPx() // Dịch điểm đầu sang phải, điểm cuối sang trái
        val innerPaddingY = 12.dp.toPx() // Dịch điểm cao nhất xuống, điểm thấp nhất lên

        // Khu vực chứa toàn bộ lưới (Grid)
        val gridAreaHeight = size.height - xAxisTextPadding
        val gridAreaWidth = size.width - yAxisTextPadding

        // Khu vực an toàn để vẽ các chấm tròn (Trừ đi khoảng đệm nội bộ)
        val drawAreaHeight = gridAreaHeight - (innerPaddingY * 2)
        val drawAreaWidth = gridAreaWidth - (innerPaddingX * 2)

        // Tọa độ bắt đầu thực sự của điểm biểu đồ đầu tiên
        val startX = yAxisTextPadding + innerPaddingX

        // 2. VẼ TRỤC Y VÀ ĐƯỜNG LƯỚI NGANG
        for (i in 0..ySteps) {
            val stepValue = (maxCount * i / ySteps).toInt()

            // Đường lưới cũng bắt đầu từ vùng có chứa padding Y
            val yPos = gridAreaHeight - innerPaddingY - (drawAreaHeight * i / ySteps)

            val textLayoutResult = textMeasurer.measure(text = stepValue.toString(), style = labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                color = labelColor,
                topLeft = Offset(
                    x = yAxisTextPadding - textLayoutResult.size.width - 16f, // Chữ cách đường lưới một khoảng nhỏ
                    y = yPos - textLayoutResult.size.height / 2f
                )
            )

            // Vẽ lưới ngang
            drawLine(
                color = gridLineColor,
                start = Offset(yAxisTextPadding, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 2f
            )
        }

        val effectiveDataSize = if (data.size < 2) 2 else data.size
        val spacing = drawAreaWidth / (effectiveDataSize - 1)

        // 3. TÍNH TỌA ĐỘ CÁC ĐIỂM
        val points = data.mapIndexed { index, pair ->
            val x = startX + (index * spacing)
            val ratio = pair.second.toFloat() / maxCount
            // Y nằm trong vùng drawArea, không bị chạm đáy Grid
            val y = gridAreaHeight - innerPaddingY - (ratio * drawAreaHeight)
            Offset(x, y)
        }

        // 4. VẼ TRỤC X (Ngày tháng)
        data.forEachIndexed { index, pair ->
            val xPos = startX + (index * spacing)

            if (index % 2 == 0) {
                val xTextResult = textMeasurer.measure(text = pair.first, style = labelStyle)
                drawText(
                    textLayoutResult = xTextResult,
                    color = labelColor,
                    topLeft = Offset(
                        x = xPos - xTextResult.size.width / 2f,
                        y = gridAreaHeight + 12f // Dịch chữ X xuống dưới đường lưới đáy
                    )
                )
            }
        }

        // 5. PATH ĐƯỜNG CONG VÀ GRADIENT
        val strokePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val controlX = (p1.x + p2.x) / 2
                    cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                }
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            if (points.isNotEmpty()) {
                val baseY = gridAreaHeight - innerPaddingY // Gradient phủ đến đường lưới thấp nhất
                lineTo(points.last().x, baseY)
                lineTo(points.first().x, baseY)
            }
            close()
        }

        // 6. ANIMATION & VẼ PATH
        clipRect(
            left = 0f,
            right = startX + drawAreaWidth * animationProgress + 20f, // +20f để viền dot cuối không bị cắt
            top = 0f,
            bottom = size.height
        ) {
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(softFillColor, Color.Transparent),
                    startY = 0f,
                    endY = gridAreaHeight - innerPaddingY
                )
            )
            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 7. VẼ CÁC CHẤM TRÒN (DOTS)
        points.forEachIndexed { index, point ->
            if (point.x <= startX + drawAreaWidth * animationProgress + 5f) {
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}