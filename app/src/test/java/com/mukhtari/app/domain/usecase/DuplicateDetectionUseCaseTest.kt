package com.mukhtari.app.domain.usecase

import com.mukhtari.app.data.local.entity.PersonEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateDetectionUseCaseTest {

    private val normalizationUseCase = ArabicNormalizationUseCase()

    @Test
    fun testExactMatchScoring() = runBlocking {
        val person1 = PersonEntity(
            id = 1, publicCode = "P1", fullName = "احمد محمد علي", fatherName = "محمد",
            grandfatherName = "علي", surname = "الخفاجي", gender = "ذكر", birthDate = "1990-01-01",
            maritalStatus = "single", relationToHead = "self", familyId = 1, houseId = 1,
            workStatus = "working", employer = null, jobTitle = null, educationLevel = "bachelor",
            phone = "07700000000", phoneAlt = null, notes = null, createdAt = 0, updatedAt = 0, deletedAt = null, deletedReason = null
        )
        
        val duplicate = person1.copy(id = 2, publicCode = "P2")
        
        val useCase = DuplicateDetectionUseCase(normalizationUseCase)
        val score = useCase.calculateDuplicateScore(person1, duplicate)
        
        assertTrue(score >= 80) // High score for exact match
    }

    @Test
    fun testNoFalsePositives() = runBlocking {
        val person1 = PersonEntity(
            id = 1, publicCode = "P1", fullName = "احمد محمد علي", fatherName = "محمد",
            grandfatherName = "علي", surname = "الخفاجي", gender = "ذكر", birthDate = "1990-01-01",
            maritalStatus = "single", relationToHead = "self", familyId = 1, houseId = 1,
            workStatus = "working", employer = null, jobTitle = null, educationLevel = "bachelor",
            phone = "07700000000", phoneAlt = null, notes = null, createdAt = 0, updatedAt = 0, deletedAt = null, deletedReason = null
        )
        
        val person2 = person1.copy(id = 2, publicCode = "P2", fullName = "علي حسين محمود", phone = "07800000000", fatherName = "حسين", grandfatherName = "محمود", familyId = 2, houseId = 2)
        
        val useCase = DuplicateDetectionUseCase(normalizationUseCase)
        val score = useCase.calculateDuplicateScore(person1, person2)
        
        assertTrue("Expected low score for different people, actual: \$score", score < 50)
    }
}
