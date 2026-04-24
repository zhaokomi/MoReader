package com.mochen.reader.domain.repository

import com.mochen.reader.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesByBookId(bookId: Long): Flow<List<Note>>
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesByChapter(bookId: Long, chapterIndex: Int): Flow<List<Note>>
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun deleteNotesByBookId(bookId: Long)
}
