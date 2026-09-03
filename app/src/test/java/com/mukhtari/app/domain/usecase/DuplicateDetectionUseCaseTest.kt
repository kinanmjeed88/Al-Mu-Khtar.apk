package com.mukhtari.app.domain.usecase

import com.mukhtari.app.data.local.entity.PersonEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DuplicateDetectionUseCaseTest {

    private lateinit var arabicNormalizationUseCase: ArabicNormalizationUseCase
    private lateinit var duplicateDetectionUseCase: DuplicateDetectionUseCase

    @Before
    fun setup() {
        arabicNormalizationUseCase = ArabicNormalizationUseCase()
        duplicateDetectionUseCase = DuplicateDetectionUseCase(arabicNormalizationUseCase)
    }

    private fun createPerson(
        fullName: String,
        fatherName: String? = null,
        grandfatherName: String? = null,
        birthDate: String? = null,
        phone: String? = null,
        houseId: Long? = null,
        familyId: Long? = null
    ): PersonEntity {
        return PersonEntity(
            publicCode = "P",
            fullName = fullName,
            fatherName = fatherName,
            grandfatherName = grandfatherName,
            surname = null,
            gender = "male",
            birthDate = birthDate,
            maritalStatus = "single",
            relationToHead = null,
            familyId = familyId,
            houseId = houseId,
            workStatus = "student",
            employer = null,
            jobTitle = null,
            educationLevel = "university",
            phone = phone,
            phoneAlt = null,
            notes = null,
            isDeleted = 0,
            deletedAt = null,
            deletedReason = null,
            createdAt = 0L,
            updatedAt = 0L
        )
    }

    @Test
    fun testExactMatchScore() {
        val p1 = createPerson("أحمد", "محمد", "علي", "1990-01-01", "07701234567", 1L, 1L)
        val p2 = createPerson("احمد", "مُحَمَّد", "على", "1990-01-01", "07701234567", 1L, 1L)
        
        val score = duplicateDetectionUseCase.calculateDuplicateScore(p1, p2)
        assertEquals(130, score)
    }

    @Test
    fun testPartialMatchScore() {
        val p1 = createPerson("احمد سعيد", "محمد", "علي", "1990-01-01", null, null, null)
        val p2 = createPerson("احمد", "محمد", "حسن", "1992-01-01", null, null, null)
        
        val score = duplicateDetectionUseCase.calculateDuplicateScore(p1, p2)
        assertEquals(30, score)
    }
}
