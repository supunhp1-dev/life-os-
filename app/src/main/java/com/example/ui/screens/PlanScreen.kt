package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.data.model.TaskEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.components.IntentionBanner
import com.example.ui.theme.BorderLine
import com.example.ui.theme.CatFinance
import com.example.ui.theme.CatFinanceBg
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatHealthBg
import com.example.ui.theme.CatHome
import com.example.ui.theme.CatHomeBg
import com.example.ui.theme.CatPersonal
import com.example.ui.theme.CatPersonalBg
import com.example.ui.theme.CatStudy
import com.example.ui.theme.CatStudyBg
import com.example.ui.theme.CatTravel
import com.example.ui.theme.CatTravelBg
import com.example.ui.theme.CatWork
import com.example.ui.theme.CatWorkBg
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.PaperBg
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlanScreen(
    viewModel: LifeOSViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calendarViewMode by viewModel.calendarViewMode.collectAsState()
    val selectedCategory by viewModel.calendarCategory.collectAsState()
    val events by viewModel.events.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()

    val showEventDialog by viewModel.showEventDialog.collectAsState()
    val editingEvent by viewModel.editingEvent.collectAsState()
    val showIntentionDialog by viewModel.showIntentionDialog.collectAsState()

    val currency = settings["currency"] ?: "LKR"
    val intention = settings["weekIntention"] ?: "A little progress, with room to breathe."

    val weekDays = remember(selectedDate) { viewModel.getWeekDays() }
    val monthDays = remember(selectedDate) { viewModel.getMonthDays() }

    // Filter events by selected category
    val filteredEvents = remember(events, selectedCategory) {
        if (selectedCategory == "all") events else events.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val dayEvents = remember(selectedDate, filteredEvents) {
        filteredEvents.filter { it.date == selectedDate }.sortedBy { it.startTime }
    }
    val dayTasks = remember(selectedDate, tasks) {
        tasks.filter { it.date == selectedDate }
    }
    val openTasksCount = remember(dayTasks) { dayTasks.count { !it.isDone } }
    val conflicts = remember(selectedDate, events) {
        viewModel.getConflictsForDate(selectedDate, events)
    }

    val plannedCost = remember(dayEvents) { dayEvents.sumOf { it.plannedCost } }
    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Weekly Intention Banner
                IntentionBanner(
                    intention = intention,
                    openTasksCount = openTasksCount,
                    eventsCount = dayEvents.size,
                    onEditClick = { viewModel.showIntentionDialog.value = true }
                )
            }

            // Calendar Navigation Toolbar with Day/Week/Month Switcher
            item {
                CalendarControlToolbar(
                    viewMode = calendarViewMode,
                    selectedDate = selectedDate,
                    onViewModeChange = { viewModel.setCalendarViewMode(it) },
                    onPrev = {
                        when (calendarViewMode) {
                            "day" -> viewModel.selectDateOffset(-1)
                            "week" -> viewModel.selectDateOffset(-7)
                            else -> viewModel.selectMonthOffset(-1)
                        }
                    },
                    onNext = {
                        when (calendarViewMode) {
                            "day" -> viewModel.selectDateOffset(1)
                            "week" -> viewModel.selectDateOffset(7)
                            else -> viewModel.selectMonthOffset(1)
                        }
                    }
                )
            }

            // Category Filter Chips
            item {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.setCalendarCategory(it) }
                )
            }

            // Conflicts Banner
            if (conflicts.isNotEmpty() && calendarViewMode != "month") {
                item {
                    ConflictWarningBanner(conflicts = conflicts)
                }
            }

            // Main View Content based on View Mode
            when (calendarViewMode) {
                "month" -> {
                    item {
                        FullMonthCalendarView(
                            monthDays = monthDays,
                            selectedDate = selectedDate,
                            todayStr = todayStr,
                            events = filteredEvents,
                            tasks = tasks,
                            onSelectDay = { dayStr ->
                                viewModel.selectDate(dayStr)
                                // Optionally expand to day view on tap
                                viewModel.setCalendarViewMode("day")
                            }
                        )
                    }

                    item {
                        CalendarColorLegend()
                    }
                }

                "week" -> {
                    item {
                        // Week Horizon Day Selector
                        WeekHorizonBar(
                            selectedDate = selectedDate,
                            weekDays = weekDays,
                            todayStr = todayStr,
                            events = filteredEvents,
                            tasks = tasks,
                            onSelectDate = { viewModel.selectDate(it) },
                            onPrevWeek = { viewModel.selectDateOffset(-7) },
                            onNextWeek = { viewModel.selectDateOffset(7) }
                        )
                    }

                    item {
                        // 7-Day Multi-Column Grid View
                        MultiDayWeekGridView(
                            weekDays = weekDays,
                            events = filteredEvents,
                            selectedDate = selectedDate,
                            todayStr = todayStr,
                            onSelectDate = { viewModel.selectDate(it) },
                            onEventClick = { ev ->
                                viewModel.editingEvent.value = ev
                                viewModel.showEventDialog.value = true
                            }
                        )
                    }

                    item {
                        CalendarColorLegend()
                    }
                }

                else -> {
                    // "day" view mode
                    item {
                        DaySummaryStrip(
                            date = selectedDate,
                            eventsCount = dayEvents.size,
                            openTasks = openTasksCount,
                            plannedCost = plannedCost,
                            currency = currency,
                            onAddEvent = {
                                viewModel.editingEvent.value = null
                                viewModel.showEventDialog.value = true
                            }
                        )
                    }

                    // 07:00 to 22:00 Timeline Slots
                    items((7..22).toList()) { hour ->
                        val hourStr = String.format(Locale.getDefault(), "%02d:00", hour)
                        val matchingEvents = dayEvents.filter {
                            val startHour = it.startTime.split(":").firstOrNull()?.toIntOrNull() ?: -1
                            startHour == hour
                        }

                        TimelineHourRow(
                            hourLabel = formatHourLabel(hour),
                            events = matchingEvents,
                            currency = currency,
                            onSlotClick = {
                                viewModel.editingEvent.value = EventEntity(
                                    title = "",
                                    date = selectedDate,
                                    startTime = hourStr,
                                    endTime = String.format(Locale.getDefault(), "%02d:00", (hour + 1).coerceAtMost(23)),
                                    category = "Personal"
                                )
                                viewModel.showEventDialog.value = true
                            },
                            onEventClick = { ev ->
                                viewModel.editingEvent.value = ev
                                viewModel.showEventDialog.value = true
                            },
                            onDeleteEvent = { ev ->
                                viewModel.deleteEvent(ev)
                            }
                        )
                    }

                    item {
                        CalendarColorLegend()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Event Create / Edit Dialog
    if (showEventDialog) {
        EventEditDialog(
            initialEvent = editingEvent,
            defaultDate = selectedDate,
            onDismiss = {
                viewModel.showEventDialog.value = false
                viewModel.editingEvent.value = null
            },
            onSave = { event ->
                viewModel.saveEvent(event)
                viewModel.showEventDialog.value = false
                viewModel.editingEvent.value = null
            },
            onDelete = { event ->
                viewModel.deleteEvent(event)
                viewModel.showEventDialog.value = false
                viewModel.editingEvent.value = null
            }
        )
    }

    // Weekly Intention Dialog
    if (showIntentionDialog) {
        EditIntentionDialog(
            currentIntention = intention,
            onDismiss = { viewModel.showIntentionDialog.value = false },
            onSave = { newIntention ->
                viewModel.updateSetting("weekIntention", newIntention)
                viewModel.showIntentionDialog.value = false
            }
        )
    }
}

@Composable
private fun CalendarControlToolbar(
    viewMode: String,
    selectedDate: String,
    onViewModeChange: (String) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val date = try {
        LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val title = when (viewMode) {
        "day" -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
        "week" -> {
            val monday = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val sunday = monday.plusDays(6)
            "${monday.format(DateTimeFormatter.ofPattern("d MMM"))} – ${sunday.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
        }
        else -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }

            // View Mode Switcher Pill (Day / Week / Month)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftSage.copy(alpha = 0.5f))
                    .padding(2.dp)
            ) {
                listOf("day" to "Day", "week" to "Week", "month" to "Month").forEach { (key, label) ->
                    val isActive = viewMode == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActive) ForestGreen else Color.Transparent)
                            .clickable { onViewModeChange(key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) Color.White else MutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    val categories = listOf(
        "all" to "All Categories",
        "Work" to "Work",
        "Personal" to "Personal",
        "Health" to "Health",
        "Home" to "Home",
        "Study" to "Study",
        "Travel" to "Travel",
        "Finance" to "Finance"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(categories) { (catKey, label) ->
            val isSelected = selectedCategory == catKey
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(catKey) },
                label = { Text(label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SoftSage,
                    selectedLabelColor = ForestGreen
                )
            )
        }
    }
}

@Composable
private fun FullMonthCalendarView(
    monthDays: List<LocalDate>,
    selectedDate: String,
    todayStr: String,
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    onSelectDay: (String) -> Unit
) {
    val selectedLocalDate = try {
        LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        LocalDate.now()
    }
    val currentMonth = selectedLocalDate.month

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Day of Week Header Row
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { dow ->
                    Text(
                        text = dow,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 6 rows of 7 days
            (0 until 6).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0 until 7).forEach { col ->
                        val dayIndex = row * 7 + col
                        if (dayIndex < monthDays.size) {
                            val day = monthDays[dayIndex]
                            val dayStr = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val isToday = dayStr == todayStr
                            val isSelected = dayStr == selectedDate
                            val isCurrentMonth = day.month == currentMonth

                            val dayEvents = events.filter { it.date == dayStr }
                            val dayTasks = tasks.filter { it.date == dayStr && !it.isDone }

                            MonthCell(
                                dayNumber = day.dayOfMonth.toString(),
                                isCurrentMonth = isCurrentMonth,
                                isToday = isToday,
                                isSelected = isSelected,
                                events = dayEvents,
                                hasTasks = dayTasks.isNotEmpty(),
                                onClick = { onSelectDay(dayStr) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun MonthCell(
    dayNumber: String,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<EventEntity>,
    hasTasks: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) ForestGreen else Color.Transparent

    Column(
        modifier = modifier
            .height(72.dp)
            .padding(1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SoftSage.copy(alpha = 0.35f) else Color.Transparent)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date circle / badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isToday) ForestGreen else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayNumber,
                fontSize = 11.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) Color.White else if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MutedText.copy(alpha = 0.4f)
            )
        }

        // Mini event category strips
        events.take(2).forEach { ev ->
            val catColor = getCategoryColor(ev.category)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(catColor.copy(alpha = 0.2f))
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = ev.title,
                    fontSize = 7.sp,
                    color = catColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (events.size > 2) {
            Text(
                text = "+${events.size - 2}",
                fontSize = 7.sp,
                color = MutedText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MultiDayWeekGridView(
    weekDays: List<LocalDate>,
    events: List<EventEntity>,
    selectedDate: String,
    todayStr: String,
    onSelectDate: (String) -> Unit,
    onEventClick: (EventEntity) -> Unit
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Week Horizon Grid",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally scrollable 7 day columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekDays.forEach { day ->
                    val dayStr = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val isToday = dayStr == todayStr
                    val isSelected = dayStr == selectedDate
                    val dayEvents = events.filter { it.date == dayStr }.sortedBy { it.startTime }

                    Column(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SoftSage.copy(alpha = 0.25f) else PaperBg)
                            .border(1.dp, if (isSelected) ForestGreen else BorderLine, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        // Day column header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDate(dayStr) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ForestGreen else MutedText
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) ForestGreen else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (dayEvents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Open", fontSize = 10.sp, color = MutedText.copy(alpha = 0.6f))
                            }
                        } else {
                            dayEvents.forEach { ev ->
                                val catColor = getCategoryColor(ev.category)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { onEventClick(ev) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = catColor.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text(
                                            text = ev.title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${ev.startTime} – ${ev.endTime}",
                                            fontSize = 9.sp,
                                            color = catColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarColorLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Category Legend", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    "Work" to CatWork,
                    "Personal" to CatPersonal,
                    "Health" to CatHealth,
                    "Home" to CatHome,
                    "Study" to CatStudy,
                    "Travel" to CatTravel,
                    "Finance" to CatFinance
                ).forEach { (name, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = name, fontSize = 9.sp, color = MutedText)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHorizonBar(
    selectedDate: String,
    weekDays: List<LocalDate>,
    todayStr: String,
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    onSelectDate: (String) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("week_horizon_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevWeek, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Week")
                }

                val startMonth = weekDays.first().month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val endMonth = weekDays.last().month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val year = weekDays.first().year
                val rangeLabel = if (startMonth == endMonth) {
                    "$startMonth ${weekDays.first().dayOfMonth} – ${weekDays.last().dayOfMonth}, $year"
                } else {
                    "$startMonth ${weekDays.first().dayOfMonth} – $endMonth ${weekDays.last().dayOfMonth}, $year"
                }

                Text(
                    text = rangeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextWeek, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Week")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items(weekDays) { day ->
                    val dayStr = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val isSelected = dayStr == selectedDate
                    val isToday = dayStr == todayStr

                    val dayEventsCount = events.count { it.date == dayStr }
                    val dayTasksCount = tasks.count { it.date == dayStr && !it.isDone }

                    DayChip(
                        dayOfWeek = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                        dayNumber = day.dayOfMonth.toString(),
                        isSelected = isSelected,
                        isToday = isToday,
                        hasEvents = dayEventsCount > 0,
                        hasTasks = dayTasksCount > 0,
                        onClick = { onSelectDate(dayStr) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    dayOfWeek: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> ForestGreen
        isToday -> SoftSage
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subTextColor = when {
        isSelected -> Color.White.copy(alpha = 0.8f)
        else -> MutedText
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayOfWeek,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = subTextColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayNumber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else ForestGreen)
                )
            }
            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else CatHome)
                )
            }
        }
    }
}

@Composable
private fun DaySummaryStrip(
    date: String,
    eventsCount: Int,
    openTasks: Int,
    plannedCost: Double,
    currency: String,
    onAddEvent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Selected Day ($date)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$eventsCount events · $openTasks to-dos",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (plannedCost > 0) {
                    Text(
                        text = "Planned cost: $currency ${String.format(Locale.getDefault(), "%,.2f", plannedCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CatWork,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onAddEvent,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("plan_add_event_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Event", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ConflictWarningBanner(conflicts: List<com.example.ui.ConflictPair>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E9)),
        border = BorderStroke(1.dp, Color(0xFFECD9BA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFF8C6834),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "${conflicts.size} overlapping event(s) detected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF8C6834)
                )
                conflicts.take(2).forEach {
                    Text(
                        text = "\"${it.eventA.title}\" (${it.eventA.startTime}) overlaps \"${it.eventB.title}\" (${it.eventB.startTime})",
                        fontSize = 11.sp,
                        color = Color(0xFF8C6834)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineHourRow(
    hourLabel: String,
    events: List<EventEntity>,
    currency: String,
    onSlotClick: () -> Unit,
    onEventClick: (EventEntity) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = hourLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MutedText,
            modifier = Modifier
                .width(46.dp)
                .padding(top = 8.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (events.isEmpty()) Color.Transparent else MaterialTheme.colorScheme.surface)
                .clickable(onClick = onSlotClick)
                .padding(2.dp)
        ) {
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PaperBg.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "+ add",
                        fontSize = 10.sp,
                        color = MutedText.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                events.forEach { ev ->
                    EventCardBlock(
                        event = ev,
                        currency = currency,
                        onClick = { onEventClick(ev) },
                        onDelete = { onDeleteEvent(ev) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun EventCardBlock(
    event: EventEntity,
    currency: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = getCategoryColor(event.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${event.startTime} – ${event.endTime}",
                        fontSize = 11.sp,
                        color = categoryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (event.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MutedText)
                        Text(text = event.location, fontSize = 11.sp, color = MutedText)
                    }
                }
                if (event.plannedCost > 0) {
                    Text(
                        text = "Cost: $currency ${String.format(Locale.getDefault(), "%,.2f", event.plannedCost)}",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditDialog(
    initialEvent: EventEntity?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit,
    onDelete: (EventEntity) -> Unit
) {
    val isEdit = initialEvent != null && initialEvent.id != 0L

    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var date by remember { mutableStateOf(initialEvent?.date ?: defaultDate) }
    var startTime by remember { mutableStateOf(initialEvent?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialEvent?.endTime ?: "10:00") }
    var category by remember { mutableStateOf(initialEvent?.category ?: "Personal") }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var plannedCostStr by remember { mutableStateOf(if (initialEvent != null && initialEvent.plannedCost > 0) initialEvent.plannedCost.toString() else "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var isAllDay by remember { mutableStateOf(initialEvent?.isAllDay ?: false) }
    var repeat by remember { mutableStateOf(initialEvent?.repeat ?: "none") }

    val categories = listOf("Personal", "Work", "Health", "Home", "Study", "Travel", "Finance")
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEdit) "Edit Event" else "Plan an Event", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = plannedCostStr,
                    onValueChange = { plannedCostStr = it },
                    label = { Text("Planned Cost (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cost = plannedCostStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            EventEntity(
                                id = initialEvent?.id ?: 0L,
                                title = title.trim(),
                                date = date,
                                startTime = startTime,
                                endTime = endTime,
                                category = category,
                                location = location.trim(),
                                plannedCost = cost,
                                description = description.trim(),
                                isAllDay = isAllDay,
                                repeat = repeat,
                                tags = initialEvent?.tags ?: listOf(category)
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (isEdit && initialEvent != null) {
                    TextButton(onClick = { onDelete(initialEvent) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun EditIntentionDialog(
    currentIntention: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentIntention) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("This Week's Intention", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Choose an intention or focus for your week ahead.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Intention") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "work" -> CatWork
        "personal" -> CatPersonal
        "health" -> CatHealth
        "home" -> CatHome
        "study" -> CatStudy
        "travel" -> CatTravel
        "finance" -> CatFinance
        else -> ForestGreen
    }
}

private fun formatHourLabel(hour: Int): String {
    val h = if (hour == 0 || hour == 12) 12 else hour % 12
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h $amPm"
}
