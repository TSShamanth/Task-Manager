# Personal Task Manager Android App

## Goal

This project aims to build a simple, functional, and polished Personal Task Manager Android application. It allows users to manage their tasks efficiently with features like task creation, viewing, filtering, updating, and reminder notifications.

## Features

*   **Task Management:** Create, view, update, and delete tasks with details such as title, description, due date/time, priority, status, and optional reminders.
*   **Task List:** Display tasks grouped by due date (Today, Tomorrow, Overdue, Upcoming) with sticky headers. Tasks can be filtered by All, Today, Completed, and Overdue.
*   **Task Detail/Edit:** View full information about a selected task and make further edits.
*   **Local Storage:** All tasks are stored locally on the device using Room database, ensuring data persistence across app restarts.
*   **Reminder Notifications:** Support for reminder notifications for tasks at the scheduled time using `AlarmManager`.
*   **Modern UI:** Built with Jetpack Compose, providing a clean, user-friendly design with proper navigation across multiple screens.
*   **Undo Functionality:** A snackbar with an undo option appears when a task's status is updated.

## Architecture

The application follows a Clean Architecture pattern with a clear separation of concerns, divided into three main layers:

*   **UI Layer:** Composed of Jetpack Compose Composables and ViewModels for lifecycle-aware state management. It handles all UI states (loading, empty, error, content).
*   **Domain Layer:** Contains Use Cases (Interactors) that encapsulate the business logic of the application, acting as intermediaries between the UI and Data layers.
*   **Data Layer:** Responsible for data persistence and retrieval. It includes a `TaskRepository` interface and its `TaskRepositoryImpl` implementation, which interacts with the local Room database (`TaskDao`).

**Dependency Injection:** Hilt is used for dependency injection to manage dependencies and decouple components across the layers.

**Asynchronous Operations:** Kotlin Coroutines and Flows are utilized for non-blocking operations and smooth performance.

## Technologies Used

*   **Kotlin:** Primary programming language.
*   **Jetpack Compose:** Modern toolkit for building native Android UI.
*   **Jetpack Navigation Compose:** For navigating between screens.
*   **Hilt (Dagger Hilt):** For dependency injection.
*   **Room Persistence Library:** For local database storage.
*   **Kotlin Coroutines & Flow:** For asynchronous programming and reactive data streams.
*   **AlarmManager:** For scheduling task reminder notifications.
*   **Material 3:** For modern UI components and design principles.

## How to Build and Run

1.  **Clone the repository:**
    ```bash
    git clone <repository_url>
    cd TaskManager
    ```
2.  **Open in Android Studio:** Open the cloned project in Android Studio (Jellyfish | 2023.3.1 or newer recommended).
3.  **Sync Gradle:** Let Gradle sync the project dependencies. This might take some time.
4.  **Run on a Device/Emulator:** Select an Android device or emulator (API Level 24 or higher) and click the 'Run' button in Android Studio.

## Images Showcase

These are some pictures of the app's user interface :<br /><br />
<img src="Images/Main Screen.png" width=180>
<br /><br />
<img src="Images/Filter_options Screen.png" width=180>
<br /><br />
<img src="Images/Search_Bar Screen.png" width=180>
<br /><br />
<img src="Images/Create_Task Screen.png" width=180>
<br /><br />
<img src="Images/Today_Task Screen.png" width=180>
<br /><br />
<img src="Images/Completed_Task Screen.png" width=180>
<br /><br />
<img src="Images/Overdue_task Screen.png" width=180>
<br /><br />
<img src="Images/Edit_Task Screen.png" width=180>

## Notes, Assumptions, and Limitations

*   **Default Values:** When creating a task, if priority is not entered, it defaults to 1. If a reminder is set without a specific time, it will default to the current time.
*   **Error Handling:** Basic validation for required fields (e.g., task title) is implemented. More robust error handling and user feedback can be added.
*   **UI Polish:** The UI is functional but can be further polished with more custom styling, animations, and better handling of various screen sizes and orientations.
*   **Notification Actions:** Advanced notification actions (Mark Done, Snooze) are not yet implemented but are planned.
*   **Search Functionality:** Search by title is not yet implemented.
*   **Time Zones:** Reminder scheduling currently uses the device's default time zone. For a more robust solution, explicit time zone handling might be required.
*   **Boot Completed:** While the `RECEIVE_BOOT_COMPLETED` permission is added, the logic to reschedule alarms after a device reboot is not yet implemented.
