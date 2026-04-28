package com.mochen.reader.presentation.bookshelf

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mochen.reader.R
import com.mochen.reader.domain.model.Book
import com.mochen.reader.domain.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    onBookClick: (Long) -> Unit,
    onWifiTransferClick: () -> Unit,
    onGroupManagementClick: () -> Unit,
    viewModel: BookshelfViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBook(it) }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.importBooksFromFolder(it) }
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                // Search bar expanded
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onSearch = { showSearch = false },
                    active = showSearch,
                    onActiveChange = { showSearch = it },
                    placeholder = { Text(stringResource(R.string.bookshelf_search)) },
                    leadingIcon = {
                        IconButton(onClick = {
                            viewModel.setSearchQuery("")
                            showSearch = false
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (showSearch) 0.dp else 16.dp)
                ) {
                    // Search results shown in the main content area
                }
            } else {
                TopAppBar(
                    title = {
                        if (uiState.isSelectionMode) {
                            Text("${uiState.selectedBookIds.size} 已选择")
                        } else {
                            Text(stringResource(R.string.bookshelf_title))
                        }
                    },
                    navigationIcon = {
                        if (uiState.isSelectionMode) {
                            IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                                Icon(Icons.Default.Close, contentDescription = "关闭")
                            }
                        }
                    },
                    actions = {
                        if (uiState.isSelectionMode) {
                            IconButton(onClick = { viewModel.selectAll() }) {
                                Icon(Icons.Default.SelectAll, contentDescription = "全选")
                            }
                            IconButton(
                                onClick = { showMoveDialog = true },
                                enabled = uiState.selectedBookIds.isNotEmpty()
                            ) {
                                Icon(Icons.Default.DriveFileMove, contentDescription = "移动")
                            }
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                enabled = uiState.selectedBookIds.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        } else {
                            // Search icon button (shows search bar on click)
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                            IconButton(onClick = { viewModel.toggleViewMode() }) {
                                Icon(
                                    if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "切换视图"
                                )
                            }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.Sort, contentDescription = "排序")
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOrder.entries.forEach { sortOrder ->
                                        DropdownMenuItem(
                                            text = { Text(sortOrder.displayName) },
                                            onClick = {
                                                viewModel.setSortOrder(sortOrder)
                                                showSortMenu = false
                                            },
                                            leadingIcon = {
                                                if (uiState.sortOrder == sortOrder) {
                                                    Icon(Icons.Default.Check, contentDescription = null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { onGroupManagementClick }) {
                                Icon(Icons.Default.Folder, contentDescription = "分组管理")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                Box {
                    FloatingActionButton(
                        onClick = { showAddMenu = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加书籍")
                    }
                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                        // Position the menu above and to the left of the FAB
                        offset = DpOffset(x = (-80).dp, y = (-160).dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_from_file)) },
                            onClick = {
                                filePickerLauncher.launch(arrayOf("*/*"))
                                showAddMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.FileOpen, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_from_folder)) },
                            onClick = {
                                folderPickerLauncher.launch(null)
                                showAddMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_from_wifi)) },
                            onClick = {
                                onWifiTransferClick()
                                showAddMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group tabs
            if (uiState.groups.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = uiState.groups.indexOfFirst { it.id == uiState.selectedGroupId } + 1,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 16.dp
                ) {
                    Tab(
                        selected = uiState.selectedGroupId == null,
                        onClick = { viewModel.setSelectedGroup(null) },
                        text = { Text("全部") }
                    )
                    uiState.groups.forEach { group ->
                        Tab(
                            selected = uiState.selectedGroupId == group.id,
                            onClick = { viewModel.setSelectedGroup(group.id) },
                            text = { Text(group.name) }
                        )
                    }
                }
            }

            // Book list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        if (uiState.isImporting) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.importProgress,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else if (uiState.books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.bookshelf_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "点击右下角 + 按钮导入书籍",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                if (uiState.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.books) { bookItem: com.mochen.reader.domain.model.Book ->
                            BookGridItem(
                                book = bookItem,
                                isSelected = bookItem.id in uiState.selectedBookIds,
                                isSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleBookSelection(bookItem.id)
                                    } else {
                                        onBookClick(bookItem.id)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                        viewModel.toggleBookSelection(bookItem.id)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.books) { bookItem: com.mochen.reader.domain.model.Book ->
                            BookListItem(
                                book = bookItem,
                                isSelected = bookItem.id in uiState.selectedBookIds,
                                isSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleBookSelection(bookItem.id)
                                    } else {
                                        onBookClick(bookItem.id)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                        viewModel.toggleBookSelection(bookItem.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除选中的 ${uiState.selectedBookIds.size} 本书籍吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSelectedBooks()
                            showDeleteDialog = false
                        }
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Move to group dialog
        if (showMoveDialog) {
            AlertDialog(
                onDismissRequest = { showMoveDialog = false },
                title = { Text("移动到分组") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                viewModel.moveSelectedBooksToGroup(0)
                                showMoveDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("默认分组")
                        }
                        uiState.groups.forEach { group ->
                            TextButton(
                                onClick = {
                                    viewModel.moveSelectedBooksToGroup(group.id)
                                    showMoveDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(group.name)
                            }
                        }
                        TextButton(
                            onClick = { showMoveDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("取消")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}

@Composable
fun BookGridItem(
    book: Book,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (isSelected) {
                        Modifier.background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        )
                    } else Modifier
                )
        ) {
            if (book.coverPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.coverPath)
                        .crossfade(true)
                        .build(),
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Selection indicator
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }

            // Progress badge
            if (book.currentProgress > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${book.progressPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BookListItem(
    book: Book,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(60.dp, 80.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (book.coverPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverPath)
                            .crossfade(true)
                            .build(),
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = book.format.extension.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = book.formattedFileSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (book.currentProgress > 0) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    CircularProgressIndicator(
                        progress = { book.currentProgress },
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "${book.progressPercentage}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
