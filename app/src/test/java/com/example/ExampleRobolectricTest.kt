package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.LifeOSDatabase
import com.example.data.model.AssetEntity
import com.example.data.model.EventEntity
import com.example.data.model.MaintenanceEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.LifeOSRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: LifeOSDatabase
    private lateinit var repository: LifeOSRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LifeOSDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LifeOSRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LifeOS", appName)
    }

    @Test
    fun `insert and retrieve task from Room database`() = runBlocking {
        val task = TaskEntity(
            title = "Organize week priorities",
            date = "2026-10-14",
            time = "09:00",
            priority = "High",
            isDone = false,
            cost = 0.0
        )
        db.taskDao().insertTask(task)
        val allTasks = db.taskDao().getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Organize week priorities", allTasks[0].title)
        assertEquals("High", allTasks[0].priority)
    }

    @Test
    fun `insert and retrieve event from Room database`() = runBlocking {
        val event = EventEntity(
            title = "Deep Focus Work",
            date = "2026-10-14",
            startTime = "10:00",
            endTime = "11:30",
            category = "Work",
            description = "Uninterrupted coding sprint"
        )
        db.eventDao().insertEvent(event)
        val allEvents = db.eventDao().getAllEvents().first()
        assertEquals(1, allEvents.size)
        assertEquals("Deep Focus Work", allEvents[0].title)
    }

    @Test
    fun `record subscription renewal creates expense and advances next due date`() = runBlocking {
        val sub = SubscriptionEntity(
            title = "Cloud Storage 2TB",
            amount = 3200.0,
            cycle = "monthly",
            nextDue = "2026-10-15"
        )
        val id = repository.insertSubscription(sub)
        val savedSub = sub.copy(id = id)

        repository.recordSubscriptionRenewal(savedSub)

        val updatedSub = repository.allSubscriptions.first().first { it.id == id }
        assertEquals("2026-11-15", updatedSub.nextDue)

        val expenses = repository.allExpenses.first()
        assertTrue(expenses.any { it.title == "Cloud Storage 2TB Renewal" && it.amount == 3200.0 })
    }

    @Test
    fun `log maintenance service records expense and updates next due date`() = runBlocking {
        val item = MaintenanceEntity(
            title = "AC Coil Deep Clean",
            intervalMonths = 4,
            estimatedCost = 4500.0
        )
        val id = repository.insertMaintenance(item)
        val savedItem = item.copy(id = id)

        repository.logServiceCompleted(savedItem, 4500.0)

        val updatedItem = repository.allMaintenance.first().first { it.id == id }
        assertTrue(updatedItem.lastService.isNotBlank())
        assertTrue(updatedItem.nextDue.isNotBlank())

        val expenses = repository.allExpenses.first()
        assertTrue(expenses.any { it.title == "Maintenance: AC Coil Deep Clean" && it.amount == 4500.0 })
    }
}
