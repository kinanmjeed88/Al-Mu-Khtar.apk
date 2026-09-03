package com.mukhtari.app.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.entity.RegionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeAndReadRegion() = runBlocking {
        val region = RegionEntity(
            publicCode = "REG-01",
            governorate = "Baghdad",
            district = "Karkh",
            subDistrict = "Mansour",
            mahalla = "601",
            name = "Al-Mansour",
            description = null,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.regionDao().insertRegion(region)
        val activeRegions = db.regionDao().getActiveRegions()
        assertEquals(1, activeRegions.size)
        assertEquals("Al-Mansour", activeRegions[0].name)
    }
}
