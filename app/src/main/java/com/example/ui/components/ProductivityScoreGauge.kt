package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductivitySummary

@Composable
fun ProductivityScoreGauge(
    summary: ProductivitySummary,
    isGraphsExpanded: Boolean,
    onToggleGraphs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val score = summary.todayScore.coerceIn(-100, 100)

    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "animatedScore"
    )

    // Trend calculation or label
    val trendText = remember(summary.todayScore, summary.weeklyAverageScore, summary.streakDays) {
        val diff = summary.todayScore - summary.weeklyAverageScore
        when {
            diff > 0 -> "+$diff% vs 7-day average"
            diff < 0 -> "$diff% vs 7-day average"
            summary.streakDays > 0 -> "${summary.streakDays}-day streak active"
            else -> "7d average: ${summary.weeklyAverageScore}%"
        }
    }

    // Mini bar heights and colors for the sparkline (6 bars as shown in design)
    val barItems: List<Pair<Float, Boolean>> = remember(summary.weeklyHistory, summary.todayScore) {
        if (summary.weeklyHistory.isNotEmpty()) {
            val list = summary.weeklyHistory.takeLast(6).map {
                val normalized = ((it.score.coerceIn(-100, 100) + 100f) / 200f).coerceIn(0.15f, 1f)
                Pair(normalized, it.score < 0)
            }
            if (list.size < 6) {
                val padding = List(6 - list.size) { Pair(0.5f, false) }
                padding + list
            } else list
        } else {
            listOf(
                Pair(0.40f, false),
                Pair(0.60f, false),
                Pair(0.30f, false),
                Pair(0.85f, false),
                Pair(0.70f, false),
                Pair(((score + 100f) / 200f).coerceIn(0.15f, 1f), score < 0)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleGraphs() }
            .testTag("productivity_gauge_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column: Title, Score, Trend
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRODUCTIVITY SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${animatedScore.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        if (summary.streakDays > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${summary.streakDays}d",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Side: Mini Bar Chart (Sparkline) & Dropdown indicator
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            barItems.forEach { (ratio, isNegative) ->
                                val barHeight = (ratio * 44).dp
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onToggleGraphs,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("toggle_graphs_button")
                    ) {
                        Icon(
                            imageVector = if (isGraphsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isGraphsExpanded) "Hide charts" else "Show charts",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Status summary on expansion / hint
            if (summary.totalToday > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${summary.completedToday} Done",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• ${summary.pendingToday} Remaining",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    if (summary.overduePenaltyTotal > 0) {
                        Text(
                            text = "• -${summary.overduePenaltyTotal}% Overdue Penalty",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
