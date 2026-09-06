package com.mukhtari.app.ui.certificates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.entity.ResidencyCertificateEntity
import com.mukhtari.app.data.local.entity.TransactionEntity
import com.mukhtari.app.domain.repository.CertificateRepository
import com.mukhtari.app.domain.repository.FamilyRepository
import com.mukhtari.app.domain.repository.HouseRepository
import com.mukhtari.app.domain.repository.PersonRepository
import com.mukhtari.app.domain.repository.TransactionRepository
import com.mukhtari.app.domain.repository.AlleyRepository
import com.mukhtari.app.domain.repository.StreetRepository
import com.mukhtari.app.domain.repository.RegionRepository
import com.mukhtari.app.domain.usecase.PdfGeneratorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ResidencyCertificateViewModel(
    private val personRepository: PersonRepository,
    private val familyRepository: FamilyRepository,
    private val houseRepository: HouseRepository,
    private val alleyRepository: AlleyRepository,
    private val streetRepository: StreetRepository,
    private val regionRepository: RegionRepository,
    private val transactionRepository: TransactionRepository,
    private val certificateRepository: CertificateRepository,
    private val pdfGeneratorUseCase: PdfGeneratorUseCase
) : ViewModel() {

    private val _certificates = MutableStateFlow<List<ResidencyCertificateEntity>>(emptyList())
    val certificates: StateFlow<List<ResidencyCertificateEntity>> = _certificates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadCertificatesForPerson(personId: Long) {
        viewModelScope.launch {
            certificateRepository.getCertificatesForPerson(personId).collect {
                _certificates.value = it
            }
        }
    }

    fun issueCertificate(personId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val person = personRepository.getActivePersonById(personId) ?: return@launch

                var familyName = ""
                var address = ""

                if (person.familyId != null) {
                    val family = familyRepository.getFamilyById(person.familyId)
                    if (family != null) {
                        familyName = family.familyName ?: family.familyCode
                    }
                }

                if (person.houseId != null) {
                    val house = houseRepository.getHouseById(person.houseId)
                    if (house != null) {
                        val parts = mutableListOf<String>()

                        if (house.streetId != null) {
                            val street = streetRepository.getStreetById(house.streetId)
                            if (street != null) {
                                val region = regionRepository.getActiveRegionById(street.regionId)
                                if (region != null) {
                                    parts.add("محافظة ${region.governorate}")
                                    parts.add("قضاء ${region.district}")
                                    parts.add("منطقة ${region.name}")
                                    parts.add("محلة ${region.mahalla}")
                                }
                                parts.add("شارع ${street.name}")
                            }
                        }

                        if (house.alleyId != null) {
                            val alley = alleyRepository.getAlleyById(house.alleyId)
                            if (alley != null) {
                                parts.add("زقاق ${alley.name}")
                            }
                        }

                        parts.add("دار رقم ${house.houseNumber}")

                        address = parts.joinToString(" / ")
                    }
                }

                // 1. Create Transaction
                val timestamp = System.currentTimeMillis()
                val transactionCode = "CERT-${UUID.randomUUID().toString().take(8).uppercase()}"

                val transaction = TransactionEntity(
                    transactionCode = transactionCode,
                    transactionType = "residency_certificate",
                    personId = person.id,
                    familyId = person.familyId,
                    applicantNameSnapshot = person.fullName,
                    requestDate = timestamp.toString(),
                    subject = "إصدار تأييد سكن",
                    details = "إصدار تأييد سكن للمواطن ${person.fullName}",
                    status = "completed",
                    notes = null,
                    isDeleted = 0,
                    deletedAt = null,
                    deletedReason = null,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
                val transactionId = transactionRepository.saveTransaction(transaction)

                // 2. Generate PDF using UseCase
                val pdfPath = pdfGeneratorUseCase.generateResidencyCertificate(
                    snapshotName = person.fullName,
                    snapshotFamily = familyName,
                    snapshotAddress = address,
                    transactionCode = transactionCode,
                    issuedAt = timestamp
                )

                // 3. Create Certificate Entity
                val certificate = ResidencyCertificateEntity(
                    transactionId = transactionId,
                    personId = person.id,
                    snapshotName = person.fullName,
                    snapshotFamily = familyName,
                    snapshotHouse = address,
                    snapshotAddress = address,
                    pdfPath = pdfPath,
                    issuedAt = timestamp,
                    createdAt = timestamp
                )
                certificateRepository.saveCertificate(certificate)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
