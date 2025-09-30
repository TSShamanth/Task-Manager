package com.example.taskmanager.navigation

sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object CreateTask : Screen("create_task")
    object TaskDetail : Screen("task_detail")
}