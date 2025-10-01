package com.example.taskmanager.ui.tasklist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.taskmanager.data.Task
import com.example.taskmanager.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val groupedTasks by viewModel.tasks.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showSearchField by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Task Manager") },
                    actions = {
                        IconButton(onClick = { showSearchField = !showSearchField }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Tasks")
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Filter Tasks")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            TaskFilter.values().forEach { filter ->
                                DropdownMenuItem(text = { Text(filter.displayName) }, onClick = {
                                    viewModel.onFilterSelected(filter)
                                    showMenu = false
                                })
                            }
                        }
                    }
                )
                if (showSearchField) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        label = { Text("Search by title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateTask.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (groupedTasks.isEmpty()) {
                Text(text = "No tasks yet!", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    groupedTasks.forEach { (category, tasks) ->
                        item { Text(text = category, modifier = Modifier.padding(16.dp)) }
                        items(tasks, key = { it.id }) { task ->
                            var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                        dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        showDeleteConfirmationDialog = true
                                        false
                                    } else {
                                        false
                                    }
                                }
                            )

                            if (showDeleteConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmationDialog = false },
                                    title = { Text("Confirm Deletion") },
                                    text = { Text("Are you sure you want to delete this task?") },
                                    confirmButton = {
                                        Button(onClick = {
                                            viewModel.deleteTask(task)
                                            showDeleteConfirmationDialog = false
                                        }) {
                                            Text("Delete")
                                        }
                                    },
                                    dismissButton = {
                                        Button(onClick = {
                                            showDeleteConfirmationDialog = false
                                            scope.launch { dismissState.reset() }
                                        }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.Settled -> Color.LightGray
                                            else -> Color.Red
                                        },
                                        label = ""
                                    )
                                    val alignment = Alignment.CenterEnd
                                    val icon = Icons.Default.Delete

                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = "Delete Icon",
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                },
                                content = {
                                    TaskItem(
                                        task = task,
                                        onTaskClick = { task ->
                                            navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                        },
                                        onTaskCheckedChange = { task, isChecked ->
                                            viewModel.onTaskCheckedChanged(task, isChecked)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Task status updated",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                                    viewModel.onTaskCheckedChanged(task, !isChecked)
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}