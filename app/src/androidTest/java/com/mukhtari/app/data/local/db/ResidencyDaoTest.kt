package com.mukhtari.app.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.entity.ResidencyEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ResidencyDaoTest {
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
    fun testCurrentResidencyLogic() = runBlocking {
        val personId = 1L
        val pastResidency = ResidencyEntity(
            personId = personId,
            familyId = null,
            houseId = null,
            startDate = "2020-01-01",
            endDate = "2021-01-01",
            residencyType = "resident",
            verificationStatus = "verified",
            reason = null,
            previousAddressText = null,
            notes = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val currentResidency = ResidencyEntity(
            personId = personId,
            familyId = null,
            houseId = null,
            startDate = "2021-01-02",
            endDate = null,
            residencyType = "resident",
            verificationStatus = "verified",
            reason = null,
            previousAddressText = null,
            notes = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        db.residencyDao().insertResidency(pastResidency)
        db.residencyDao().insertResidency(currentResidency)

        val active = db.residencyDao().getCurrentResidencyForPerson(personId)
        
        assertEquals("2021-01-02", active?.startDate)
        assertNull(active?.endDate)
    }
}
