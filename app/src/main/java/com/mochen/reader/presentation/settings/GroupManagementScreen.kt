package com.mochen.reader.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mochen.reader.R
import com.mochen.reader.domain.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    onBackClick: () -> Unit,
    viewModel: GroupManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分组管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "新建分组")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无分组",
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建分组")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(uiState.groups, key = { it.id }) { group ->
                    ListItem(
                        headlineContent = { Text(group.name) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    editingGroup = group
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                                }
                                IconButton(onClick = {
                                    editingGroup = group
                                    showDeleteDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            editingGroup = group
                            showEditDialog = true
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        // Create dialog
        if (showCreateDialog) {
            var groupName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("新建分组") },
                text = {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("分组名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                viewModel.createGroup(groupName)
                                showCreateDialog = false
                            }
                        },
                        enabled = groupName.isNotBlank()
                    ) {
                        Text("创建")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Edit dialog
        if (showEditDialog && editingGroup != null) {
            var groupName by remember { mutableStateOf(editingGroup!!.name) }
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    editingGroup = null
                },
                title = { Text("编辑分组") },
                text = {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("分组名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                viewModel.updateGroup(editingGroup!!.copy(name = groupName))
                                showEditDialog = false
                                editingGroup = null
                            }
                        },
                        enabled = groupName.isNotBlank()
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditDialog = false
                        editingGroup = null
                    }) {
                        Text("取消")
                    }
                }
            )
        }

        // Delete dialog
        if (showDeleteDialog && editingGroup != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    editingGroup = null
                },
                title = { Text("确认删除") },
                text = { Text("确定要删除分组 \"${editingGroup!!.name}\" 吗？分组内的书籍将移至默认分组。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteGroup(editingGroup!!.id)
                            showDeleteDialog = false
                            editingGroup = null
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        editingGroup = null
                    }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
