package com.example.taskmanager.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.Task
import com.example.taskmanager.domain.usecase.DeleteTaskUseCase
import com.example.taskmanager.domain.usecase.GetTasksUseCase
import com.example.taskmanager.domain.usecase.UpdateTaskUseCase
import com.example.taskmanager.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _tasks = MutableStateFlow<Map<String, List<Task>>>(emptyMap())
    val tasks: StateFlow<Map<String, List<Task>>> = _tasks.asStateFlow()

    private val _currentFilter = MutableStateFlow<TaskFilter>(TaskFilter.All)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        getTasks()
    }

    private fun getTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                combine(getTasksUseCase(), _currentFilter, _searchQuery) { tasks, filter, query ->
                    val filteredTasks = tasks.filter { task ->
                        task.title.contains(query, ignoreCase = true)
                    }.filter { task ->
                        when (filter) {
                            TaskFilter.All -> true
                            TaskFilter.Today -> isToday(task.dueDate) && task.status != "Completed"
                            TaskFilter.Completed -> task.status == "Completed"
                            TaskFilter.Overdue -> isOverdue(task.dueDate) && task.status != "Completed"
                        }
                    }
                    groupTasks(filteredTasks)
                }.onEach {
                    _tasks.value = it
                }.launchIn(viewModelScope)
            } catch (e: Exception) {
                _error.value = "Failed to load tasks: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = if (isChecked) "Completed" else "Pending")
            updateTaskUseCase(updatedTask)
            if (isChecked) {
                alarmScheduler.cancel(updatedTask)
            } else {
                updatedTask.reminder?.let { alarmScheduler.schedule(updatedTask) }
            }
        }
    }

    fun onFilterSelected(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            alarmScheduler.cancel(task)
            deleteTaskUseCase(task)
        }
    }

    private fun groupTasks(tasks: List<Task>): Map<String, List<Task>> {
        val grouped = mutableMapOf<String, MutableList<Task>>()

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        tomorrow.set(Calendar.HOUR_OF_DAY, 0)
        tomorrow.set(Calendar.MINUTE, 0)
        tomorrow.set(Calendar.SECOND, 0)
        tomorrow.set(Calendar.MILLISECOND, 0)

        tasks.forEach { task ->
            val taskDate = Calendar.getInstance().apply { timeInMillis = task.dueDate }
            taskDate.set(Calendar.HOUR_OF_DAY, 0)
            taskDate.set(Calendar.MINUTE, 0)
            taskDate.set(Calendar.SECOND, 0)
            taskDate.set(Calendar.MILLISECOND, 0)

            when {
                task.status == "Completed" -> {
                    grouped.getOrPut("Completed") { mutableListOf() }.add(task)
                }
                taskDate.timeInMillis == today.timeInMillis -> {
                    grouped.getOrPut("Today") { mutableListOf() }.add(task)
                }
                taskDate.timeInMillis == tomorrow.timeInMillis -> {
                    grouped.getOrPut("Tomorrow") { mutableListOf() }.add(task)
                }
                taskDate.timeInMillis < today.timeInMillis -> {
                    grouped.getOrPut("Overdue") { mutableListOf() }.add(task)
                }
                else -> {
                    grouped.getOrPut("Upcoming") { mutableListOf() }.add(task)
                }
            }
        }
        return grouped
    }

    private fun isToday(dueDate: Long): Boolean {
        val today = Calendar.getInstance()
        val taskDate = Calendar.getInstance().apply { timeInMillis = dueDate }
        return today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isOverdue(dueDate: Long): Boolean {
        val today = Calendar.getInstance()
        val taskDate = Calendar.getInstance().apply { timeInMillis = dueDate }
        return taskDate.before(today) && !isToday(dueDate)
    }
}

sealed class TaskFilter(val displayName: String) {
    object All : TaskFilter("All")
    object Today : TaskFilter("Today")
    object Completed : TaskFilter("Completed")
    object Overdue : TaskFilter("Overdue")

    companion object {
        fun values() = listOf(All, Today, Completed, Overdue)
    }
}