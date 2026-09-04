package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.entity.PersonEntity

interface PersonRepository {
    suspend fun getActivePersons(): List<PersonEntity>
    suspend fun getActivePersonById(id: Long): PersonEntity?
    suspend fun savePerson(person: PersonEntity): Long
    suspend fun softDeletePerson(id: Long)
    suspend fun searchPersons(query: String): List<PersonEntity>
}
