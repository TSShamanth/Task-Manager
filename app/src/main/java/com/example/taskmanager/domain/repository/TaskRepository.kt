package com.example.taskmanager.domain.repository

import com.example.taskmanager.data.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    suspend fun insertTask(task: Task)

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    fun getTaskById(taskId: Int): Flow<Task?>

    fun getAllTasks(): Flow<List<Task>>
}