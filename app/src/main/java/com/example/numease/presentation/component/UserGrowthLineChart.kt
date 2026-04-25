package com.example.numease.presentation.component


import androidx.compose.animation.core.tween

import androidx.compose.foundation.layout.*


import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect


@Composable
fun UserGrowthLineChart(data: List<Pair<String, Int>>) {
    if (data.isEmpty()) return

    val maxCount = data.maxOf { it.second }.coerceAtLeast(5).toFloat()
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(data) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp, bottom = 20.dp, start = 10.dp, end = 10.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)

        val points = data.mapIndexed { index, pair ->
            val x = index * spacing
            val y = height - (pair.second / maxCount) * height
            Offset(x, y)
        }

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

        // Vẽ vùng Gradient bên dưới đường
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        clipRect(right = width * animationProgress) {
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3).copy(alpha = 0.3f), Color.Transparent)
                )
            )
            drawPath(
                path = strokePath,
                color = Color(0xFF2196F3),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Vẽ các điểm mốc (Dots)
        points.forEachIndexed { index, point ->
            if (index * spacing <= width * animationProgress) {
                drawCircle(Color.White, radius = 4.dp.toPx(), center = point)
                drawCircle(Color(0xFF2196F3), radius = 3.dp.toPx(), center = point, style = Stroke(2.dp.toPx()))
            }
        }
    }
}