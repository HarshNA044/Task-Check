package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductivityRecordEntity
import com.example.data.model.ProductivitySummary
import com.example.ui.theme.TertiaryEmerald
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DailyScorePoint(
    val date: String,
    val dayLabel: String,
    val score: Int
)

@Composable
fun ProductivityGraphsView(
    isExpanded: Boolean,
    summary: ProductivitySummary,
    history: List<ProductivityRecordEntity>,
    modifier: Modifier = Modifier
) {
    val completedColor = TertiaryEmerald
    val pendingColor = MaterialTheme.colorScheme.primary
    val overdueColor = MaterialTheme.colorScheme.error

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Trend Line Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("productivity_trend_chart_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = "Score Trend",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "7-Day Productivity Trend",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Avg: ${summary.weeklyAverageScore}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7-Day Trend Canvas Chart
                    val today = remember { LocalDate.now() }
                    val scoresList: List<DailyScorePoint> = remember(summary.weeklyHistory, today) {
                        (6 downTo 0).map { offset ->
                            val date = today.minusDays(offset.toLong())
                            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val dayLabel = date.format(DateTimeFormatter.ofPattern("E", Locale.getDefault()))
                            val foundRecord = summary.weeklyHistory.find { it.date == dateStr }
                            val score = if (offset == 0) summary.todayScore else (foundRecord?.score ?: 100)
                            DailyScorePoint(dateStr, dayLabel, score)
                        }
                    }

                    ProductivityTrendChart(scores = scoresList)
                }
            }

            // Task Completion Breakdown Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("productivity_breakdown_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.StackedBarChart,
                                contentDescription = "Task Breakdown",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Task Completion Breakdown",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = completedColor, label = "Completed (+)")
                        LegendItem(color = pendingColor, label = "Pending")
                        LegendItem(color = overdueColor, label = "Incomplete / Rolled (-)")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7-day completion breakdown bars
                    TaskBreakdownBars(
                        history = history,
                        todaySummary = summary,
                        completedColor = completedColor,
                        pendingColor = pendingColor,
                        uncompletedColor = overdueColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Penalty & Momentum Explanation Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Uncompleted tasks from previous days show as negative (-100% to -10%) and automatically roll over to the next day with delay tracking.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProductivityTrendChart(
    scores: List<DailyScorePoint>,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface
    val labelColorArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val errorColorArgb = MaterialTheme.colorScheme.error.toArgb()
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (scores.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val bottomPadding = 26.dp.toPx()
        val topPadding = 16.dp.toPx()
        val chartHeight = height - bottomPadding - topPadding

        val stepX = width / (scores.size.coerceAtLeast(2) - 1).toFloat()

        // Guidelines at +100%, 0% (baseline), and -100%
        val y100 = topPadding
        val yZero = topPadding + chartHeight * 0.5f
        val yMinus100 = topPadding + chartHeight

        // +100% line
        drawLine(
            color = outlineColor.copy(alpha = 0.4f),
            start = Offset(0f, y100),
            end = Offset(width, y100),
            strokeWidth = 1.dp.toPx()
        )

        // 0% Baseline (Prominent)
        drawLine(
            color = outlineColor.copy(alpha = 0.7f),
            start = Offset(0f, yZero),
            end = Offset(width, yZero),
            strokeWidth = 1.5.dp.toPx()
        )

        // -100% Negative line
        drawLine(
            color = errorColor.copy(alpha = 0.25f),
            start = Offset(0f, yMinus100),
            end = Offset(width, yMinus100),
            strokeWidth = 1.dp.toPx()
        )

        // Y-axis labels
        val axisPaint = android.graphics.Paint().apply {
            color = labelColorArgb
            textSize = 8.5.dp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }
        val errorAxisPaint = android.graphics.Paint().apply {
            color = errorColorArgb
            textSize = 8.5.dp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        drawContext.canvas.nativeCanvas.drawText("+100%", width - 4.dp.toPx(), y100 + 3.dp.toPx(), axisPaint)
        drawContext.canvas.nativeCanvas.drawText("0%", width - 4.dp.toPx(), yZero - 2.dp.toPx(), axisPaint)
        drawContext.canvas.nativeCanvas.drawText("-100%", width - 4.dp.toPx(), yMinus100 - 2.dp.toPx(), errorAxisPaint)

        val points = scores.mapIndexed { index, item ->
            val scoreClamped = item.score.coerceIn(-100, 100)
            // Normalized from -100..100 to 0..1
            val normalized = (scoreClamped + 100f) / 200f
            val x = index * stepX
            val y = topPadding + chartHeight * (1f - normalized)
            Offset(x, y)
        }

        // Draw Filled Area to 0% baseline
        val fillPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
            lineTo(points.last().x, yZero)
            lineTo(points.first().x, yZero)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.05f),
                    errorColor.copy(alpha = 0.30f)
                ),
                startY = y100,
                endY = yMinus100
            )
        )

        // Draw Line Curve
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
        }

        drawPath(
            path = linePath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, primaryColor, errorColor),
                startY = y100,
                endY = yMinus100
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Score Labels & Dots
        val scorePositivePaint = android.graphics.Paint().apply {
            color = primaryColorArgb
            textSize = 9.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        val scoreNegativePaint = android.graphics.Paint().apply {
            color = errorColorArgb
            textSize = 9.5.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        val dayLabelPaint = android.graphics.Paint().apply {
            color = labelColorArgb
            textSize = 10.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        points.forEachIndexed { index, pt ->
            val score = scores[index].score
            val isNeg = score < 0
            val dotColor = if (isNeg) errorColor else primaryColor

            drawCircle(
                color = surfaceColor,
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = dotColor,
                radius = 3.5.dp.toPx(),
                center = pt
            )

            // Draw score badge text above/below dot
            val scoreText = if (score > 0) "+$score%" else "$score%"
            val textY = if (isNeg) pt.y + 13.dp.toPx() else pt.y - 6.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                scoreText,
                pt.x,
                textY,
                if (isNeg) scoreNegativePaint else scorePositivePaint
            )

            // Day Label below chart
            val label = scores[index].dayLabel
            drawContext.canvas.nativeCanvas.drawText(
                label,
                pt.x,
                height - 4.dp.toPx(),
                dayLabelPaint
            )
        }
    }
}

@Composable
private fun TaskBreakdownBars(
    history: List<ProductivityRecordEntity>,
    todaySummary: ProductivitySummary,
    completedColor: Color,
    pendingColor: Color,
    uncompletedColor: Color,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val days = remember(history, todaySummary) {
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val record = history.find { it.date == dateStr }

            val completed = if (offset == 0) todaySummary.completedToday else (record?.completedCount ?: 0)
            val pending = if (offset == 0) todaySummary.pendingToday else 0
            val uncompleted = if (offset == 0) todaySummary.pendingToday else (record?.uncompletedCount ?: 0)
            val score = if (offset == 0) todaySummary.todayScore else (record?.score ?: 100)

            Tuple4(
                date.format(DateTimeFormatter.ofPattern("E", Locale.getDefault())),
                Triple(completed, pending, uncompleted),
                offset == 0,
                score
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(125.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { (dayLabel, counts, isToday, score) ->
            val (completed, pending, uncompleted) = counts
            val total = (completed + pending + uncompleted).coerceAtLeast(1)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.width(36.dp)
            ) {
                // Score indicator pill
                if (score < 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "$score%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Stacked Bar
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (uncompleted > 0) {
                            val uncompletedFrac = uncompleted.toFloat() / total
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((uncompletedFrac * 72).dp)
                                    .background(uncompletedColor)
                            )
                        }
                        if (pending > 0) {
                            val pendingFrac = pending.toFloat() / total
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((pendingFrac * 72).dp)
                                    .background(pendingColor)
                            )
                        }
                        if (completed > 0) {
                            val completedFrac = completed.toFloat() / total
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((completedFrac * 72).dp)
                                    .background(completedColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
