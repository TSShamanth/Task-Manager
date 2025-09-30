package com.example.taskmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmanager.ui.createtask.CreateTaskScreen
import com.example.taskmanager.ui.taskdetail.TaskDetailScreen
import com.example.taskmanager.ui.tasklist.TaskListScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.TaskList.route) {
        composable(Screen.TaskList.route) {
            TaskListScreen(navController = navController)
        }
        composable(Screen.CreateTask.route) {
            CreateTaskScreen(navController = navController)
        }
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) {
            TaskDetailScreen(navController = navController)
        }
    }
}