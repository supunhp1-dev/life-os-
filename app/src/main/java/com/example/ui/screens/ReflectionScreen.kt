package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReflectionScreen(
    viewModel: LifeOSViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsMap.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val events by viewModel.events.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var wentWell by remember(settings) { mutableStateOf(settings["reviewWentWell"] ?: "") }
    var attention by remember(settings) { mutableStateOf(settings["reviewAttention"] ?: "") }
    var nextWeek by remember(settings) { mutableStateOf(settings["reviewNextWeek"] ?: "") }
    val savedAt = settings["reviewSavedAt"] ?: "Not saved yet"

    val completedTasksCount = remember(tasks) { tasks.count { it.isDone } }
    val totalTasksCount = remember(tasks) { tasks.size }
    val completionPercent = if (totalTasksCount > 0) (completedTasksCount.toFloat() / totalTasksCount.toFloat()) else 0f
    val currency = settings["currency"] ?: "LKR"
    val totalSpent = remember(expenses) { expenses.sumOf { it.amount } }

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
                text = "Weekly Reflection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.updateSetting("reviewWentWell", wentWell)
                    viewModel.updateSetting("reviewAttention", attention)
                    viewModel.updateSetting("reviewNextWeek", nextWeek)
                    viewModel.updateSetting("reviewSavedAt", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderLine)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LOOK BACK, THEN LOOK FORWARD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Take a few minutes to notice what worked and choose what deserves your energy next.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Task Completion: ${(completionPercent * 100).toInt()}% ($completedTasksCount/$totalTasksCount)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                            Text(
                                text = "Actual Spend: $currency ${String.format(Locale.getDefault(), "%,.0f", totalSpent)}",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { completionPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ForestGreen,
                            trackColor = SoftSage
                        )
                    }
                }
            }

            // Question 1
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderLine)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "01 · What went well, even if it was small?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = wentWell,
                            onValueChange = { wentWell = it },
                            placeholder = { Text("Notice the small wins and habits you want to keep…", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                }
            }

            // Question 2
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderLine)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "02 · What took more energy than it deserved?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = attention,
                            onValueChange = { attention = it },
                            placeholder = { Text("Anything to simplify, delegate, or stop carrying?", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                }
            }

            // Question 3
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderLine)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "03 · What matters most next week?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nextWeek,
                            onValueChange = { nextWeek = it },
                            placeholder = { Text("Choose one to three clear priorities for the week ahead…", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Last saved: $savedAt · Stored locally in your SQLite database",
                    fontSize = 11.sp,
                    color = MutedText,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
