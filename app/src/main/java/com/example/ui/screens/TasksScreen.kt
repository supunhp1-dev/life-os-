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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.CatWork
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TasksScreen(
    viewModel: LifeOSViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val filter by viewModel.taskFilter.collectAsState()
    val sort by viewModel.taskSort.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()
    val showTaskDialog by viewModel.showTaskDialog.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()

    val currency = settings["currency"] ?: "LKR"
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }

    val openTasks = remember(tasks) { tasks.filter { !it.isDone } }
    val completedTasks = remember(tasks) { tasks.filter { it.isDone } }
    val overdueTasks = remember(tasks, today) { tasks.filter { !it.isDone && it.date < today } }
    val totalPlannedCost = remember(openTasks) { openTasks.sumOf { it.cost } }

    val displayedTasks = remember(tasks, filter, sort) {
        val filtered = when (filter) {
            "open" -> openTasks
            "done" -> completedTasks
            else -> tasks
        }
        when (sort) {
            "priority" -> filtered.sortedBy {
                when (it.priority) {
                    "High" -> 0
                    "Medium" -> 1
                    else -> 2
                }
            }
            "cost" -> filtered.sortedByDescending { it.cost }
            "title" -> filtered.sortedBy { it.title.lowercase() }
            else -> filtered.sortedBy { it.date }
        }
    }

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
                // Page Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "THE SMALL THINGS THAT ADD UP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                        Text(
                            text = "Tasks & To-Dos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.editingTask.value = null
                            viewModel.showTaskDialog.value = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_task_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Task", fontSize = 12.sp)
                    }
                }
            }

            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Still To Do",
                        value = "${openTasks.size}",
                        subtitle = "Active tasks",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Overdue",
                        value = "${overdueTasks.size}",
                        subtitle = if (overdueTasks.isNotEmpty()) "Review today" else "Clear horizon",
                        valueColor = if (overdueTasks.isNotEmpty()) PriorityHigh else ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Task Cost",
                        value = "$currency ${String.format(Locale.getDefault(), "%,.0f", totalPlannedCost)}",
                        subtitle = "Planned cost",
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }

            // Filter Tabs (Open, Done, All)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderLine)
                ) {
                    val tabs = listOf("open" to "Open (${openTasks.size})", "done" to "Done (${completedTasks.size})", "all" to "All (${tasks.size})")
                    val selectedIndex = tabs.indexOfFirst { it.first == filter }.coerceAtLeast(0)

                    TabRow(
                        selectedTabIndex = selectedIndex,
                        containerColor = Color.Transparent,
                        contentColor = ForestGreen,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = ForestGreen
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, (key, label) ->
                            Tab(
                                selected = selectedIndex == index,
                                onClick = { viewModel.setTaskFilter(key) },
                                text = {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedIndex == index) ForestGreen else MutedText
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Task List
            if (displayedTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BorderLine)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (filter == "done") "No completed tasks yet" else "All caught up!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Take a breath or add a small task to move forward.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                }
            } else {
                items(displayedTasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        today = today,
                        currency = currency,
                        onToggle = { viewModel.toggleTask(task) },
                        onEdit = {
                            viewModel.editingTask.value = task
                            viewModel.showTaskDialog.value = true
                        },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Task Add / Edit Dialog
    if (showTaskDialog) {
        TaskEditDialog(
            initialTask = editingTask,
            defaultDate = today,
            onDismiss = {
                viewModel.showTaskDialog.value = false
                viewModel.editingTask.value = null
            },
            onSave = { task ->
                viewModel.saveTask(task)
                viewModel.showTaskDialog.value = false
                viewModel.editingTask.value = null
            },
            onDelete = { task ->
                viewModel.deleteTask(task)
                viewModel.showTaskDialog.value = false
                viewModel.editingTask.value = null
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = subtitle, fontSize = 9.sp, color = MutedText)
        }
    }
}

@Composable
private fun TaskItemRow(
    task: TaskEntity,
    today: String,
    currency: String,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = !task.isDone && task.date < today

    val priorityColor = when (task.priority) {
        "High" -> PriorityHigh
        "Medium" -> PriorityMedium
        else -> PriorityLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ForestGreen,
                    uncheckedColor = MutedText
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isDone) MutedText else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Due Date
                    Text(
                        text = if (isOverdue) "Overdue · ${task.date}" else task.date,
                        fontSize = 11.sp,
                        color = if (isOverdue) PriorityHigh else MutedText,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Priority Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    if (task.cost > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$currency ${String.format(Locale.getDefault(), "%,.0f", task.cost)}",
                            fontSize = 10.sp,
                            color = CatWork,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
fun TaskEditDialog(
    initialTask: TaskEntity?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    val isEdit = initialTask != null && initialTask.id != 0L

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var date by remember { mutableStateOf(initialTask?.date ?: defaultDate) }
    var time by remember { mutableStateOf(initialTask?.time ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "Medium") }
    var costStr by remember { mutableStateOf(if (initialTask != null && initialTask.cost > 0) initialTask.cost.toString() else "") }
    var notes by remember { mutableStateOf(initialTask?.notes ?: "") }

    val priorities = listOf("High", "Medium", "Low")
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEdit) "Edit Task" else "Add a Task", fontWeight = FontWeight.Bold)
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
                    label = { Text("What needs doing? *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (opt)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Priority Dropdown
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        priorities.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    priority = p
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Cost (if any)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Context") },
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
                        val cost = costStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            TaskEntity(
                                id = initialTask?.id ?: 0L,
                                title = title.trim(),
                                date = date,
                                time = time.trim(),
                                priority = priority,
                                isDone = initialTask?.isDone ?: false,
                                cost = cost,
                                notes = notes.trim(),
                                tags = initialTask?.tags ?: listOf("Task")
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
                if (isEdit && initialTask != null) {
                    TextButton(onClick = { onDelete(initialTask) }) {
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
