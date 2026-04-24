package com.mochen.reader.data.repository

import com.mochen.reader.data.local.dao.ChapterDao
import com.mochen.reader.data.local.entity.ChapterEntity
import com.mochen.reader.domain.model.Chapter
import com.mochen.reader.domain.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterRepositoryImpl @Inject constructor(
    private val chapterDao: ChapterDao
) : ChapterRepository {

    override fun getChaptersByBookId(bookId: Long): Flow<List<Chapter>> {
        return chapterDao.getChaptersByBookId(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChaptersByBookIdSync(bookId: Long): List<Chapter> {
        return chapterDao.getChaptersByBookIdSync(bookId).map { it.toDomain() }
    }

    override suspend fun getChapter(bookId: Long, chapterIndex: Int): Chapter? {
        return chapterDao.getChapter(bookId, chapterIndex)?.toDomain()
    }

    override suspend fun insertChapters(chapters: List<Chapter>) {
        chapterDao.insertChapters(chapters.map { it.toEntity() })
    }

    override suspend fun deleteChaptersByBookId(bookId: Long) {
        chapterDao.deleteChaptersByBookId(bookId)
    }

    private fun ChapterEntity.toDomain(): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            chapterIndex = chapterIndex,
            title = title,
            volumeIndex = volumeIndex,
            volumeTitle = volumeTitle,
            startPosition = startPosition,
            endPosition = endPosition,
            wordCount = wordCount
        )
    }

    private fun Chapter.toEntity(): ChapterEntity {
        return ChapterEntity(
            id = id,
            bookId = bookId,
            chapterIndex = chapterIndex,
            title = title,
            volumeIndex = volumeIndex,
            volumeTitle = volumeTitle,
            startPosition = startPosition,
            endPosition = endPosition,
            wordCount = wordCount
        )
    }
}
