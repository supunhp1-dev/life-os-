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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.model.MaintenanceEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MaintenanceScreen(
    viewModel: LifeOSViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maintenanceList by viewModel.maintenance.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()

    var editingMaintenance by remember { mutableStateOf<MaintenanceEntity?>(null) }
    var loggingServiceFor by remember { mutableStateOf<MaintenanceEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val currency = settings["currency"] ?: "LKR"
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }

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
                text = "Maintenance & Service",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editingMaintenance = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule", fontSize = 12.sp)
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
                        text = "Care for what you own. Keep service intervals, vehicle maintenance, and home appliance upkeep on schedule.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestGreen,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (maintenanceList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No maintenance schedules yet. Tap '+ Schedule' to add one.", color = MutedText)
                    }
                }
            } else {
                items(maintenanceList, key = { it.id }) { item ->
                    val isOverdue = item.nextDue.isNotBlank() && item.nextDue < today

                    MaintenanceCardItem(
                        item = item,
                        currency = currency,
                        isOverdue = isOverdue,
                        onLogService = { loggingServiceFor = item },
                        onEdit = {
                            editingMaintenance = item
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteMaintenance(item) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDialog) {
        MaintenanceEditDialog(
            initialItem = editingMaintenance,
            onDismiss = {
                showDialog = false
                editingMaintenance = null
            },
            onSave = { item ->
                viewModel.saveMaintenance(item)
                showDialog = false
                editingMaintenance = null
            },
            onDelete = { item ->
                viewModel.deleteMaintenance(item)
                showDialog = false
                editingMaintenance = null
            }
        )
    }

    if (loggingServiceFor != null) {
        val target = loggingServiceFor!!
        LogServiceDialog(
            item = target,
            currency = currency,
            onDismiss = { loggingServiceFor = null },
            onConfirm = { cost ->
                viewModel.logServiceCompleted(target, cost)
                loggingServiceFor = null
            }
        )
    }
}

@Composable
private fun MaintenanceCardItem(
    item: MaintenanceEntity,
    currency: String,
    isOverdue: Boolean,
    onLogService: () -> Unit,
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.type,
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isOverdue) Color(0xFFF7E5E1) else SoftSage)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isOverdue) "Overdue" else "Next: ${item.nextDue.ifBlank { "Unscheduled" }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) PriorityHigh else ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Interval: Every ${item.intervalMonths} months · Provider: ${item.provider.ifBlank { "Local" }}",
                fontSize = 11.sp,
                color = MutedText
            )

            if (item.notes.isNotBlank()) {
                Text(text = item.notes, fontSize = 11.sp, color = MutedText)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last: ${item.lastService.ifBlank { "Never logged" }}",
                    fontSize = 10.sp,
                    color = MutedText
                )

                Row {
                    Button(
                        onClick = onLogService,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Service", fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LogServiceDialog(
    item: MaintenanceEntity,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var costStr by remember { mutableStateOf(if (item.estimatedCost > 0) item.estimatedCost.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Completed Service", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = "Logging service for: ${item.title}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "This will update the last service date to today and calculate the next due date.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Actual Service Cost ($currency)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    onConfirm(cost)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceEditDialog(
    initialItem: MaintenanceEntity?,
    onDismiss: () -> Unit,
    onSave: (MaintenanceEntity) -> Unit,
    onDelete: (MaintenanceEntity) -> Unit
) {
    val isEdit = initialItem != null && initialItem.id != 0L

    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var type by remember { mutableStateOf(initialItem?.type ?: "Routine service") }
    var lastService by remember { mutableStateOf(initialItem?.lastService ?: "") }
    var nextDue by remember { mutableStateOf(initialItem?.nextDue ?: LocalDate.now().plusMonths(6).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var intervalMonthsStr by remember { mutableStateOf(initialItem?.intervalMonths?.toString() ?: "6") }
    var estimatedCostStr by remember { mutableStateOf(if (initialItem != null && initialItem.estimatedCost > 0) initialItem.estimatedCost.toString() else "") }
    var provider by remember { mutableStateOf(initialItem?.provider ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    val types = listOf("Routine service", "Vehicle service", "Safety inspection", "Filter replacement", "Repair", "Other")
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Schedule" else "New Schedule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Maintenance Item *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Service Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = intervalMonthsStr,
                        onValueChange = { intervalMonthsStr = it },
                        label = { Text("Interval (Mos)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = estimatedCostStr,
                        onValueChange = { estimatedCostStr = it },
                        label = { Text("Est Cost") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = nextDue,
                    onValueChange = { nextDue = it },
                    label = { Text("Next Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Service Provider / Center") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val interval = intervalMonthsStr.toIntOrNull() ?: 6
                    val cost = estimatedCostStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank()) {
                        onSave(
                            MaintenanceEntity(
                                id = initialItem?.id ?: 0L,
                                title = title.trim(),
                                type = type,
                                lastService = lastService.trim(),
                                nextDue = nextDue.trim(),
                                intervalMonths = interval,
                                estimatedCost = cost,
                                provider = provider.trim(),
                                notes = notes.trim()
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
                if (isEdit && initialItem != null) {
                    TextButton(onClick = { onDelete(initialItem) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
