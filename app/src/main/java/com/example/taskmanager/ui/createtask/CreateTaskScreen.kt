package com.example.taskmanager.ui.createtask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.taskmanager.data.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CreateTaskScreen(
    navController: NavController,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var priority by remember { mutableStateOf("1") }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current

    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
            dueDate = calendar.timeInMillis
            if (hasReminder) reminderTime = calendar.timeInMillis // Update reminder date as well
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val tempCalendar = Calendar.getInstance().apply { timeInMillis = dueDate }
            tempCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            tempCalendar.set(Calendar.MINUTE, selectedMinute)
            reminderTime = tempCalendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    // Initialize reminderTime if hasReminder is true and reminderTime is null
    if (hasReminder && reminderTime == null) {
        reminderTime = dueDate
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { datePickerDialog.show() }) {
            Text("Select Due Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate)}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = priority,
            onValueChange = { priority = it },
            label = { Text("Priority (1-5)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = hasReminder,
                onCheckedChange = { hasReminder = it }
            )
            Text("Set Reminder")
        }
        if (hasReminder) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { timePickerDialog.show() }) {
                Text("Select Reminder Time: ${reminderTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: ""}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (title.isNotBlank()) {
                val task = Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority.toIntOrNull() ?: 1,
                    status = "Pending",
                    reminder = if (hasReminder) reminderTime else null
                )
                viewModel.addTask(task)
                navController.popBackStack()
            } else {
                // Show error for empty title
            }
        }) {
            Text("Save Task")
        }
    }
}