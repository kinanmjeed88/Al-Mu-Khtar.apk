package com.mukhtari.app.domain.repository

import com.mukhtari.app.data.local.dao.ActivityLogDao
import com.mukhtari.app.data.local.entity.ActivityLogEntity
import com.mukhtari.app.data.repository.ActivityLogRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MockActivityLogDao : ActivityLogDao {
    val logs = mutableListOf<ActivityLogEntity>()

    override suspend fun insertLog(log: ActivityLogEntity): Long {
        logs.add(log)
        return logs.size.toLong()
    }

    override fun getAllActivityLogs(): Flow<List<ActivityLogEntity>> {
        return flowOf(logs.sortedByDescending { it.timestamp })
    }
}

class ActivityLogRepositoryTest {

    @Test
    fun testLogActivity() = runBlocking {
        val mockDao = MockActivityLogDao()
        val repository = ActivityLogRepositoryImpl(mockDao)

        repository.logActivity(
            actionType = "CREATE",
            entityType = "person",
            entityId = 123L,
            description = "Created person test",
            oldValues = null,
            newValues = "{ \"id\": 123 }"
        )

        assertEquals(1, mockDao.logs.size)
        val log = mockDao.logs.first()
        assertEquals("CREATE", log.actionType)
        assertEquals("person", log.entityType)
        assertEquals(123L, log.entityId)
    }
}
