package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BillEntity
import com.example.data.model.EventEntity
import com.example.data.model.TaskEntity
import com.example.data.model.VaultEntity
import com.example.ui.theme.CatPersonalBg
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.SoftSage

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val type: String,
    val onClick: () -> Unit
)

@Composable
fun SearchDialog(
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    bills: List<BillEntity>,
    vaultRecords: List<VaultEntity>,
    onDismiss: () -> Unit,
    onSelectEvent: (EventEntity) -> Unit,
    onSelectTask: (TaskEntity) -> Unit,
    onSelectBill: (BillEntity) -> Unit,
    onSelectVault: (VaultEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query, events, tasks, bills, vaultRecords) {
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.lowercase().trim()
            val list = mutableListOf<SearchResultItem>()

            events.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
            }.forEach { ev ->
                list.add(
                    SearchResultItem(
                        title = ev.title,
                        subtitle = "Event · ${ev.date} · ${ev.startTime} · ${ev.category}",
                        type = "Event",
                        onClick = {
                            onDismiss()
                            onSelectEvent(ev)
                        }
                    )
                )
            }

            tasks.filter {
                it.title.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
            }.forEach { t ->
                list.add(
                    SearchResultItem(
                        title = t.title,
                        subtitle = "Task · Due ${t.date} · ${t.priority} priority",
                        type = "Task",
                        onClick = {
                            onDismiss()
                            onSelectTask(t)
                        }
                    )
                )
            }

            bills.filter {
                it.title.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
            }.forEach { b ->
                list.add(
                    SearchResultItem(
                        title = b.title,
                        subtitle = "Bill · Due ${b.due} · ${if (b.isPaid) "Paid" else "Pending"}",
                        type = "Bill",
                        onClick = {
                            onDismiss()
                            onSelectBill(b)
                        }
                    )
                )
            }

            vaultRecords.filter {
                it.title.lowercase().contains(q) ||
                it.type.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
            }.forEach { v ->
                list.add(
                    SearchResultItem(
                        title = v.title,
                        subtitle = "Vault · ${v.type} · Expiry: ${v.expiryDate.ifBlank { "None" }}",
                        type = "Vault",
                        onClick = {
                            onDismiss()
                            onSelectVault(v)
                        }
                    )
                )
            }

            list
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("search_modal")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search LifeOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Find events, tasks, bills, records…", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ForestGreen)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (query.isBlank()) {
                    Text(
                        text = "Type any word, tag, or date to search across your local database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else if (results.isEmpty()) {
                    Text(
                        text = "No matches found for \"$query\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(results) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { item.onClick() }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SoftSage)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.type,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText,
                                        fontSize = 11.sp
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
