package com.mochen.reader.presentation.reader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.Chapter
import com.mochen.reader.domain.repository.BookRepository
import com.mochen.reader.domain.repository.ChapterRepository
import com.mochen.reader.parser.BookParser
import com.mochen.reader.parser.TxtParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SearchUiState(
    val searchQuery: String = "",
    val results: List<SearchResultDisplay> = emptyList(),
    val isLoading: Boolean = false,
    val currentResult: SearchResultDisplay? = null
)

data class SearchResultDisplay(
    val chapterIndex: Int,
    val chapterTitle: String,
    val context: String,
    val position: Int
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val bookParser: BookParser,
    private val txtParser: TxtParser,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var book: Book? = null
    private var chapters: List<Chapter> = emptyList()
    private var fullContent: String = ""

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            bookRepository.getBookById(bookId).first()?.let { b ->
                book = b
                chapters = chapterRepository.getChaptersByBookIdSync(bookId)

                // Load full content for searching
                val uri = Uri.parse(b.filePath)
                fullContent = txtParser.readFullContent(context, uri)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length >= 2) {
            performSearch(query)
        } else {
            _uiState.update { it.copy(results = emptyList()) }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val results = withContext(Dispatchers.Default) {
                val searchResults = mutableListOf<SearchResultDisplay>()
                val lines = fullContent.lines()

                lines.forEachIndexed { index, line ->
                    if (line.contains(query, ignoreCase = true)) {
                        val chapter = findChapterForLine(index)
                        val contextStart = maxOf(0, line.indexOf(query, ignoreCase = true) - 20)
                        val contextEnd = minOf(line.length, line.indexOf(query, ignoreCase = true) + query.length + 20)
                        val context = line.substring(contextStart, contextEnd).let {
                            if (contextStart > 0) "...$it" else it
                        }.let {
                            if (contextEnd < line.length) "$it..." else it
                        }

                        searchResults.add(
                            SearchResultDisplay(
                                chapterIndex = chapter?.chapterIndex ?: 0,
                                chapterTitle = chapter?.title ?: "",
                                context = context,
                                position = index
                            )
                        )
                    }
                }
                searchResults.take(100) // Limit results
            }

            _uiState.update { it.copy(results = results, isLoading = false) }
        }
    }

    private fun findChapterForLine(lineIndex: Int): Chapter? {
        return chapters.findLast { it.startPosition <= lineIndex }
    }

    fun setCurrentResult(result: SearchResultDisplay) {
        _uiState.update { it.copy(currentResult = result) }
    }
}
