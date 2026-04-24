package com.mochen.reader.domain.repository

import com.mochen.reader.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<Group>>
    suspend fun getGroupById(id: Long): Group?
    suspend fun insertGroup(group: Group): Long
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(id: Long)
}
