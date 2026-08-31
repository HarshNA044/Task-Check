package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayBadgeInfo
import com.example.data.model.Holiday
import com.example.data.model.HolidayProvider
import com.example.ui.theme.TertiaryEmerald
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

private const val PAGER_CENTER_INDEX = 1200
private const val PAGER_TOTAL_PAGES = 2400

@Composable
fun MonthCalendarView(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    dayBadges: Map<String, DayBadgeInfo>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onTodayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val baseMonth = remember { YearMonth.now() }

    val monthsDifference = (currentMonth.year - baseMonth.year) * 12 + (currentMonth.monthValue - baseMonth.monthValue)
    val targetPageIndex = PAGER_CENTER_INDEX + monthsDifference

    val pagerState = rememberPagerState(
        initialPage = targetPageIndex,
        pageCount = { PAGER_TOTAL_PAGES }
    )

    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - PAGER_CENTER_INDEX
        val newMonth = baseMonth.plusMonths(offset.toLong())
        if (newMonth != currentMonth) {
            onMonthChanged(newMonth)
        }
    }

    LaunchedEffect(currentMonth) {
        val diff = (currentMonth.year - baseMonth.year) * 12 + (currentMonth.monthValue - baseMonth.monthValue)
        val expectedPage = PAGER_CENTER_INDEX + diff
        if (pagerState.currentPage != expectedPage) {
            pagerState.animateScrollToPage(expectedPage)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_calendar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Month Header & Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Swipe horizontally to browse months",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isCurrentMonthToday = YearMonth.now() == currentMonth
                    AssistChip(
                        onClick = {
                            onTodayClicked()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(PAGER_CENTER_INDEX)
                            }
                        },
                        label = {
                            Text(
                                "Today",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isCurrentMonthToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isCurrentMonthToday) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (isCurrentMonthToday) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            leadingIconContentColor = if (isCurrentMonthToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        modifier = Modifier.testTag("calendar_today_chip")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_prev_month_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_next_month_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Days of Week Header (M T W T F S S)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val daysOfWeek = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                )

                daysOfWeek.forEach { dow ->
                    val isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
                    Text(
                        text = dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isWeekend) MaterialTheme.colorScheme.error.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            // Pager for month grid with fluid swipe animation transition
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_horizontal_pager")
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val offset = page - PAGER_CENTER_INDEX
                val pageMonth = baseMonth.plusMonths(offset.toLong())

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Subtle parallax & smooth alpha transition
                            val scale = 1f - (pageOffset * 0.05f).coerceIn(0f, 0.1f)
                            scaleX = scale
                            scaleY = scale
                            alpha = (1f - (pageOffset * 0.4f)).coerceIn(0.4f, 1f)
                        }
                ) {
                    MonthGrid(
                        month = pageMonth,
                        selectedDate = selectedDate,
                        dayBadges = dayBadges,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    dayBadges: Map<String, DayBadgeInfo>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val firstOfMonth = month.atDay(1)
    val dayOfWeekOffset = (firstOfMonth.dayOfWeek.value - 1) % 7 // Monday = 0
    val daysInMonth = month.lengthOfMonth()

    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (week in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayCol in 0 until 7) {
                    val cellIndex = week * 7 + dayCol
                    val isCurrentMonthDay = cellIndex >= dayOfWeekOffset && cellIndex < (dayOfWeekOffset + daysInMonth)

                    val date = when {
                        cellIndex < dayOfWeekOffset -> {
                            val dayNum = daysInPrevMonth - dayOfWeekOffset + cellIndex + 1
                            prevMonth.atDay(dayNum)
                        }
                        isCurrentMonthDay -> {
                            val dayNum = cellIndex - dayOfWeekOffset + 1
                            month.atDay(dayNum)
                        }
                        else -> {
                            val nextMonth = month.plusMonths(1)
                            val dayNum = cellIndex - (dayOfWeekOffset + daysInMonth) + 1
                            nextMonth.atDay(dayNum)
                        }
                    }

                    val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val badgeInfo = dayBadges[dateKey]
                    val isSelected = date == selectedDate
                    val isToday = date == today

                    DayCell(
                        date = date,
                        isCurrentMonth = isCurrentMonthDay,
                        isSelected = isSelected,
                        isToday = isToday,
                        badgeInfo = badgeInfo,
                        onDateSelected = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    badgeInfo: DayBadgeInfo?,
    onDateSelected: () -> Unit
) {
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    val holiday = remember(date) { HolidayProvider.getHoliday(date) }
    val isHoliday = holiday != null
    val holidayColor = Color(0xFFF59E0B) // Amber-Gold holiday indicator

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> primaryColor
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "cellBg"
    )

    val textColor = when {
        isSelected -> onPrimaryColor
        !isCurrentMonth -> onSurfaceVariantColor.copy(alpha = 0.35f)
        isHoliday -> holidayColor
        isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
        else -> onSurfaceColor
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                when {
                    isToday && !isSelected -> Modifier.border(1.5.dp, primaryColor, CircleShape)
                    isHoliday && !isSelected -> Modifier.border(1.5.dp, holidayColor.copy(alpha = 0.85f), CircleShape)
                    else -> Modifier
                }
            )
            .clickable { onDateSelected() }
            .testTag("calendar_day_cell_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday || isHoliday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Day Indicator Badges
            Row(
                modifier = Modifier.padding(top = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHoliday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) onPrimaryColor else holidayColor)
                    )
                }

                if (badgeInfo != null && badgeInfo.totalTasks > 0) {
                    if (badgeInfo.hasRolledOver) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) onPrimaryColor else MaterialTheme.colorScheme.secondary)
                        )
                    }

                    val statusDotColor = when {
                        isSelected -> onPrimaryColor
                        badgeInfo.completedTasks == badgeInfo.totalTasks -> TertiaryEmerald
                        badgeInfo.hasHighPriority -> MaterialTheme.colorScheme.error
                        else -> primaryColor
                    }

                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                } else if (!isHoliday) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
