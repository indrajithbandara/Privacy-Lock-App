package com.example

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.LockedApp
import com.example.data.TimelineEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryBehaviorTest {
    @Test fun refreshPreservesProtectionAndPinning() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val app = LockedApp("fixture.app", "Old", "Other", true, true, true, "android")
            db.lockedAppDao().insertApp(app)
            db.lockedAppDao().insertNewApp(app.copy(isLocked = false, isPinned = false))
            db.lockedAppDao().updateAppMetadata(app.packageName, "Updated", "Other", "android")
            val refreshed = checkNotNull(db.lockedAppDao().getApp(app.packageName))
            assertTrue(refreshed.isLocked)
            assertTrue(refreshed.isPinned)
            assertTrue(refreshed.isFavorite)
            assertEquals("Updated", refreshed.name)
        } finally { db.close() }
    }

    @Test fun retentionKeepsNewestRecords() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            for (i in 1..5) db.timelineEventDao().insertEvent(TimelineEvent(id = i, timestamp = i.toLong(), type = "AUTH_FAILURE", description = "Fixture"))
            db.timelineEventDao().retainNewestEvents(2)
            assertEquals(listOf(4, 5), db.timelineEventDao().getAllEventsDirect().map { it.id })
        } finally { db.close() }
    }
}
