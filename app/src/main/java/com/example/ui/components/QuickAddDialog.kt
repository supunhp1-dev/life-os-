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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MutedText
import com.example.ui.theme.SoftSage

@Composable
fun QuickAddDialog(
    onDismiss: () -> Unit,
    onAddEvent: () -> Unit,
    onAddTask: () -> Unit,
    onAddExpense: () -> Unit,
    onAddBill: () -> Unit,
    onAddVault: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("quick_add_modal")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add to your day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep small details thoughtfully together.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                QuickAddItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Event or appointment",
                    subtitle = "Plan time with start/end, location and cost",
                    onClick = {
                        onDismiss()
                        onAddEvent()
                    }
                )

                QuickAddItem(
                    icon = Icons.Default.CheckCircleOutline,
                    title = "Task or to-do",
                    subtitle = "Add to your list with due date and priority",
                    onClick = {
                        onDismiss()
                        onAddTask()
                    }
                )

                QuickAddItem(
                    icon = Icons.Default.Wallet,
                    title = "Record an expense",
                    subtitle = "Log money actually spent with category",
                    onClick = {
                        onDismiss()
                        onAddExpense()
                    }
                )

                QuickAddItem(
                    icon = Icons.Default.Receipt,
                    title = "Add a bill or payment",
                    subtitle = "Track due date, cycle and payment status",
                    onClick = {
                        onDismiss()
                        onAddBill()
                    }
                )

                QuickAddItem(
                    icon = Icons.Default.FolderSpecial,
                    title = "Life record (Vault)",
                    subtitle = "Warranty, document, insurance or asset expiry",
                    onClick = {
                        onDismiss()
                        onAddVault()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickAddItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SoftSage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
                fontSize = 11.sp
            )
        }
    }
}
