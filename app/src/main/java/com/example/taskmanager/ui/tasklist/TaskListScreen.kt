package com.example.taskmanager.ui.tasklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
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
                        items(tasks) { task ->
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
                    }
                }
            }
        }
    }
}