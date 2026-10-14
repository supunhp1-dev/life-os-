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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.BillEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.LifeOSViewModel
import com.example.ui.theme.BorderLine
import com.example.ui.theme.CatFinance
import com.example.ui.theme.CatHome
import com.example.ui.theme.CatWork
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MutedText
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.SoftSage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoneyScreen(
    viewModel: LifeOSViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()

    val showExpenseDialog by viewModel.showExpenseDialog.collectAsState()
    val showBillDialog by viewModel.showBillDialog.collectAsState()
    val showBudgetDialog by viewModel.showBudgetDialog.collectAsState()
    val partialPayBill by viewModel.partialPayBill.collectAsState()

    val currency = settings["currency"] ?: "LKR"
    val budget = settings["monthlyBudget"]?.toDoubleOrNull() ?: 100000.0

    val today = remember { LocalDate.now() }
    val currentMonthKey = remember(today) {
        String.format(Locale.getDefault(), "%04d-%02d", today.year, today.monthValue)
    }

    // Monthly spent
    val monthlyExpenses = remember(expenses, currentMonthKey) {
        expenses.filter { it.date.startsWith(currentMonthKey) }
    }
    val spentThisMonth = remember(monthlyExpenses) { monthlyExpenses.sumOf { it.amount } }

    val unpaidBills = remember(bills) { bills.filter { !it.isPaid } }
    val billsDueTotal = remember(unpaidBills) {
        unpaidBills.sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
    }

    // Six-month spend trend
    val monthlyTrends = remember(expenses, today) {
        (5 downTo 0).map { monthOffset ->
            val monthDate = today.minusMonths(monthOffset.toLong())
            val key = String.format(Locale.getDefault(), "%04d-%02d", monthDate.year, monthDate.monthValue)
            val monthTotal = expenses.filter { it.date.startsWith(key) }.sumOf { it.amount }
            val label = monthDate.format(DateTimeFormatter.ofPattern("MMM"))
            Triple(label, monthTotal, key == currentMonthKey)
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
                            text = "MONEY, WITH CONTEXT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                        Text(
                            text = "Money & Costs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.showBillDialog.value = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Bill", fontSize = 12.sp, color = ForestGreen)
                        }

                        Button(
                            onClick = { viewModel.showExpenseDialog.value = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Expense", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Monthly Budget Card
            item {
                BudgetSummaryCard(
                    spent = spentThisMonth,
                    budget = budget,
                    currency = currency,
                    onEditBudget = { viewModel.showBudgetDialog.value = true }
                )
            }

            // 6-Month Spending Trend
            item {
                SpendingTrendCard(
                    trends = monthlyTrends,
                    currency = currency
                )
            }

            // Bills Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bills & Due Dates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Total outstanding: $currency ${String.format(Locale.getDefault(), "%,.0f", billsDueTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                    TextButton(onClick = { viewModel.showBillDialog.value = true }) {
                        Text("+ Add Bill", color = ForestGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bills List
            if (bills.isEmpty()) {
                item {
                    EmptyStateCard(message = "No bills tracked yet. Add recurring bills to plan ahead.")
                }
            } else {
                items(bills, key = { it.id }) { bill ->
                    BillItemRow(
                        bill = bill,
                        currency = currency,
                        onMarkPaid = { viewModel.markBillPaid(bill) },
                        onPartialPay = { viewModel.partialPayBill.value = bill },
                        onDelete = { viewModel.deleteBill(bill) }
                    )
                }
            }

            // Expense History Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expense History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${expenses.size} recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }

            // Expenses List
            if (expenses.isEmpty()) {
                item {
                    EmptyStateCard(message = "No expenses recorded yet. Tap '+ Expense' to log spending.")
                }
            } else {
                items(expenses, key = { it.id }) { exp ->
                    ExpenseItemRow(
                        expense = exp,
                        currency = currency,
                        onDelete = { viewModel.deleteExpense(exp) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Expense Dialog
    if (showExpenseDialog) {
        ExpenseEditDialog(
            defaultDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
            onDismiss = { viewModel.showExpenseDialog.value = false },
            onSave = { exp ->
                viewModel.saveExpense(exp)
                viewModel.showExpenseDialog.value = false
            }
        )
    }

    // Add Bill Dialog
    if (showBillDialog) {
        BillEditDialog(
            defaultDate = today.plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE),
            onDismiss = { viewModel.showBillDialog.value = false },
            onSave = { bill ->
                viewModel.saveBill(bill)
                viewModel.showBillDialog.value = false
            }
        )
    }

    // Partial Pay Dialog
    if (partialPayBill != null) {
        val b = partialPayBill!!
        val remaining = (b.amount - b.paidAmount).coerceAtLeast(0.0)
        PartialPayDialog(
            bill = b,
            remaining = remaining,
            currency = currency,
            onDismiss = { viewModel.partialPayBill.value = null },
            onPay = { amount ->
                viewModel.recordPartialPayment(b, amount)
                viewModel.partialPayBill.value = null
            }
        )
    }

    // Edit Budget Dialog
    if (showBudgetDialog) {
        EditBudgetDialog(
            currentBudget = budget,
            currency = currency,
            onDismiss = { viewModel.showBudgetDialog.value = false },
            onSave = { newBudget ->
                viewModel.updateSetting("monthlyBudget", newBudget.toString())
                viewModel.showBudgetDialog.value = false
            }
        )
    }
}

@Composable
private fun BudgetSummaryCard(
    spent: Double,
    budget: Double,
    currency: String,
    onEditBudget: () -> Unit
) {
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = (budget - spent).coerceAtLeast(0.0)
    val percentUsed = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ForestGreen),
        border = BorderStroke(1.dp, ForestGreenDark)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending Outlook",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = onEditBudget) {
                    Text("Change Budget", color = Color(0xFFD9E7B6), fontSize = 11.sp)
                }
            }

            Text(
                text = "$currency ${String.format(Locale.getDefault(), "%,.0f", spent)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "of your $currency ${String.format(Locale.getDefault(), "%,.0f", budget)} budget",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFD9E7B6),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$percentUsed% used", fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                Text(
                    text = "$currency ${String.format(Locale.getDefault(), "%,.0f", remaining)} left",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun SpendingTrendCard(
    trends: List<Triple<String, Double, Boolean>>,
    currency: String
) {
    val maxSpend = remember(trends) { (trends.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(100.0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "6-Month Spending Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                trends.forEach { (label, total, isCurrent) ->
                    val fraction = (total / maxSpend).toFloat().coerceIn(0.05f, 1f)
                    val barHeight = (70 * fraction).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isCurrent) ForestGreen else SoftSage)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) ForestGreen else MutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillItemRow(
    bill: BillEntity,
    currency: String,
    onMarkPaid: () -> Unit,
    onPartialPay: () -> Unit,
    onDelete: () -> Unit
) {
    val remaining = (bill.amount - bill.paidAmount).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_item_${bill.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (bill.isPaid) SoftSage else Color(0xFFFBF0D7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = if (bill.isPaid) ForestGreen else Color(0xFF9C7A35),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (bill.isPaid) "Paid · Next: ${bill.due}" else "Due ${bill.due} · ${bill.repeat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bill.isPaid) ForestGreen else MutedText,
                    fontSize = 11.sp
                )
                if (bill.paidAmount > 0 && !bill.isPaid) {
                    Text(
                        text = "Paid $currency ${String.format(Locale.getDefault(), "%,.0f", bill.paidAmount)} of ${String.format(Locale.getDefault(), "%,.0f", bill.amount)}",
                        fontSize = 10.sp,
                        color = CatWork
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency ${String.format(Locale.getDefault(), "%,.0f", if (bill.isPaid) bill.amount else remaining)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (bill.isPaid) MutedText else PriorityHigh
                )

                if (!bill.isPaid) {
                    Row {
                        TextButton(onClick = onPartialPay) {
                            Text("Partial", fontSize = 10.sp, color = MutedText)
                        }
                        TextButton(onClick = onMarkPaid) {
                            Text("Pay Full", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    expense: ExpenseEntity,
    currency: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PaperBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Wallet, contentDescription = null, tint = CatFinance, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${expense.date} · ${expense.category}${if (expense.notes.isNotBlank()) " · ${expense.notes}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "− $currency ${String.format(Locale.getDefault(), "%,.2f", expense.amount)}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditDialog(
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var category by remember { mutableStateOf("Home") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Home", "Food", "Travel", "Health", "Personal", "Work", "Bills", "Shopping", "Other")
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What did you spend on? *") },
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

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

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
                        categories.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { category = c; categoryExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onSave(
                            ExpenseEntity(
                                title = title.trim(),
                                date = date,
                                amount = amount,
                                category = category,
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillEditDialog(
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (BillEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var due by remember { mutableStateOf(defaultDate) }
    var repeat by remember { mutableStateOf("monthly") }

    val repeats = listOf("none", "weekly", "monthly", "yearly")
    var repeatExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Name *") },
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

                OutlinedTextField(
                    value = due,
                    onValueChange = { due = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = repeatExpanded,
                    onExpandedChange = { repeatExpanded = it }
                ) {
                    OutlinedTextField(
                        value = repeat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = repeatExpanded,
                        onDismissRequest = { repeatExpanded = false }
                    ) {
                        repeats.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { repeat = r; repeatExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onSave(
                            BillEntity(
                                title = title.trim(),
                                amount = amount,
                                due = due,
                                repeat = repeat
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PartialPayDialog(
    bill: BillEntity,
    remaining: Double,
    currency: String,
    onDismiss: () -> Unit,
    onPay: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Partial Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = "Paying for: ${bill.title}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Remaining balance: $currency ${String.format(Locale.getDefault(), "%,.2f", remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount ($currency)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (amt != null && amt > 0 && amt <= remaining) {
                        onPay(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Confirm Pay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetStr by remember { mutableStateOf(currentBudget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Set your personal spending guardrail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Budget Amount ($currency)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val b = budgetStr.toDoubleOrNull()
                    if (b != null && b > 0) {
                        onSave(b)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLine)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = message, fontSize = 12.sp, color = MutedText)
        }
    }
}
