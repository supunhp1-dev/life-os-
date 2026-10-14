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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatPersonal
import com.example.ui.theme.CatWork
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: LifeOSViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.vaultRecords.collectAsState()
    val filter by viewModel.vaultFilter.collectAsState()
    val showVaultDialog by viewModel.showVaultDialog.collectAsState()
    val editingVault by viewModel.editingVault.collectAsState()

    val today = remember { LocalDate.now() }
    val todayStr = remember(today) { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    val expiringSoonCount = remember(records, today) {
        records.count {
            val d = parseSafeDate(it.expiryDate)
            d != null && d >= today && ChronoUnit.DAYS.between(today, d) <= 30
        }
    }
    val expiredCount = remember(records, today) {
        records.count {
            val d = parseSafeDate(it.expiryDate)
            d != null && d < today
        }
    }

    val displayedRecords = remember(records, filter, today) {
        records.filter { r ->
            val d = parseSafeDate(r.expiryDate)
            when (filter) {
                "due_soon" -> d != null && d >= today && ChronoUnit.DAYS.between(today, d) <= 30
                "expired" -> d != null && d < today
                "warranty" -> r.type.equals("Warranty", ignoreCase = true)
                "document" -> r.type.equals("Document", ignoreCase = true)
                "insurance" -> r.type.equals("Insurance", ignoreCase = true)
                else -> true
            }
        }.sortedBy { it.expiryDate.ifBlank { "9999-12-31" } }
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
                            text = "THE IMPORTANT THINGS, KEPT TOGETHER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                        Text(
                            text = "Life Records (Vault)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.editingVault.value = null
                            viewModel.showVaultDialog.value = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_vault_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Record", fontSize = 12.sp)
                    }
                }
            }

            // Metrics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VaultMetricCard(
                        title = "Saved Records",
                        value = "${records.size}",
                        subtitle = "Warranties & files",
                        modifier = Modifier.weight(1f)
                    )
                    VaultMetricCard(
                        title = "Due Soon",
                        value = "$expiringSoonCount",
                        subtitle = "Next 30 days",
                        valueColor = if (expiringSoonCount > 0) PriorityMedium else ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                    VaultMetricCard(
                        title = "Expired",
                        value = "$expiredCount",
                        subtitle = if (expiredCount > 0) "Action needed" else "All valid",
                        valueColor = if (expiredCount > 0) PriorityHigh else ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Chips
            item {
                val chips = listOf(
                    "all" to "All (${records.size})",
                    "due_soon" to "Due Soon ($expiringSoonCount)",
                    "expired" to "Expired ($expiredCount)",
                    "warranty" to "Warranties",
                    "document" to "Documents",
                    "insurance" to "Insurance"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chips) { (key, label) ->
                        FilterChip(
                            selected = filter == key,
                            onClick = { viewModel.setVaultFilter(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftSage,
                                selectedLabelColor = ForestGreen
                            )
                        )
                    }
                }
            }

            // Records List
            if (displayedRecords.isEmpty()) {
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
                                text = "No matching life records",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Save warranties, insurance, documents or assets to never miss an expiry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                }
            } else {
                items(displayedRecords, key = { it.id }) { item ->
                    VaultItemCard(
                        record = item,
                        today = today,
                        onEdit = {
                            viewModel.editingVault.value = item
                            viewModel.showVaultDialog.value = true
                        },
                        onDelete = { viewModel.deleteVaultRecord(item) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Vault Add / Edit Dialog
    if (showVaultDialog) {
        VaultEditDialog(
            initialRecord = editingVault,
            defaultDate = todayStr,
            onDismiss = {
                viewModel.showVaultDialog.value = false
                viewModel.editingVault.value = null
            },
            onSave = { record ->
                viewModel.saveVaultRecord(record)
                viewModel.showVaultDialog.value = false
                viewModel.editingVault.value = null
            },
            onDelete = { record ->
                viewModel.deleteVaultRecord(record)
                viewModel.showVaultDialog.value = false
                viewModel.editingVault.value = null
            }
        )
    }
}

@Composable
private fun VaultMetricCard(
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
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = subtitle, fontSize = 9.sp, color = MutedText)
        }
    }
}

@Composable
private fun VaultItemCard(
    record: VaultEntity,
    today: LocalDate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val expiry = parseSafeDate(record.expiryDate)
    val daysRemaining = if (expiry != null) ChronoUnit.DAYS.between(today, expiry) else null

    val (badgeText, badgeBg, badgeColor) = when {
        daysRemaining == null -> Triple("No Expiry", SoftSage, ForestGreen)
        daysRemaining < 0 -> Triple("Expired ${-daysRemaining}d ago", Color(0xFFF7E5E1), PriorityHigh)
        daysRemaining <= 30 -> Triple("Due in ${daysRemaining}d", Color(0xFFFBF0D7), PriorityMedium)
        else -> Triple("Expires ${record.expiryDate}", SoftSage, ForestGreen)
    }

    val icon = when (record.type.lowercase()) {
        "warranty" -> Icons.Default.Shield
        "document" -> Icons.Default.Description
        "insurance" -> Icons.Default.Security
        "health" -> Icons.Default.HealthAndSafety
        else -> Icons.Default.Inventory2
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("vault_card_${record.id}"),
        shape = RoundedCornerShape(14.dp),
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
                        Icon(imageVector = icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = record.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.purchaseDate.isNotBlank()) {
                    Text(
                        text = "Purchased: ${record.purchaseDate}",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultEditDialog(
    initialRecord: VaultEntity?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (VaultEntity) -> Unit,
    onDelete: (VaultEntity) -> Unit
) {
    val isEdit = initialRecord != null && initialRecord.id != 0L

    var title by remember { mutableStateOf(initialRecord?.title ?: "") }
    var type by remember { mutableStateOf(initialRecord?.type ?: "Warranty") }
    var purchaseDate by remember { mutableStateOf(initialRecord?.purchaseDate ?: "") }
    var expiryDate by remember { mutableStateOf(initialRecord?.expiryDate ?: "") }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }

    val types = listOf("Warranty", "Document", "Insurance", "Health", "Asset", "Other")
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEdit) "Edit Record" else "Save Life Record", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Record Name *") },
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
                        label = { Text("Type") },
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

                OutlinedTextField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it },
                    label = { Text("Purchase / Issue Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry / Renewal Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Details / Serial / Policy # / Renewal Notes") },
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
                        onSave(
                            VaultEntity(
                                id = initialRecord?.id ?: 0L,
                                title = title.trim(),
                                type = type,
                                purchaseDate = purchaseDate.trim(),
                                expiryDate = expiryDate.trim(),
                                notes = notes.trim(),
                                tags = initialRecord?.tags ?: listOf(type)
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
                if (isEdit && initialRecord != null) {
                    TextButton(onClick = { onDelete(initialRecord) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

private fun parseSafeDate(dateStr: String): LocalDate? {
    if (dateStr.isBlank()) return null
    return try {
        LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        null
    }
}
