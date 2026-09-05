package com.mukhtari.app.ui.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeletedItem(
    val id: Long,
    val type: String,
    val displayName: String,
    val deletedAt: Long?
)

class RecycleBinViewModel(
    private val personRepository: PersonRepository,
    private val houseRepository: HouseRepository,
    private val familyRepository: FamilyRepository,
    private val regionRepository: RegionRepository,
    private val streetRepository: StreetRepository,
    private val alleyRepository: AlleyRepository,
    private val incomingLetterRepository: IncomingLetterRepository,
    private val outgoingLetterRepository: OutgoingLetterRepository
) : ViewModel() {

    private val _deletedItems = MutableStateFlow<List<DeletedItem>>(emptyList())
    val deletedItems: StateFlow<List<DeletedItem>> = _deletedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDeletedItems()
    }

    fun loadDeletedItems() {
        viewModelScope.launch {
            _isLoading.value = true
            val items = mutableListOf<DeletedItem>()

            personRepository.getDeletedPersons().forEach {
                items.add(DeletedItem(it.id, "person", "شخص: ${it.fullName}", it.deletedAt))
            }
            houseRepository.getDeletedHouses().forEach {
                items.add(DeletedItem(it.id, "house", "دار: ${it.houseNumber}", it.deletedAt))
            }
            familyRepository.getDeletedFamilies().forEach {
                items.add(DeletedItem(it.id, "family", "عائلة: ${it.familyCode}", it.deletedAt))
            }
            regionRepository.getDeletedRegions().forEach {
                items.add(DeletedItem(it.id, "region", "منطقة: ${it.name}", it.deletedAt))
            }
            streetRepository.getDeletedStreets().forEach {
                items.add(DeletedItem(it.id, "street", "شارع: ${it.name}", it.deletedAt))
            }
            alleyRepository.getDeletedAlleys().forEach {
                items.add(DeletedItem(it.id, "alley", "زقاق: ${it.name}", it.deletedAt))
            }
            incomingLetterRepository.getDeletedLetters().forEach {
                items.add(DeletedItem(it.id, "incoming_letter", "وارد: ${it.letterNumber}", null))
            }
            outgoingLetterRepository.getDeletedLetters().forEach {
                items.add(DeletedItem(it.id, "outgoing_letter", "صادر: ${it.letterNumber}", null))
            }

            _deletedItems.value = items.sortedByDescending { it.deletedAt ?: 0L }
            _isLoading.value = false
        }
    }

    fun restoreItem(item: DeletedItem) {
        viewModelScope.launch {
            when (item.type) {
                "person" -> personRepository.restorePerson(item.id)
                "house" -> houseRepository.restoreHouse(item.id)
                "family" -> familyRepository.restoreFamily(item.id)
                "region" -> regionRepository.restoreRegion(item.id)
                "street" -> streetRepository.restoreStreet(item.id)
                "alley" -> alleyRepository.restoreAlley(item.id)
                "incoming_letter" -> incomingLetterRepository.restoreLetter(item.id)
                "outgoing_letter" -> outgoingLetterRepository.restoreLetter(item.id)
            }
            loadDeletedItems()
        }
    }

    fun hardDeleteItem(item: DeletedItem) {
        viewModelScope.launch {
            when (item.type) {
                "person" -> personRepository.hardDeletePerson(item.id)
                "house" -> houseRepository.hardDeleteHouse(item.id)
                "family" -> familyRepository.hardDeleteFamily(item.id)
                "region" -> regionRepository.hardDeleteRegion(item.id)
                "street" -> streetRepository.hardDeleteStreet(item.id)
                "alley" -> alleyRepository.hardDeleteAlley(item.id)
                "incoming_letter" -> incomingLetterRepository.hardDeleteLetter(item.id)
                "outgoing_letter" -> outgoingLetterRepository.hardDeleteLetter(item.id)
            }
            loadDeletedItems()
        }
    }
}
