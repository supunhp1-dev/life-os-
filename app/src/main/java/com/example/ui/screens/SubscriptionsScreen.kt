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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.CatWork
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SubscriptionsScreen(
    viewModel: LifeOSViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()

    var editingSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val currency = settings["currency"] ?: "LKR"
    val activeSubs = remember(subscriptions) { subscriptions.filter { it.status == "active" } }

    // Normalized monthly commitment
    val monthlyTotal = remember(activeSubs) {
        activeSubs.sumOf { s ->
            when (s.cycle.lowercase()) {
                "weekly" -> s.amount * (52.0 / 12.0)
                "quarterly" -> s.amount / 3.0
                "yearly" -> s.amount / 12.0
                else -> s.amount
            }
        }
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
                text = "Subscriptions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editingSub = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Plan", fontSize = 12.sp)
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
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreen),
                    border = BorderStroke(1.dp, ForestGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estimated Monthly Commitment",
                            fontSize = 11.sp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$currency ${String.format(Locale.getDefault(), "%,.0f", monthlyTotal)} / mo",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "${activeSubs.size} active subscriptions normalized from billing cycles",
                            fontSize = 11.sp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (subscriptions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No subscriptions tracked yet. Tap '+ Add Plan' to start.", color = MutedText)
                    }
                }
            } else {
                items(subscriptions, key = { it.id }) { sub ->
                    SubscriptionCardItem(
                        sub = sub,
                        currency = currency,
                        onRecordRenewal = { viewModel.recordSubscriptionRenewal(sub) },
                        onEdit = {
                            editingSub = sub
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteSubscription(sub) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDialog) {
        SubscriptionEditDialog(
            initialSub = editingSub,
            onDismiss = {
                showDialog = false
                editingSub = null
            },
            onSave = { sub ->
                viewModel.saveSubscription(sub)
                showDialog = false
                editingSub = null
            },
            onDelete = { sub ->
                viewModel.deleteSubscription(sub)
                showDialog = false
                editingSub = null
            }
        )
    }
}

@Composable
private fun SubscriptionCardItem(
    sub: SubscriptionEntity,
    currency: String,
    onRecordRenewal: () -> Unit,
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
                        Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (sub.provider.isNotBlank()) {
                            Text(text = sub.provider, fontSize = 11.sp, color = MutedText)
                        }
                    }
                }

                Text(
                    text = "$currency ${String.format(Locale.getDefault(), "%,.0f", sub.amount)} / ${sub.cycle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ForestGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Renewal: ${sub.nextDue.ifBlank { "Unscheduled" }}",
                    fontSize = 11.sp,
                    color = MutedText
                )

                Row {
                    Button(
                        onClick = onRecordRenewal,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftSage),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Record Paid", color = ForestGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEditDialog(
    initialSub: SubscriptionEntity?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit
) {
    val isEdit = initialSub != null && initialSub.id != 0L

    var title by remember { mutableStateOf(initialSub?.title ?: "") }
    var provider by remember { mutableStateOf(initialSub?.provider ?: "") }
    var amountStr by remember { mutableStateOf(if (initialSub != null) initialSub.amount.toString() else "") }
    var cycle by remember { mutableStateOf(initialSub?.cycle ?: "monthly") }
    var nextDue by remember { mutableStateOf(initialSub?.nextDue ?: LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var status by remember { mutableStateOf(initialSub?.status ?: "active") }
    var cancelInfo by remember { mutableStateOf(initialSub?.cancelInfo ?: "") }

    val cycles = listOf("weekly", "monthly", "quarterly", "yearly")
    var cycleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Subscription" else "Add Subscription", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Service / Plan Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Provider (e.g. Google, Spotify)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = cycleExpanded,
                    onExpandedChange = { cycleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = cycle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = cycleExpanded,
                        onDismissRequest = { cycleExpanded = false }
                    ) {
                        cycles.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { cycle = c; cycleExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = nextDue,
                    onValueChange = { nextDue = it },
                    label = { Text("Next Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = cancelInfo,
                    onValueChange = { cancelInfo = it },
                    label = { Text("Cancellation / Notice terms (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onSave(
                            SubscriptionEntity(
                                id = initialSub?.id ?: 0L,
                                title = title.trim(),
                                provider = provider.trim(),
                                amount = amt,
                                cycle = cycle,
                                nextDue = nextDue.trim(),
                                status = status,
                                cancelInfo = cancelInfo.trim()
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
                if (isEdit && initialSub != null) {
                    TextButton(onClick = { onDelete(initialSub) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
