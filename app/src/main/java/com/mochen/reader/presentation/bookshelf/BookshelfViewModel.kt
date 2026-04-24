package com.mochen.reader.presentation.bookshelf

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.Group
import com.mochen.reader.domain.repository.BookRepository
import com.mochen.reader.domain.repository.ChapterRepository
import com.mochen.reader.domain.repository.GroupRepository
import com.mochen.reader.parser.BookParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BookshelfUiState(
    val books: List<Book> = emptyList(),
    val recentBooks: List<Book> = emptyList(),
    val groups: List<Group> = emptyList(),
    val selectedGroupId: Long? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isGridView: Boolean = true,
    val sortOrder: SortOrder = SortOrder.LAST_READ,
    val isSelectionMode: Boolean = false,
    val selectedBookIds: Set<Long> = emptySet(),
    val error: String? = null
)

enum class SortOrder(val displayName: String) {
    NAME("按书名"),
    IMPORT_TIME("按导入时间"),
    LAST_READ("按最近阅读"),
    PROGRESS("按阅读进度")
}

@HiltViewModel
class BookshelfViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val groupRepository: GroupRepository,
    private val bookParser: BookParser,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookshelfUiState())
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
        loadGroups()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                bookRepository.getAllBooks(),
                bookRepository.getRecentBooks(5),
                _uiState.map { it.searchQuery }.distinctUntilChanged(),
                _uiState.map { it.selectedGroupId }.distinctUntilChanged(),
                _uiState.map { it.sortOrder }.distinctUntilChanged()
            ) { allBooks, recentBooks, query, groupId, sortOrder ->
                var filteredBooks = if (groupId != null) {
                    allBooks.filter { it.groupId == groupId }
                } else {
                    allBooks
                }

                if (query.isNotBlank()) {
                    filteredBooks = filteredBooks.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.author.contains(query, ignoreCase = true)
                    }
                }

                filteredBooks = when (sortOrder) {
                    SortOrder.NAME -> filteredBooks.sortedBy { it.title }
                    SortOrder.IMPORT_TIME -> filteredBooks.sortedByDescending { it.importTime }
                    SortOrder.LAST_READ -> filteredBooks.sortedByDescending { it.lastReadTime }
                    SortOrder.PROGRESS -> filteredBooks.sortedByDescending { it.currentProgress }
                }

                BookshelfUiState(
                    books = filteredBooks,
                    recentBooks = recentBooks,
                    groups = _uiState.value.groups,
                    selectedGroupId = groupId,
                    searchQuery = query,
                    isLoading = false,
                    isGridView = _uiState.value.isGridView,
                    sortOrder = sortOrder,
                    isSelectionMode = _uiState.value.isSelectionMode,
                    selectedBookIds = _uiState.value.selectedBookIds
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
    }

    fun setSelectedGroup(groupId: Long?) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun toggleSelectionMode() {
        _uiState.update {
            it.copy(
                isSelectionMode = !it.isSelectionMode,
                selectedBookIds = if (it.isSelectionMode) emptySet() else it.selectedBookIds
            )
        }
    }

    fun toggleBookSelection(bookId: Long) {
        _uiState.update {
            val newSelected = if (bookId in it.selectedBookIds) {
                it.selectedBookIds - bookId
            } else {
                it.selectedBookIds + bookId
            }
            it.copy(selectedBookIds = newSelected)
        }
    }

    fun selectAll() {
        _uiState.update { it.copy(selectedBookIds = it.books.map { book -> book.id }.toSet()) }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedBookIds = emptySet()) }
    }

    fun deleteSelectedBooks() {
        viewModelScope.launch {
            _uiState.value.selectedBookIds.forEach { bookId ->
                bookRepository.deleteBook(bookId)
            }
            _uiState.update {
                it.copy(
                    isSelectionMode = false,
                    selectedBookIds = emptySet()
                )
            }
        }
    }

    fun moveSelectedBooksToGroup(groupId: Long) {
        viewModelScope.launch {
            _uiState.value.selectedBookIds.forEach { bookId ->
                bookRepository.updateGroup(bookId, groupId)
            }
            _uiState.update {
                it.copy(
                    isSelectionMode = false,
                    selectedBookIds = emptySet()
                )
            }
        }
    }

    fun importBook(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val book = bookParser.parseBook(context, uri)
                if (book != null) {
                    val existingBook = bookRepository.getBookByPath(book.filePath)
                    if (existingBook == null) {
                        val bookId = bookRepository.insertBook(book)
                        // Parse chapters
                        val chapters = bookParser.parseChapters(context, uri, bookId)
                        chapterRepository.insertChapters(chapters)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "导入失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun importBooksFromFolder(folderUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val children = context.contentResolver.query(
                    folderUri, null, null, null, null
                )
                children?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIndex)
                        if (name.endsWith(".txt", true) ||
                            name.endsWith(".epub", true) ||
                            name.endsWith(".mobi", true) ||
                            name.endsWith(".azw3", true) ||
                            name.endsWith(".pdf", true)
                        ) {
                            val childUri = Uri.withAppendedPath(folderUri, name)
                            importBook(childUri)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "扫描失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            val group = Group(name = name)
            groupRepository.insertGroup(group)
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
