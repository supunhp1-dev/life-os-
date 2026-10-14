package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.CatHome
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HabitsScreen(
    viewModel: LifeOSViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }

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
                text = "Habits & Routines",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editingHabit = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Habit", fontSize = 12.sp)
            }
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
                        text = "Small, repeatable actions build momentum. Tap a habit's checkbox to log today's completion and protect your streak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestGreen,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No habits tracked yet. Tap '+ New Habit' to start.", color = MutedText)
                    }
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    val isDoneToday = habit.lastLoggedDate == todayStr

                    HabitItemCard(
                        habit = habit,
                        isDoneToday = isDoneToday,
                        onToggle = { viewModel.toggleHabit(habit) },
                        onEdit = {
                            editingHabit = habit
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteHabit(habit) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDialog) {
        HabitEditDialog(
            initialHabit = editingHabit,
            onDismiss = {
                showDialog = false
                editingHabit = null
            },
            onSave = { habit ->
                viewModel.saveHabit(habit)
                showDialog = false
                editingHabit = null
            },
            onDelete = { habit ->
                viewModel.deleteHabit(habit)
                showDialog = false
                editingHabit = null
            }
        )
    }
}

@Composable
private fun HabitItemCard(
    habit: HabitEntity,
    isDoneToday: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDoneToday,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ForestGreen,
                    uncheckedColor = MutedText
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Frequency: ${habit.frequency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            // Streak Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFBF0D7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = CatHome,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${habit.streak}d streak",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CatHome
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun HabitEditDialog(
    initialHabit: HabitEntity?,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit,
    onDelete: (HabitEntity) -> Unit
) {
    val isEdit = initialHabit != null && initialHabit.id != 0L
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var frequency by remember { mutableStateOf(initialHabit?.frequency ?: "daily") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Habit" else "New Habit", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Frequency (e.g. daily, weekdays, 3x a week)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            HabitEntity(
                                id = initialHabit?.id ?: 0L,
                                title = title.trim(),
                                frequency = frequency.trim(),
                                streak = initialHabit?.streak ?: 0,
                                lastLoggedDate = initialHabit?.lastLoggedDate ?: ""
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (isEdit && initialHabit != null) {
                    TextButton(onClick = { onDelete(initialHabit) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
