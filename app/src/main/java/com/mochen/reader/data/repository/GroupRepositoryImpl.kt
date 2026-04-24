package com.mochen.reader.data.repository

import com.mochen.reader.data.local.dao.GroupDao
import com.mochen.reader.data.local.entity.GroupEntity
import com.mochen.reader.domain.model.Group
import com.mochen.reader.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao
) : GroupRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGroupById(id: Long): Group? {
        return groupDao.getGroupById(id)?.toDomain()
    }

    override suspend fun insertGroup(group: Group): Long {
        return groupDao.insertGroup(group.toEntity())
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.updateGroup(group.toEntity())
    }

    override suspend fun deleteGroup(id: Long) {
        groupDao.deleteGroupById(id)
    }

    private fun GroupEntity.toDomain(): Group {
        return Group(
            id = id,
            name = name,
            coverColor = coverColor,
            sortOrder = sortOrder,
            createdAt = createdAt
        )
    }

    private fun Group.toEntity(): GroupEntity {
        return GroupEntity(
            id = id,
            name = name,
            coverColor = coverColor,
            sortOrder = sortOrder,
            createdAt = createdAt
        )
    }
}
