package com.mochen.reader.presentation.reader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochen.reader.data.datastore.SettingsDataStore
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.Bookmark
import com.mochen.reader.domain.model.BookmarkColor
import com.mochen.reader.domain.model.Chapter
import com.mochen.reader.domain.model.HighlightColor
import com.mochen.reader.domain.model.Note
import com.mochen.reader.domain.repository.BookRepository
import com.mochen.reader.domain.repository.BookmarkRepository
import com.mochen.reader.domain.repository.ChapterRepository
import com.mochen.reader.domain.repository.NoteRepository
import com.mochen.reader.parser.BookParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val currentChapter: Chapter? = null,
    val currentChapterIndex: Int = 0,
    val chapterContent: String = "",
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = false,
    val showSettingsPanel: Boolean = false,
    val showChapterList: Boolean = false,
    val selectedTab: Int = 0,
    val bookmarks: List<Bookmark> = emptyList(),
    val notes: List<Note> = emptyList(),
    val isFullScreen: Boolean = true,
    val error: String? = null,
    // Reading settings
    val fontSize: Int = 16,
    val lineSpacing: Float = 1.5f,
    val pageMargin: Int = 16,
    val pageMode: PageMode = PageMode.SIMULATION,
    val readerTheme: ReaderThemeType = ReaderThemeType.WHITE,
    val brightness: Float = 0.5f,
    val brightnessFollowSystem: Boolean = true,
    val keepScreenOn: Boolean = true,
    val textSelection: TextSelection? = null
)

data class TextSelection(
    val start: Int,
    val end: Int,
    val text: String
)

enum class PageMode(val displayName: String) {
    SIMULATION("仿真翻页"),
    COVER("覆盖"),
    FADE("淡入淡出"),
    NONE("无动画"),
    VERTICAL("上下滚动")
}

enum class ReaderThemeType(val displayName: String) {
    WHITE("白色"),
    CREAM("米黄"),
    GREEN("护眼绿"),
    GRAY("灰色"),
    BLACK("纯黑")
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val noteRepository: NoteRepository,
    private val settingsDataStore: SettingsDataStore,
    private val bookParser: BookParser,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var startTime: Long = 0L
    private var readingStartPosition: Int = 0

    init {
        loadBook()
        loadSettings()
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load book info
            bookRepository.getBookById(bookId).collect { book ->
                _uiState.update { it.copy(book = book) }

                if (book != null) {
                    // Load chapters
                    val chapters = chapterRepository.getChaptersByBookIdSync(bookId)
                    _uiState.update { state ->
                        state.copy(
                            chapters = chapters,
                            currentChapterIndex = book.lastReadChapter
                        )
                    }

                    // Load chapter content
                    loadChapterContent(book.lastReadChapter)
                }
            }
        }

        // Load bookmarks and notes
        viewModelScope.launch {
            bookmarkRepository.getBookmarksByBookId(bookId).collect { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
        }

        viewModelScope.launch {
            noteRepository.getNotesByBookId(bookId).collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.fontSize.collect { fontSize ->
                _uiState.update { it.copy(fontSize = fontSize) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.lineSpacing.collect { lineSpacing ->
                _uiState.update { it.copy(lineSpacing = lineSpacing) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.pageMargin.collect { margin ->
                _uiState.update { it.copy(pageMargin = margin) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.pageMode.collect { mode ->
                _uiState.update { it.copy(pageMode = PageMode.entries.find { m -> m.name.lowercase() == mode } ?: PageMode.SIMULATION) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.readerTheme.collect { theme ->
                _uiState.update { it.copy(readerTheme = ReaderThemeType.entries.find { t -> t.name.lowercase() == theme } ?: ReaderThemeType.WHITE) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.brightness.collect { brightness ->
                _uiState.update { it.copy(brightness = brightness) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.brightnessFollowSystem.collect { follow ->
                _uiState.update { it.copy(brightnessFollowSystem = follow) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.keepScreenOn.collect { keepOn ->
                _uiState.update { it.copy(keepScreenOn = keepOn) }
            }
        }
    }

    private fun loadChapterContent(chapterIndex: Int) {
        viewModelScope.launch {
            val book = _uiState.value.book ?: return@launch
            val chapters = _uiState.value.chapters
            if (chapterIndex < chapters.size) {
                val chapter = chapters[chapterIndex]
                val uri = Uri.parse(book.filePath)
                val content = bookParser.readChapterContent(context, uri, chapter)

                // Paginate content
                val pages = paginateContent(content, _uiState.value.fontSize)

                _uiState.update {
                    it.copy(
                        currentChapter = chapter,
                        currentChapterIndex = chapterIndex,
                        chapterContent = content,
                        pages = pages,
                        currentPage = 0,
                        totalPages = pages.size,
                        isLoading = false
                    )
                }

                startTime = System.currentTimeMillis()
                readingStartPosition = chapterIndex
            }
        }
    }

    private fun paginateContent(content: String, fontSize: Int): List<String> {
        // Simple pagination - split by paragraphs
        val paragraphs = content.split("\n\n", "\n")
        val pages = mutableListOf<String>()
        var currentPage = StringBuilder()
        val charsPerPage = 500 / (fontSize / 14) // Adjust based on font size

        for (paragraph in paragraphs) {
            if (currentPage.length + paragraph.length > charsPerPage && currentPage.isNotEmpty()) {
                pages.add(currentPage.toString().trim())
                currentPage = StringBuilder()
            }
            currentPage.append(paragraph).append("\n\n")
        }

        if (currentPage.isNotEmpty()) {
            pages.add(currentPage.toString().trim())
        }

        return pages.ifEmpty { listOf(content) }
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage < state.totalPages - 1) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        } else if (state.currentChapterIndex < state.chapters.size - 1) {
            // Go to next chapter
            loadChapterContent(state.currentChapterIndex + 1)
        }
        saveReadingProgress()
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
        } else if (_uiState.value.currentChapterIndex > 0) {
            // Go to previous chapter
            val prevChapterIndex = _uiState.value.currentChapterIndex - 1
            loadChapterContent(prevChapterIndex)
            // Go to last page of previous chapter
            viewModelScope.launch {
                kotlinx.coroutines.delay(100)
                _uiState.update { it.copy(currentPage = maxOf(0, it.totalPages - 1)) }
            }
        }
        saveReadingProgress()
    }

    fun goToChapter(chapterIndex: Int) {
        loadChapterContent(chapterIndex)
        _uiState.update { it.copy(showChapterList = false) }
    }

    fun goToPage(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceIn(0, it.totalPages - 1)) }
        saveReadingProgress()
    }

    fun toggleSettingsPanel() {
        _uiState.update { it.copy(showSettingsPanel = !it.showSettingsPanel) }
    }

    fun toggleChapterList() {
        _uiState.update { it.copy(showChapterList = !it.showChapterList) }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFullScreen() {
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    fun setTextSelection(selection: TextSelection?) {
        _uiState.update { it.copy(textSelection = selection) }
    }

    // Settings
    fun setFontSize(size: Int) {
        viewModelScope.launch {
            settingsDataStore.setFontSize(size)
        }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch {
            settingsDataStore.setLineSpacing(spacing)
        }
    }

    fun setPageMargin(margin: Int) {
        viewModelScope.launch {
            settingsDataStore.setPageMargin(margin)
        }
    }

    fun setPageMode(mode: PageMode) {
        viewModelScope.launch {
            settingsDataStore.setPageMode(mode.name.lowercase())
        }
    }

    fun setReaderTheme(theme: ReaderThemeType) {
        viewModelScope.launch {
            settingsDataStore.setReaderTheme(theme.name.lowercase())
        }
    }

    fun setBrightness(brightness: Float) {
        viewModelScope.launch {
            settingsDataStore.setBrightness(brightness)
        }
    }

    fun setBrightnessFollowSystem(follow: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setBrightnessFollowSystem(follow)
        }
    }

    // Bookmarks
    fun addBookmark(position: Int = _uiState.value.currentPage) {
        viewModelScope.launch {
            val state = _uiState.value
            val chapter = state.currentChapter ?: return@launch

            val bookmark = Bookmark(
                bookId = bookId,
                chapterIndex = state.currentChapterIndex,
                chapterTitle = chapter.title,
                position = position,
                color = BookmarkColor.RED
            )
            bookmarkRepository.insertBookmark(bookmark)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmarkId)
        }
    }

    fun isCurrentPositionBookmarked(): Boolean {
        return _uiState.value.bookmarks.any {
            it.chapterIndex == _uiState.value.currentChapterIndex &&
                    it.position == _uiState.value.currentPage
        }
    }

    // Notes
    fun addNote(highlightedText: String, note: String, color: HighlightColor = HighlightColor.YELLOW) {
        viewModelScope.launch {
            val state = _uiState.value
            val chapter = state.currentChapter ?: return@launch

            val noteEntity = Note(
                bookId = bookId,
                chapterIndex = state.currentChapterIndex,
                chapterTitle = chapter.title,
                startPosition = 0,
                endPosition = 0,
                highlightedText = highlightedText,
                note = note,
                highlightColor = color
            )
            noteRepository.insertNote(noteEntity)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    private fun saveReadingProgress() {
        viewModelScope.launch {
            val state = _uiState.value
            val progress = if (state.totalPages > 0) {
                (state.currentChapterIndex + state.currentPage.toFloat() / state.totalPages) / state.chapters.size.coerceAtLeast(1)
            } else 0f

            bookRepository.updateReadingProgress(
                bookId = bookId,
                chapterIndex = state.currentChapterIndex,
                position = state.currentPage,
                progress = progress
            )

            // Record reading time
            val elapsed = (System.currentTimeMillis() - startTime) / 1000
            if (elapsed > 0) {
                // We'll save reading time when leaving the reader
            }
        }
    }

    fun onLeaveReader() {
        saveReadingProgress()
    }
}
