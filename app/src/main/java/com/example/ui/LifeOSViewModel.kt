package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AssetEntity
import com.example.data.model.BillEntity
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.HabitEntity
import com.example.data.model.MaintenanceEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.VaultEntity
import com.example.data.repository.LifeOSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class LifeOSTab(val title: String) {
    PLAN("Plan"),
    TASKS("Tasks"),
    MONEY("Money"),
    VAULT("Vault")
}

data class ConflictPair(
    val eventA: EventEntity,
    val eventB: EventEntity
)

class LifeOSViewModel(private val repository: LifeOSRepository) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Selected Navigation Tab
    private val _selectedTab = MutableStateFlow(LifeOSTab.PLAN)
    val selectedTab: StateFlow<LifeOSTab> = _selectedTab.asStateFlow()

    // Selected Date
    private val _selectedDate = MutableStateFlow(LocalDate.now().format(dateFormatter))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Calendar View Mode: "day", "week", "month"
    private val _calendarViewMode = MutableStateFlow("week")
    val calendarViewMode: StateFlow<String> = _calendarViewMode.asStateFlow()

    // Calendar Category Filter: "all", "Work", "Personal", etc.
    private val _calendarCategory = MutableStateFlow("all")
    val calendarCategory: StateFlow<String> = _calendarCategory.asStateFlow()

    // Tasks Filter & Sort
    private val _taskFilter = MutableStateFlow("open")
    val taskFilter: StateFlow<String> = _taskFilter.asStateFlow()

    private val _taskSort = MutableStateFlow("date")
    val taskSort: StateFlow<String> = _taskSort.asStateFlow()

    // Vault Filter
    private val _vaultFilter = MutableStateFlow("all")
    val vaultFilter: StateFlow<String> = _vaultFilter.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // UI Dialog States
    val showQuickAddDialog = MutableStateFlow(false)
    val showSearchDialog = MutableStateFlow(false)
    val showEventDialog = MutableStateFlow(false)
    val showTaskDialog = MutableStateFlow(false)
    val showExpenseDialog = MutableStateFlow(false)
    val showBillDialog = MutableStateFlow(false)
    val showVaultDialog = MutableStateFlow(false)
    val showIntentionDialog = MutableStateFlow(false)
    val showBudgetDialog = MutableStateFlow(false)
    val showNotesSheet = MutableStateFlow(false)
    val showHabitsSheet = MutableStateFlow(false)
    val showReflectionSheet = MutableStateFlow(false)
    val showSettingsSheet = MutableStateFlow(false)

    // Editing items
    val editingEvent = MutableStateFlow<EventEntity?>(null)
    val editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingExpense = MutableStateFlow<ExpenseEntity?>(null)
    val editingBill = MutableStateFlow<BillEntity?>(null)
    val editingVault = MutableStateFlow<VaultEntity?>(null)
    val editingNote = MutableStateFlow<NoteEntity?>(null)
    val editingHabit = MutableStateFlow<HabitEntity?>(null)
    val editingAsset = MutableStateFlow<AssetEntity?>(null)
    val editingSubscription = MutableStateFlow<SubscriptionEntity?>(null)
    val editingMaintenance = MutableStateFlow<MaintenanceEntity?>(null)
    val partialPayBill = MutableStateFlow<BillEntity?>(null)

    // Room Database Flows
    val events: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultRecords: StateFlow<List<VaultEntity>> = repository.allVaultRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenance: StateFlow<List<MaintenanceEntity>> = repository.allMaintenance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsMap: StateFlow<Map<String, String>> = repository.allSettings
        .map { list -> list.associate { it.key to it.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            repository.checkAndSeedIfEmpty()
            val defaultView = repository.getSetting("defaultCalendarView")
            defaultView.collect { view ->
                if (!view.isNullOrBlank()) {
                    _calendarViewMode.value = view
                }
            }
        }
    }

    // Navigation Tab
    fun selectTab(tab: LifeOSTab) {
        _selectedTab.value = tab
    }

    // Calendar View Mode
    fun setCalendarViewMode(mode: String) {
        _calendarViewMode.value = mode
        viewModelScope.launch {
            repository.setSetting("defaultCalendarView", mode)
        }
    }

    fun setCalendarCategory(category: String) {
        _calendarCategory.value = category
    }

    // Date Selection
    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectDateOffset(days: Long) {
        try {
            val current = LocalDate.parse(_selectedDate.value, dateFormatter)
            _selectedDate.value = current.plusDays(days).format(dateFormatter)
        } catch (e: Exception) {
            _selectedDate.value = LocalDate.now().format(dateFormatter)
        }
    }

    fun selectMonthOffset(months: Long) {
        try {
            val current = LocalDate.parse(_selectedDate.value, dateFormatter)
            _selectedDate.value = current.plusMonths(months).format(dateFormatter)
        } catch (e: Exception) {
            _selectedDate.value = LocalDate.now().format(dateFormatter)
        }
    }

    fun selectToday() {
        _selectedDate.value = LocalDate.now().format(dateFormatter)
    }

    // Week Days based on selectedDate
    fun getWeekDays(): List<LocalDate> {
        val current = try {
            LocalDate.parse(_selectedDate.value, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val monday = current.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    // Month Days (42 cells: 6 rows of 7 days)
    fun getMonthDays(): List<LocalDate> {
        val current = try {
            LocalDate.parse(_selectedDate.value, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val firstOfMonth = current.withDayOfMonth(1)
        val firstMonday = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..41).map { firstMonday.plusDays(it.toLong()) }
    }

    // Conflict detection for a given date
    fun getConflictsForDate(date: String, allEvents: List<EventEntity>): List<ConflictPair> {
        val dayEvents = allEvents.filter { it.date == date && !it.isAllDay }
            .sortedBy { it.startTime }
        val conflicts = mutableListOf<ConflictPair>()
        for (i in dayEvents.indices) {
            for (j in (i + 1) until dayEvents.size) {
                val a = dayEvents[i]
                val b = dayEvents[j]
                if (a.startTime < b.endTime && a.endTime > b.startTime) {
                    conflicts.add(ConflictPair(a, b))
                }
            }
        }
        return conflicts
    }

    // Filter & Sort Setters
    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    fun setTaskSort(sort: String) {
        _taskSort.value = sort
    }

    fun setVaultFilter(filter: String) {
        _vaultFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // CRUD - Events
    fun saveEvent(event: EventEntity) {
        viewModelScope.launch {
            if (event.id == 0L) {
                repository.insertEvent(event)
            } else {
                repository.updateEvent(event)
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // CRUD - Tasks
    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskDone(task.id, !task.isDone)
        }
    }

    // CRUD - Bills
    fun saveBill(bill: BillEntity) {
        viewModelScope.launch {
            if (bill.id == 0L) {
                repository.insertBill(bill)
            } else {
                repository.updateBill(bill)
            }
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun markBillPaid(bill: BillEntity) {
        viewModelScope.launch {
            repository.markBillPaid(bill)
        }
    }

    fun recordPartialPayment(bill: BillEntity, amount: Double) {
        viewModelScope.launch {
            repository.recordPartialPayment(bill, amount)
        }
    }

    // CRUD - Expenses
    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            if (expense.id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // CRUD - Vault
    fun saveVaultRecord(record: VaultEntity) {
        viewModelScope.launch {
            if (record.id == 0L) {
                repository.insertVault(record)
            } else {
                repository.updateVault(record)
            }
        }
    }

    fun deleteVaultRecord(record: VaultEntity) {
        viewModelScope.launch {
            repository.deleteVault(record)
        }
    }

    // CRUD - Notes
    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // CRUD - Habits
    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            if (habit.id == 0L) {
                repository.insertHabit(habit)
            } else {
                repository.updateHabit(habit)
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.toggleHabit(habit, LocalDate.now().format(dateFormatter))
        }
    }

    // CRUD - Assets
    fun saveAsset(asset: AssetEntity) {
        viewModelScope.launch {
            if (asset.id == 0L) {
                repository.insertAsset(asset)
            } else {
                repository.updateAsset(asset)
            }
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    // CRUD - Subscriptions
    fun saveSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch {
            if (sub.id == 0L) {
                repository.insertSubscription(sub)
            } else {
                repository.updateSubscription(sub)
            }
        }
    }

    fun deleteSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(sub)
        }
    }

    fun recordSubscriptionRenewal(sub: SubscriptionEntity) {
        viewModelScope.launch {
            repository.recordSubscriptionRenewal(sub)
        }
    }

    // CRUD - Maintenance
    fun saveMaintenance(item: MaintenanceEntity) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertMaintenance(item)
            } else {
                repository.updateMaintenance(item)
            }
        }
    }

    fun deleteMaintenance(item: MaintenanceEntity) {
        viewModelScope.launch {
            repository.deleteMaintenance(item)
        }
    }

    fun logServiceCompleted(item: MaintenanceEntity, cost: Double) {
        viewModelScope.launch {
            repository.logServiceCompleted(item, cost)
        }
    }

    // Settings
    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.setSetting(key, value)
        }
    }
}

class LifeOSViewModelFactory(private val repository: LifeOSRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LifeOSViewModel::class.java)) {
            return LifeOSViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
