package com.example.data.repository

import com.example.data.LifeOSDatabase
import com.example.data.model.AssetEntity
import com.example.data.model.BillEntity
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.HabitEntity
import com.example.data.model.MaintenanceEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SettingsEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LifeOSRepository(private val database: LifeOSDatabase) {
    private val eventDao = database.eventDao()
    private val taskDao = database.taskDao()
    private val billDao = database.billDao()
    private val expenseDao = database.expenseDao()
    private val vaultDao = database.vaultDao()
    private val settingsDao = database.settingsDao()
    private val noteDao = database.noteDao()
    private val habitDao = database.habitDao()
    private val assetDao = database.assetDao()
    private val subscriptionDao = database.subscriptionDao()
    private val maintenanceDao = database.maintenanceDao()

    // Events
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    fun getEventsForDate(date: String): Flow<List<EventEntity>> = eventDao.getEventsForDate(date)
    fun getEventsBetween(startDate: String, endDate: String): Flow<List<EventEntity>> =
        eventDao.getEventsBetween(startDate, endDate)

    suspend fun insertEvent(event: EventEntity): Long = eventDao.insertEvent(event)
    suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)
    fun getTasksBetween(startDate: String, endDate: String): Flow<List<TaskEntity>> =
        taskDao.getTasksBetween(startDate, endDate)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)
    suspend fun toggleTaskDone(id: Long, isDone: Boolean) = taskDao.setDone(id, isDone)

    // Bills
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    suspend fun insertBill(bill: BillEntity): Long = billDao.insertBill(bill)
    suspend fun updateBill(bill: BillEntity) = billDao.updateBill(bill)
    suspend fun deleteBill(bill: BillEntity) = billDao.deleteBill(bill)
    suspend fun deleteBillById(id: Long) = billDao.deleteBillById(id)

    // Mark bill paid
    suspend fun markBillPaid(bill: BillEntity) {
        val remaining = (bill.amount - bill.paidAmount).coerceAtLeast(0.0)
        if (remaining > 0) {
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = bill.title,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    amount = remaining,
                    category = "Bills",
                    notes = "Payment recorded from Bill Tracker",
                    billId = bill.id,
                    tags = bill.tags
                )
            )
        }

        if (bill.repeat != "none") {
            val nextDue = calculateNextDue(bill.due, bill.repeat)
            billDao.updateBill(
                bill.copy(
                    due = nextDue,
                    isPaid = false,
                    paidAmount = 0.0
                )
            )
        } else {
            billDao.updateBill(
                bill.copy(
                    isPaid = true,
                    paidAmount = bill.amount
                )
            )
        }
    }

    // Partial bill payment
    suspend fun recordPartialPayment(bill: BillEntity, amount: Double) {
        val newPaid = (bill.paidAmount + amount).coerceAtMost(bill.amount)
        expenseDao.insertExpense(
            ExpenseEntity(
                title = "${bill.title} (Partial)",
                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                amount = amount,
                category = "Bills",
                notes = "Partial payment for ${bill.title}",
                billId = bill.id,
                tags = bill.tags
            )
        )

        if (newPaid >= bill.amount) {
            if (bill.repeat != "none") {
                val nextDue = calculateNextDue(bill.due, bill.repeat)
                billDao.updateBill(
                    bill.copy(
                        due = nextDue,
                        isPaid = false,
                        paidAmount = 0.0
                    )
                )
            } else {
                billDao.updateBill(
                    bill.copy(
                        isPaid = true,
                        paidAmount = bill.amount
                    )
                )
            }
        } else {
            billDao.updateBill(
                bill.copy(paidAmount = newPaid)
            )
        }
    }

    private fun calculateNextDue(currentDue: String, repeat: String): String {
        return try {
            val date = LocalDate.parse(currentDue, DateTimeFormatter.ISO_LOCAL_DATE)
            val nextDate = when (repeat.lowercase()) {
                "weekly" -> date.plusWeeks(1)
                "monthly" -> date.plusMonths(1)
                "quarterly" -> date.plusMonths(3)
                "yearly" -> date.plusYears(1)
                else -> date.plusMonths(1)
            }
            nextDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    // Vault
    val allVaultRecords: Flow<List<VaultEntity>> = vaultDao.getAllVaultRecords()
    suspend fun insertVault(vault: VaultEntity): Long = vaultDao.insertVault(vault)
    suspend fun updateVault(vault: VaultEntity) = vaultDao.updateVault(vault)
    suspend fun deleteVault(vault: VaultEntity) = vaultDao.deleteVault(vault)
    suspend fun deleteVaultById(id: Long) = vaultDao.deleteVaultById(id)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    // Habits
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)
    suspend fun deleteHabitById(id: Long) = habitDao.deleteHabitById(id)
    suspend fun toggleHabit(habit: HabitEntity, todayStr: String) {
        val isDoneToday = habit.lastLoggedDate == todayStr
        if (isDoneToday) {
            habitDao.updateHabit(
                habit.copy(
                    streak = (habit.streak - 1).coerceAtLeast(0),
                    lastLoggedDate = ""
                )
            )
        } else {
            habitDao.updateHabit(
                habit.copy(
                    streak = habit.streak + 1,
                    lastLoggedDate = todayStr
                )
            )
        }
    }

    // Assets
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssets()
    suspend fun insertAsset(asset: AssetEntity): Long = assetDao.insertAsset(asset)
    suspend fun updateAsset(asset: AssetEntity) = assetDao.updateAsset(asset)
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset)
    suspend fun deleteAssetById(id: Long) = assetDao.deleteAssetById(id)

    // Subscriptions
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    suspend fun insertSubscription(sub: SubscriptionEntity): Long = subscriptionDao.insertSubscription(sub)
    suspend fun updateSubscription(sub: SubscriptionEntity) = subscriptionDao.updateSubscription(sub)
    suspend fun deleteSubscription(sub: SubscriptionEntity) = subscriptionDao.deleteSubscription(sub)
    suspend fun deleteSubscriptionById(id: Long) = subscriptionDao.deleteSubscriptionById(id)
    suspend fun recordSubscriptionRenewal(sub: SubscriptionEntity) {
        // 1. Record expense
        expenseDao.insertExpense(
            ExpenseEntity(
                title = "${sub.title} Renewal",
                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                amount = sub.amount,
                category = "Bills",
                notes = "Subscription renewal recorded for ${sub.provider}",
                tags = sub.tags
            )
        )
        // 2. Advance next due date
        val nextDue = calculateNextDue(sub.nextDue, sub.cycle)
        subscriptionDao.updateSubscription(
            sub.copy(nextDue = nextDue)
        )
    }

    // Maintenance
    val allMaintenance: Flow<List<MaintenanceEntity>> = maintenanceDao.getAllMaintenance()
    suspend fun insertMaintenance(item: MaintenanceEntity): Long = maintenanceDao.insertMaintenance(item)
    suspend fun updateMaintenance(item: MaintenanceEntity) = maintenanceDao.updateMaintenance(item)
    suspend fun deleteMaintenance(item: MaintenanceEntity) = maintenanceDao.deleteMaintenance(item)
    suspend fun deleteMaintenanceById(id: Long) = maintenanceDao.deleteMaintenanceById(id)
    suspend fun logServiceCompleted(item: MaintenanceEntity, actualCost: Double) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (actualCost > 0) {
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "Maintenance: ${item.title}",
                    date = todayStr,
                    amount = actualCost,
                    category = "Maintenance",
                    notes = "Service performed by ${item.provider}",
                    tags = item.tags
                )
            )
        }
        val nextDue = try {
            val d = LocalDate.now()
            d.plusMonths(item.intervalMonths.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            todayStr
        }
        maintenanceDao.updateMaintenance(
            item.copy(
                lastService = todayStr,
                nextDue = nextDue
            )
        )
    }

    // Settings
    val allSettings: Flow<List<SettingsEntity>> = settingsDao.getAllSettings()
    fun getSetting(key: String): Flow<String?> = settingsDao.getSetting(key)
    suspend fun setSetting(key: String, value: String) =
        settingsDao.setSetting(SettingsEntity(key, value))

    // Check if database needs seeding
    suspend fun checkAndSeedIfEmpty() {
        if (eventDao.getCount() == 0 && taskDao.getCount() == 0) {
            LifeOSDatabase.seedDatabase(database)
        }
    }
}
