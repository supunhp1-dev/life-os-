package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AssetDao
import com.example.data.dao.BillDao
import com.example.data.dao.EventDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.HabitDao
import com.example.data.dao.MaintenanceDao
import com.example.data.dao.NoteDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.SubscriptionDao
import com.example.data.dao.TaskDao
import com.example.data.dao.VaultDao
import com.example.data.model.AssetEntity
import com.example.data.model.BillEntity
import com.example.data.model.Converters
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.HabitEntity
import com.example.data.model.MaintenanceEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SettingsEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.VaultEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        EventEntity::class,
        TaskEntity::class,
        BillEntity::class,
        ExpenseEntity::class,
        VaultEntity::class,
        SettingsEntity::class,
        NoteEntity::class,
        HabitEntity::class,
        AssetEntity::class,
        SubscriptionEntity::class,
        MaintenanceEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeOSDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun billDao(): BillDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun vaultDao(): VaultDao
    abstract fun settingsDao(): SettingsDao
    abstract fun noteDao(): NoteDao
    abstract fun habitDao(): HabitDao
    abstract fun assetDao(): AssetDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        @Volatile
        private var INSTANCE: LifeOSDatabase? = null

        fun getDatabase(context: Context): LifeOSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeOSDatabase::class.java,
                    "lifeos_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { seedDatabase(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDatabase(database: LifeOSDatabase) {
            val today = LocalDate.now()
            val fmt = DateTimeFormatter.ISO_LOCAL_DATE
            val d = { offset: Long -> today.plusDays(offset).format(fmt) }

            // 1. Settings
            database.settingsDao().insertAll(
                listOf(
                    SettingsEntity("currency", "LKR"),
                    SettingsEntity("monthlyBudget", "100000"),
                    SettingsEntity("weekIntention", "A little progress, with room to breathe."),
                    SettingsEntity("workspaceName", "Personal Space"),
                    SettingsEntity("defaultCalendarView", "week"),
                    SettingsEntity("dailyStartHour", "7")
                )
            )

            // 2. Events for this week
            database.eventDao().insertAll(
                listOf(
                    EventEntity(
                        title = "Weekly Reset & Priorities",
                        date = d(0),
                        startTime = "08:30",
                        endTime = "09:15",
                        category = "Personal",
                        description = "Review the week ahead, select key priorities, and leave room to breathe.",
                        location = "Home Study",
                        plannedCost = 0.0,
                        tags = listOf("Personal", "Reset")
                    ),
                    EventEntity(
                        title = "Deep Focus Work Session",
                        date = d(0),
                        startTime = "10:00",
                        endTime = "11:30",
                        category = "Work",
                        description = "Single-tasking block. Phone in focus mode.",
                        location = "Desk",
                        plannedCost = 0.0,
                        tags = listOf("Work", "Focus")
                    ),
                    EventEntity(
                        title = "Healthy Lunch & Fresh Air",
                        date = d(0),
                        startTime = "12:30",
                        endTime = "13:30",
                        category = "Health",
                        description = "Step away from the screen for a nutritious meal.",
                        location = "Garden Cafe",
                        plannedCost = 1200.0,
                        tags = listOf("Health", "Personal")
                    ),
                    EventEntity(
                        title = "Grocery & Market Run",
                        date = d(1),
                        startTime = "16:30",
                        endTime = "17:30",
                        category = "Home",
                        description = "Fresh vegetables, fruit, tea, and pantry items.",
                        location = "Local Market",
                        plannedCost = 4500.0,
                        tags = listOf("Home", "Errands")
                    ),
                    EventEntity(
                        title = "Dental Health Checkup",
                        date = d(2),
                        startTime = "14:00",
                        endTime = "14:45",
                        category = "Health",
                        description = "Routine dental inspection and clean.",
                        location = "Dental Clinic",
                        plannedCost = 3500.0,
                        tags = listOf("Health")
                    ),
                    EventEntity(
                        title = "Learning & Skill Lab",
                        date = d(3),
                        startTime = "09:00",
                        endTime = "10:15",
                        category = "Study",
                        description = "Dedicated uninterrupted learning sprint.",
                        location = "Library",
                        plannedCost = 0.0,
                        tags = listOf("Study", "Growth")
                    ),
                    EventEntity(
                        title = "Weekend Coastal Road Trip",
                        date = d(5),
                        startTime = "08:00",
                        endTime = "16:00",
                        category = "Travel",
                        description = "Scenic coastal drive and explore beach market.",
                        location = "Southern Coast",
                        plannedCost = 5000.0,
                        tags = listOf("Travel", "Weekend")
                    ),
                    EventEntity(
                        title = "Family Sunday Gathering",
                        date = d(6),
                        startTime = "12:30",
                        endTime = "15:00",
                        category = "Personal",
                        description = "Relaxed afternoon lunch with family.",
                        location = "Grandma's House",
                        plannedCost = 6500.0,
                        tags = listOf("Family", "Weekend")
                    )
                )
            )

            // 3. Actionable Tasks
            database.taskDao().insertAll(
                listOf(
                    TaskEntity(
                        title = "Review top 3 intentions for the week",
                        date = d(0),
                        time = "08:30",
                        priority = "High",
                        isDone = true,
                        cost = 0.0,
                        tags = listOf("Personal")
                    ),
                    TaskEntity(
                        title = "Send monthly project summary report",
                        date = d(0),
                        time = "14:00",
                        priority = "High",
                        isDone = false,
                        cost = 0.0,
                        tags = listOf("Work")
                    ),
                    TaskEntity(
                        title = "Pick up weekly grocery staples",
                        date = d(1),
                        time = "16:30",
                        priority = "Medium",
                        isDone = false,
                        cost = 4500.0,
                        tags = listOf("Home", "Errands")
                    ),
                    TaskEntity(
                        title = "File appliance warranty in Vault",
                        date = d(2),
                        time = "18:00",
                        priority = "Low",
                        isDone = false,
                        cost = 0.0,
                        tags = listOf("Home")
                    ),
                    TaskEntity(
                        title = "Review upcoming electricity bill payment",
                        date = d(3),
                        time = "10:30",
                        priority = "Medium",
                        isDone = false,
                        cost = 0.0,
                        tags = listOf("Finance")
                    ),
                    TaskEntity(
                        title = "Call parents and plan weekend visit",
                        date = d(4),
                        time = "17:00",
                        priority = "Low",
                        isDone = false,
                        cost = 0.0,
                        tags = listOf("Family")
                    )
                )
            )

            // 4. Bills & Due Dates
            database.billDao().insertAll(
                listOf(
                    BillEntity(
                        title = "Electricity & Utilities",
                        amount = 6800.0,
                        paidAmount = 0.0,
                        due = d(4),
                        isPaid = false,
                        repeat = "monthly",
                        category = "Bills",
                        tags = listOf("Bills", "Home")
                    ),
                    BillEntity(
                        title = "High-Speed Home Fiber Internet",
                        amount = 3900.0,
                        paidAmount = 0.0,
                        due = d(7),
                        isPaid = false,
                        repeat = "monthly",
                        category = "Bills",
                        tags = listOf("Bills", "Work")
                    ),
                    BillEntity(
                        title = "Mobile Unlimited Plan",
                        amount = 1800.0,
                        paidAmount = 1800.0,
                        due = d(14),
                        isPaid = true,
                        repeat = "monthly",
                        category = "Bills",
                        tags = listOf("Bills", "Mobile")
                    )
                )
            )

            // 5. Recent Expenses
            database.expenseDao().insertAll(
                listOf(
                    ExpenseEntity(
                        title = "Weekly Farm Fresh Groceries",
                        date = d(0),
                        amount = 2350.0,
                        category = "Home",
                        notes = "Fresh vegetables, local fruit, pantry essentials"
                    ),
                    ExpenseEntity(
                        title = "Transit & Train Pass",
                        date = d(-1),
                        amount = 620.0,
                        category = "Travel",
                        notes = "Weekly commute reload"
                    ),
                    ExpenseEntity(
                        title = "Organic Coffee & Work Snack",
                        date = d(-2),
                        amount = 950.0,
                        category = "Personal",
                        notes = "Afternoon focus fuel"
                    ),
                    ExpenseEntity(
                        title = "Mobile Unlimited Plan",
                        date = d(-3),
                        amount = 1800.0,
                        category = "Bills",
                        notes = "Paid via bill tracker"
                    )
                )
            )

            // 6. Life Records (Vault)
            database.vaultDao().insertAll(
                listOf(
                    VaultEntity(
                        title = "MacBook Pro M-Series Warranty",
                        type = "Warranty",
                        purchaseDate = d(-180),
                        expiryDate = d(25),
                        reminderDays = 30,
                        notes = "AppleCare+ active. Check renewal options before expiry.",
                        tags = listOf("Work", "Tech")
                    ),
                    VaultEntity(
                        title = "Comprehensive Vehicle Insurance",
                        type = "Insurance",
                        purchaseDate = d(-300),
                        expiryDate = d(50),
                        reminderDays = 30,
                        notes = "Policy #INS-9082. Compare renewal rates 2 weeks ahead.",
                        tags = listOf("Vehicle", "Important")
                    ),
                    VaultEntity(
                        title = "International Passport Validity",
                        type = "Document",
                        purchaseDate = d(-800),
                        expiryDate = d(450),
                        reminderDays = 60,
                        notes = "Ensure at least 6 months validity before booking international travel.",
                        tags = listOf("Travel", "Identity")
                    ),
                    VaultEntity(
                        title = "Home Inverter AC 5-Year Compressor Warranty",
                        type = "Warranty",
                        purchaseDate = d(-400),
                        expiryDate = d(900),
                        reminderDays = 30,
                        notes = "Includes annual free service filter check.",
                        tags = listOf("Home", "Appliance")
                    )
                )
            )

            // 7. Notes
            database.noteDao().insertAll(
                listOf(
                    NoteEntity(
                        title = "Ideas to come back to",
                        date = d(0),
                        body = "Keep a running list here. Nothing needs to become a task just because you thought of it.",
                        tags = listOf("Ideas", "Personal")
                    ),
                    NoteEntity(
                        title = "Weekend recipe experiment",
                        date = d(-1),
                        body = "Slow-baked artisanal sourdough with whipped herb butter and honey roasted baby carrots.",
                        tags = listOf("Food", "Home")
                    )
                )
            )

            // 8. Habits
            database.habitDao().insertAll(
                listOf(
                    HabitEntity(
                        title = "Morning hydration & gentle stretch",
                        frequency = "daily",
                        streak = 7,
                        lastLoggedDate = d(0),
                        tags = listOf("Health")
                    ),
                    HabitEntity(
                        title = "Uninterrupted deep work block",
                        frequency = "weekdays",
                        streak = 4,
                        lastLoggedDate = d(0),
                        tags = listOf("Work")
                    ),
                    HabitEntity(
                        title = "Evening walk & tech-free unwind",
                        frequency = "daily",
                        streak = 12,
                        lastLoggedDate = d(-1),
                        tags = listOf("Personal")
                    )
                )
            )

            // 9. Assets
            database.assetDao().insertAll(
                listOf(
                    AssetEntity(
                        title = "MacBook Pro M3 Max",
                        type = "Electronics",
                        purchaseDate = d(-180),
                        price = 680000.0,
                        warrantyEnd = d(540),
                        serial = "C02G9988H1",
                        condition = "New",
                        notes = "Primary developer machine. AppleCare active.",
                        tags = listOf("Tech", "Work")
                    ),
                    AssetEntity(
                        title = "Daikin Inverter AC 18000BTU",
                        type = "Home appliance",
                        purchaseDate = d(-365),
                        price = 195000.0,
                        warrantyEnd = d(1100),
                        serial = "DK-INV-992",
                        condition = "Good",
                        notes = "Installed in master bedroom. Needs filter clean every 4 months.",
                        tags = listOf("Home", "Appliance")
                    ),
                    AssetEntity(
                        title = "Honda Grace Hybrid",
                        type = "Vehicle",
                        purchaseDate = d(-720),
                        price = 8200000.0,
                        warrantyEnd = d(360),
                        serial = "CAB-4482",
                        condition = "Good",
                        notes = "Full insurance with Ceylinco VIP.",
                        tags = listOf("Vehicle")
                    )
                )
            )

            // 10. Subscriptions
            database.subscriptionDao().insertAll(
                listOf(
                    SubscriptionEntity(
                        title = "Cloud Storage 2TB",
                        provider = "Google One",
                        amount = 3200.0,
                        cycle = "monthly",
                        nextDue = d(18),
                        status = "active",
                        cancelInfo = "Cancel via Google Play Store subscriptions",
                        notes = "Shared with family storage group",
                        tags = listOf("Tech", "Cloud")
                    ),
                    SubscriptionEntity(
                        title = "Spotify Family Plan",
                        provider = "Spotify",
                        amount = 1490.0,
                        cycle = "monthly",
                        nextDue = d(9),
                        status = "active",
                        cancelInfo = "Account settings on Spotify web",
                        notes = "Autopay via Visa card",
                        tags = listOf("Entertainment")
                    ),
                    SubscriptionEntity(
                        title = "GitHub Copilot & Pro",
                        provider = "GitHub",
                        amount = 4500.0,
                        cycle = "monthly",
                        nextDue = d(25),
                        status = "active",
                        notes = "Developer tooling subscription",
                        tags = listOf("Work", "Tech")
                    )
                )
            )

            // 11. Maintenance
            database.maintenanceDao().insertAll(
                listOf(
                    MaintenanceEntity(
                        title = "Honda Hybrid 10,000km Periodic Service",
                        type = "Vehicle service",
                        lastService = d(-150),
                        nextDue = d(35),
                        intervalMonths = 6,
                        estimatedCost = 28000.0,
                        provider = "Stafford Motors",
                        notes = "Engine oil, hybrid filter, brake fluid inspection.",
                        tags = listOf("Vehicle", "Maintenance")
                    ),
                    MaintenanceEntity(
                        title = "Bedroom AC Filter & Coil Deep Clean",
                        type = "Routine service",
                        lastService = d(-90),
                        nextDue = d(14),
                        intervalMonths = 4,
                        estimatedCost = 4500.0,
                        provider = "CoolAir Tech",
                        notes = "Chemical coil rinse and antibacterial filter check.",
                        tags = listOf("Home", "Maintenance")
                    )
                )
            )
        }
    }
}
