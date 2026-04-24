package com.mochen.reader.data.repository

import com.mochen.reader.data.local.dao.NoteDao
import com.mochen.reader.data.local.entity.NoteEntity
import com.mochen.reader.domain.model.HighlightColor
import com.mochen.reader.domain.model.Note
import com.mochen.reader.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotesByBookId(bookId: Long): Flow<List<Note>> {
        return noteDao.getNotesByBookId(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNotesByChapter(bookId: Long, chapterIndex: Int): Flow<List<Note>> {
        return noteDao.getNotesByChapter(bookId, chapterIndex).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun deleteNotesByBookId(bookId: Long) {
        noteDao.deleteNotesByBookId(bookId)
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            bookId = bookId,
            chapterIndex = chapterIndex,
            chapterTitle = chapterTitle,
            startPosition = startPosition,
            endPosition = endPosition,
            highlightedText = highlightedText,
            note = note,
            highlightColor = HighlightColor.fromValue(highlightColor),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            bookId = bookId,
            chapterIndex = chapterIndex,
            chapterTitle = chapterTitle,
            startPosition = startPosition,
            endPosition = endPosition,
            highlightedText = highlightedText,
            note = note,
            highlightColor = highlightColor.value,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
