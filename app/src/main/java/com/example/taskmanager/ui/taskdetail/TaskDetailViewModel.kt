package com.example.taskmanager.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.Task
import com.example.taskmanager.domain.usecase.DeleteTaskUseCase
import com.example.taskmanager.domain.usecase.GetTaskByIdUseCase
import com.example.taskmanager.domain.usecase.UpdateTaskUseCase
import com.example.taskmanager.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    init {
        savedStateHandle.get<Int>("taskId")?.let { taskId ->
            viewModelScope.launch {
                getTaskByIdUseCase(taskId).filterNotNull().collect { task ->
                    _task.value = task
                }
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
            task.reminder?.let { 
                alarmScheduler.schedule(task)
            } ?: alarmScheduler.cancel(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            alarmScheduler.cancel(task)
            deleteTaskUseCase(task)
        }
    }
}