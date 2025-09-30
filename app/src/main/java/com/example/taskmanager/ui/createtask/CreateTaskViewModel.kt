package com.example.taskmanager.ui.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.Task
import com.example.taskmanager.domain.usecase.AddTaskUseCase
import com.example.taskmanager.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    fun addTask(task: Task) {
        viewModelScope.launch {
            addTaskUseCase(task)
            task.reminder?.let { 
                alarmScheduler.schedule(task)
            }
        }
    }
}