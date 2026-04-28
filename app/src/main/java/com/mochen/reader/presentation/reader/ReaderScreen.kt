package com.mochen.reader.presentation.reader

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.mochen.reader.R
import com.mochen.reader.domain.model.Bookmark
import com.mochen.reader.domain.model.Chapter
import com.mochen.reader.domain.model.HighlightColor
import com.mochen.reader.domain.model.Note
import com.mochen.reader.presentation.theme.ReaderColors
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    initialChapterIndex: Int = 0,
    onBackClick: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    // Apply initial chapter index when available
    LaunchedEffect(initialChapterIndex, viewModel) {
        if (initialChapterIndex > 0) {
            viewModel.goToChapter(initialChapterIndex)
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    // Full screen handling
    DisposableEffect(uiState.isFullScreen) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (uiState.isFullScreen) {
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            activity?.let {
                WindowCompat.getInsetsController(it.window, it.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Keep screen on
    DisposableEffect(uiState.keepScreenOn) {
        val window = activity?.window
        if (uiState.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Screen orientation
    DisposableEffect(Unit) {
        val currentOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        onDispose {
            activity?.requestedOrientation = currentOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            viewModel.onLeaveReader()
        }
    }

    // Background color based on theme
    val backgroundColor = when (uiState.readerTheme) {
        ReaderThemeType.WHITE -> ReaderColors.WhiteBg
        ReaderThemeType.CREAM -> ReaderColors.CreamBg
        ReaderThemeType.GREEN -> ReaderColors.GreenBg
        ReaderThemeType.GRAY -> ReaderColors.GrayBg
        ReaderThemeType.BLACK -> ReaderColors.BlackBg
    }

    val textColor = when (uiState.readerTheme) {
        ReaderThemeType.WHITE -> ReaderColors.WhiteText
        ReaderThemeType.CREAM -> ReaderColors.CreamText
        ReaderThemeType.GREEN -> ReaderColors.GreenText
        ReaderThemeType.GRAY -> ReaderColors.GrayText
        ReaderThemeType.BLACK -> ReaderColors.BlackText
    }

    // Swipe threshold for horizontal drag
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 50.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val screenWidth = size.width
                        when {
                            offset.x < screenWidth / 3 -> viewModel.previousPage()
                            offset.x > screenWidth * 2 / 3 -> viewModel.nextPage()
                            else -> viewModel.toggleSettingsPanel()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {},
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (abs(dragAmount) > swipeThreshold) {
                            if (dragAmount < 0) {
                                // Swipe left -> next page
                                viewModel.nextPage()
                            } else {
                                // Swipe right -> previous page
                                viewModel.previousPage()
                            }
                        }
                    }
                )
            }
    ) {
        // Content
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = textColor
            )
        } else {
            when (uiState.pageMode) {
                PageMode.VERTICAL -> {
                    VerticalScrollReader(
                        content = uiState.chapterContent,
                        fontSize = uiState.fontSize,
                        lineSpacing = uiState.lineSpacing,
                        textColor = textColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    PagedReader(
                        content = if (uiState.pages.isNotEmpty()) uiState.pages[uiState.currentPage] else "",
                        currentPage = uiState.currentPage,
                        totalPages = uiState.totalPages,
                        fontSize = uiState.fontSize,
                        lineSpacing = uiState.lineSpacing,
                        textColor = textColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Top bar
        AnimatedVisibility(
            visible = !uiState.isFullScreen || uiState.showSettingsPanel || uiState.showChapterList,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.book?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.currentChapter?.title ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleChapterList() }) {
                        Icon(Icons.Default.List, contentDescription = "目录")
                    }
                    IconButton(onClick = {
                        if (viewModel.isCurrentPositionBookmarked()) {
                            val bookmark = uiState.bookmarks.find {
                                it.chapterIndex == uiState.currentChapterIndex &&
                                        it.position == uiState.currentPage
                            }
                            bookmark?.let { viewModel.deleteBookmark(it.id) }
                        } else {
                            viewModel.addBookmark()
                        }
                    }) {
                        Icon(
                            if (viewModel.isCurrentPositionBookmarked())
                                Icons.Filled.Bookmark
                            else
                                Icons.Default.BookmarkBorder,
                            contentDescription = "书签"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor.copy(alpha = 0.95f)
                )
            )
        }

        // Bottom bar
        AnimatedVisibility(
            visible = !uiState.isFullScreen || uiState.showSettingsPanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomAppBar(
                containerColor = backgroundColor.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous chapter
                    IconButton(
                        onClick = {
                            if (uiState.currentChapterIndex > 0) {
                                viewModel.goToChapter(uiState.currentChapterIndex - 1)
                            }
                        },
                        enabled = uiState.currentChapterIndex > 0
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "上一章")
                    }

                    // Progress slider
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "第 ${uiState.currentPage + 1} / ${uiState.totalPages} 页",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Slider(
                            value = uiState.currentPage.toFloat(),
                            onValueChange = { viewModel.goToPage(it.toInt()) },
                            valueRange = 0f..maxOf(uiState.totalPages - 1, 0).toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Next chapter
                    IconButton(
                        onClick = {
                            if (uiState.currentChapterIndex < uiState.chapters.size - 1) {
                                viewModel.goToChapter(uiState.currentChapterIndex + 1)
                            }
                        },
                        enabled = uiState.currentChapterIndex < uiState.chapters.size - 1
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "下一章")
                    }
                }
            }
        }

        // Settings panel (ModalBottomSheet)
        if (uiState.showSettingsPanel) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleSettingsPanel() },
                containerColor = backgroundColor
            ) {
                ReaderSettingsPanelContent(
                    uiState = uiState,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    onFontSizeChange = { viewModel.setFontSize(it) },
                    onLineSpacingChange = { viewModel.setLineSpacing(it) },
                    onThemeChange = { viewModel.setReaderTheme(it) },
                    onBrightnessChange = { viewModel.setBrightness(it) },
                    onBrightnessFollowSystemChange = { viewModel.setBrightnessFollowSystem(it) },
                    onPageModeChange = { viewModel.setPageMode(it) }
                )
            }
        }

        // Chapter list panel
        AnimatedVisibility(
            visible = uiState.showChapterList,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            ChapterListPanel(
                chapters = uiState.chapters,
                currentChapterIndex = uiState.currentChapterIndex,
                onChapterClick = { viewModel.goToChapter(it) },
                onDismiss = { viewModel.toggleChapterList() }
            )
        }
    }
}

@Composable
fun PagedReader(
    content: String,
    currentPage: Int,
    totalPages: Int,
    fontSize: Int,
    lineSpacing: Float,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(
            start = 24.dp,
            end = 24.dp,
            top = 80.dp,
            bottom = 80.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = content,
                color = textColor,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * lineSpacing).sp
            )
        }
    }
}

@Composable
fun VerticalScrollReader(
    content: String,
    fontSize: Int,
    lineSpacing: Float,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 80.dp),
        verticalArrangement = Arrangement.spacedBy((fontSize * (lineSpacing - 1)).dp)
    ) {
        items(content.split("\n\n")) { paragraph ->
            Text(
                text = paragraph,
                color = textColor,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * lineSpacing).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsPanelContent(
    uiState: ReaderUiState,
    textColor: Color,
    backgroundColor: Color,
    onFontSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onThemeChange: (ReaderThemeType) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onBrightnessFollowSystemChange: (Boolean) -> Unit,
    onPageModeChange: (PageMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Font size
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("字体大小", color = textColor)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { onFontSizeChange(uiState.fontSize - 2) },
                enabled = uiState.fontSize > 12
            ) {
                Icon(Icons.Default.TextDecrease, contentDescription = "减小", tint = textColor)
            }
            Text("${uiState.fontSize}sp", color = textColor)
            IconButton(
                onClick = { onFontSizeChange(uiState.fontSize + 2) },
                enabled = uiState.fontSize < 36
            ) {
                Icon(Icons.Default.TextIncrease, contentDescription = "增大", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Line spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("行间距", color = textColor)
            Spacer(modifier = Modifier.width(16.dp))
            Slider(
                value = uiState.lineSpacing,
                onValueChange = onLineSpacingChange,
                valueRange = 1.0f..3.0f,
                steps = 7,
                modifier = Modifier.weight(1f)
            )
            Text(String.format("%.1f", uiState.lineSpacing), color = textColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Brightness
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("亮度", color = textColor)
            Spacer(modifier = Modifier.width(16.dp))
            if (uiState.brightnessFollowSystem) {
                Text("跟随系统", color = textColor.copy(alpha = 0.6f))
            } else {
                Slider(
                    value = uiState.brightness,
                    onValueChange = onBrightnessChange,
                    modifier = Modifier.weight(1f)
                )
            }
            Switch(
                checked = uiState.brightnessFollowSystem,
                onCheckedChange = onBrightnessFollowSystemChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Themes
        Text("阅读主题", color = textColor)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReaderThemeType.entries.forEach { theme ->
                val themeColor = when (theme) {
                    ReaderThemeType.WHITE -> Color.White
                    ReaderThemeType.CREAM -> Color(0xFFF5F0E6)
                    ReaderThemeType.GREEN -> Color(0xFFE8F5E9)
                    ReaderThemeType.GRAY -> Color.Gray
                    ReaderThemeType.BLACK -> Color.Black
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColor)
                        .border(
                            width = if (uiState.readerTheme == theme) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onThemeChange(theme) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page mode
        Text("翻页模式", color = textColor)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PageMode.entries.take(4).forEach { mode ->
                FilterChip(
                    selected = uiState.pageMode == mode,
                    onClick = { onPageModeChange(mode) },
                    label = { Text(mode.displayName) }
                )
            }
        }

        // Vertical scroll mode as separate row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.pageMode == PageMode.VERTICAL,
                onClick = { onPageModeChange(PageMode.VERTICAL) },
                label = { Text(PageMode.VERTICAL.displayName) }
            )
        }
    }
}

@Composable
fun ChapterListPanel(
    chapters: List<Chapter>,
    currentChapterIndex: Int,
    onChapterClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "目录",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }
            HorizontalDivider()

            if (chapters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无目录",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn {
                    items(chapters.size) { index ->
                        val chapter = chapters.getOrNull(index)
                        if (chapter != null) {
                            val isCurrent = index == currentChapterIndex
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChapterClick(index) },
                                color = if (isCurrent)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCurrent)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.width(40.dp)
                                    )
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCurrent)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}
