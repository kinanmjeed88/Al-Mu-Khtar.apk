package com.mukhtari.app.domain.usecase

import com.mukhtari.app.data.local.entity.PersonEntity

class DuplicateDetectionUseCase(
    private val arabicNormalizationUseCase: ArabicNormalizationUseCase
) {

    fun calculateDuplicateScore(target: PersonEntity, candidate: PersonEntity): Int {
        var score = 0
        
        val normalizedTargetName = arabicNormalizationUseCase(target.fullName)
        val normalizedCandidateName = arabicNormalizationUseCase(candidate.fullName)
        
        if (normalizedTargetName == normalizedCandidateName) {
            score += 30
        } else if (normalizedTargetName.contains(normalizedCandidateName) || normalizedCandidateName.contains(normalizedTargetName)) {
            score += 10
        }

        if (!target.fatherName.isNullOrEmpty() && !candidate.fatherName.isNullOrEmpty()) {
            val normalizedTargetFather = arabicNormalizationUseCase(target.fatherName)
            val normalizedCandidateFather = arabicNormalizationUseCase(candidate.fatherName)
            if (normalizedTargetFather == normalizedCandidateFather) score += 20
        }

        if (!target.grandfatherName.isNullOrEmpty() && !candidate.grandfatherName.isNullOrEmpty()) {
            val normalizedTargetGrand = arabicNormalizationUseCase(target.grandfatherName)
            val normalizedCandidateGrand = arabicNormalizationUseCase(candidate.grandfatherName)
            if (normalizedTargetGrand == normalizedCandidateGrand) score += 15
        }

        if (!target.birthDate.isNullOrEmpty() && target.birthDate == candidate.birthDate) {
            score += 20
        }

        if (!target.phone.isNullOrEmpty() && target.phone == candidate.phone) {
            score += 20
        }
        
        if (target.houseId != null && target.houseId == candidate.houseId) {
            score += 15
        }
        
        if (target.familyId != null && target.familyId == candidate.familyId) {
            score += 10
        }

        return score
    }
}
