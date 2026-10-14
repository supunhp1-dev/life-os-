package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ReminderItem(
    val title: String,
    val subtitle: String,
    val date: String,
    val type: String,
    val icon: ImageVector,
    val daysRemaining: Long
)

@Composable
fun RemindersScreen(
    viewModel: LifeOSViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val vaultRecords by viewModel.vaultRecords.collectAsState()

    val today = remember { LocalDate.now() }
    val todayStr = remember(today) { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    val allReminders = remember(events, tasks, bills, subscriptions, vaultRecords, today) {
        val list = mutableListOf<ReminderItem>()

        // 1. Events on or after today
        events.filter { it.date >= todayStr }.forEach { ev ->
            val d = parseSafeDate(ev.date)
            val days = if (d != null) ChronoUnit.DAYS.between(today, d) else 0L
            list.add(
                ReminderItem(
                    title = ev.title,
                    subtitle = "Event · ${ev.date} at ${ev.startTime} · ${ev.category}",
                    date = ev.date,
                    type = "Event",
                    icon = Icons.Default.CalendarMonth,
                    daysRemaining = days
                )
            )
        }

        // 2. Open Tasks with due dates
        tasks.filter { !it.isDone }.forEach { t ->
            val d = parseSafeDate(t.date)
            val days = if (d != null) ChronoUnit.DAYS.between(today, d) else 0L
            list.add(
                ReminderItem(
                    title = t.title,
                    subtitle = "Task · ${t.priority} Priority · Due ${t.date}",
                    date = t.date,
                    type = "Task",
                    icon = Icons.Default.CheckCircleOutline,
                    daysRemaining = days
                )
            )
        }

        // 3. Unpaid Bills
        bills.filter { !it.isPaid }.forEach { b ->
            val d = parseSafeDate(b.due)
            val days = if (d != null) ChronoUnit.DAYS.between(today, d) else 0L
            list.add(
                ReminderItem(
                    title = "Bill: ${b.title}",
                    subtitle = "Amount: ${b.amount} · Due ${b.due}",
                    date = b.due,
                    type = "Bill",
                    icon = Icons.Default.Receipt,
                    daysRemaining = days
                )
            )
        }

        // 4. Subscriptions renewing soon
        subscriptions.filter { it.status == "active" && it.nextDue.isNotBlank() }.forEach { s ->
            val d = parseSafeDate(s.nextDue)
            val days = if (d != null) ChronoUnit.DAYS.between(today, d) else 0L
            list.add(
                ReminderItem(
                    title = "Subscription: ${s.title}",
                    subtitle = "${s.cycle} renewal on ${s.nextDue}",
                    date = s.nextDue,
                    type = "Subscription",
                    icon = Icons.Default.Repeat,
                    daysRemaining = days
                )
            )
        }

        // 5. Vault records expiring
        vaultRecords.filter { it.expiryDate.isNotBlank() }.forEach { v ->
            val d = parseSafeDate(v.expiryDate)
            val days = if (d != null) ChronoUnit.DAYS.between(today, d) else 0L
            list.add(
                ReminderItem(
                    title = "${v.type}: ${v.title}",
                    subtitle = "Expiration date: ${v.expiryDate}",
                    date = v.expiryDate,
                    type = "Vault",
                    icon = Icons.Default.FolderSpecial,
                    daysRemaining = days
                )
            )
        }

        list.sortedBy { it.daysRemaining }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Reminders & Nudges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftSage.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, SoftSage)
                ) {
                    Text(
                        text = "A gentle nudge at the right time. Unifies upcoming events, task deadlines, bill due dates, and record expiries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestGreen,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (allReminders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No upcoming reminders scheduled.", color = MutedText)
                    }
                }
            } else {
                items(allReminders) { item ->
                    ReminderRowItem(item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReminderRowItem(item: ReminderItem) {
    val (statusLabel, badgeBg, badgeColor) = when {
        item.daysRemaining < 0 -> Triple("Overdue by ${-item.daysRemaining}d", Color(0xFFF7E5E1), PriorityHigh)
        item.daysRemaining == 0L -> Triple("Due Today", Color(0xFFFBF0D7), PriorityMedium)
        item.daysRemaining <= 7 -> Triple("Due in ${item.daysRemaining}d", Color(0xFFFBF0D7), PriorityMedium)
        else -> Triple("In ${item.daysRemaining}d", SoftSage, ForestGreen)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}

private fun parseSafeDate(dateStr: String): LocalDate? {
    if (dateStr.isBlank()) return null
    return try {
        LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        null
    }
}
