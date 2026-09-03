package com.mukhtari.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mukhtari.app.data.local.db.AppDatabase
import com.mukhtari.app.data.local.entity.PersonEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ResidencyRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: ResidencyRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        repository = ResidencyRepositoryImpl(db, db.residencyDao(), db.personDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testTransferPerson() = runBlocking {
        val person = PersonEntity(
            publicCode = "P01",
            fullName = "Ali",
            fatherName = "Ahmed",
            grandfatherName = null,
            surname = null,
            gender = "male",
            birthDate = null,
            maritalStatus = "single",
            relationToHead = null,
            familyId = null,
            houseId = 10L,
            workStatus = "student",
            employer = null,
            jobTitle = null,
            educationLevel = "university",
            phone = null,
            phoneAlt = null,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val personId = db.personDao().insertPerson(person)

        val newHouseId = 20L
        repository.transferPerson(
            personId = personId,
            newHouseId = newHouseId,
            newFamilyId = null,
            newStartDate = "2023-01-01",
            reason = "Moved"
        )

        val currentResidency = repository.getCurrentResidency(personId)
        assertNotNull(currentResidency)
        assertEquals(newHouseId, currentResidency?.houseId)
        assertEquals("2023-01-01", currentResidency?.startDate)

        val updatedPerson = db.personDao().getActivePersonById(personId)
        assertEquals(newHouseId, updatedPerson?.houseId)
    }
}
