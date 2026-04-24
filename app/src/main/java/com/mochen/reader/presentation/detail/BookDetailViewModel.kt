package com.mochen.reader.presentation.detail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.Bookmark
import com.mochen.reader.domain.model.Chapter
import com.mochen.reader.domain.model.Note
import com.mochen.reader.domain.repository.BookRepository
import com.mochen.reader.domain.repository.BookmarkRepository
import com.mochen.reader.domain.repository.ChapterRepository
import com.mochen.reader.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val noteRepository: NoteRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBookDetail()
    }

    private fun loadBookDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load book
            launch {
                bookRepository.getBookById(bookId).collect { book ->
                    _uiState.update { it.copy(book = book) }
                }
            }

            // Load chapters
            launch {
                chapterRepository.getChaptersByBookId(bookId).collect { chapters ->
                    _uiState.update { it.copy(chapters = chapters) }
                }
            }

            // Load bookmarks
            launch {
                bookmarkRepository.getBookmarksByBookId(bookId).collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks) }
                }
            }

            // Load notes
            launch {
                noteRepository.getNotesByBookId(bookId).collect { notes ->
                    _uiState.update { it.copy(notes = notes) }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateCover(coverPath: String) {
        viewModelScope.launch {
            bookRepository.updateCover(bookId, coverPath)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val book = _uiState.value.book ?: return@launch
            bookRepository.updateFavorite(bookId, !book.isFavorite)
        }
    }

    fun updateGroup(groupId: Long) {
        viewModelScope.launch {
            bookRepository.updateGroup(bookId, groupId)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmarkId)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    fun exportNotes(): String {
        val notes = _uiState.value.notes
        val book = _uiState.value.book ?: return ""

        val sb = StringBuilder()
        sb.appendLine("# ${book.title} 笔记")
        sb.appendLine()
        notes.forEach { note ->
            sb.appendLine("## ${note.chapterTitle}")
            sb.appendLine()
            sb.appendLine("> ${note.highlightedText}")
            sb.appendLine()
            sb.appendLine(note.note)
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }

        return sb.toString()
    }
}
