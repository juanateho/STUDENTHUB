package com.example.studenthub.ui.main

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studenthub.data.Assignment
import com.example.studenthub.data.Notification
import com.example.studenthub.data.Reminder
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxHeight()
    ) {
        // Today Subjects
        Text("Today Subjects", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SubjectCard(subject = "Math", time = "1h 30m", icon = Icons.Filled.Book)
            }
            Box(modifier = Modifier.weight(1f)) {
                SubjectCard(
                    subject = "Chemistry",
                    time = "2h 00m",
                    icon = Icons.Filled.Science
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Categories
        Text("Categories", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Categories()
        Spacer(modifier = Modifier.height(24.dp))

        // Calendar
        Text("Calendar", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        CalendarView(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ProfileDrawer(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onProfileEdit: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val user = auth.currentUser

    var subjectCount by remember { mutableStateOf(0) }
    var assignmentCount by remember { mutableStateOf(0) }
    var gradedCount by remember { mutableStateOf(0) }
    var averageGrade by remember { mutableStateOf(0.0f) }

    LaunchedEffect(user) {
        if (user != null) {
            // Count Subjects
            db.collection("subjects")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        subjectCount = snapshot.size()
                    }
                }

            // Count Assignments & Calculate Average
            db.collection("assignments")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        assignmentCount = snapshot.size()
                        val assignments = snapshot.documents.mapNotNull { it.toObject(Assignment::class.java) }
                        val graded = assignments.filter { it.grade > 0 }
                        gradedCount = graded.size
                        if (graded.isNotEmpty()) {
                            averageGrade = graded.map { it.grade }.average().toFloat()
                        } else {
                            averageGrade = 0.0f
                        }
                    }
                }
        }
    }

    ModalDrawerSheet(modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profile", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onProfileEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            ProfileInfo("Subjects", subjectCount.toString())
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfo("Assignments (Graded/Total)", "$gradedCount/$assignmentCount")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfo("Average", String.format(Locale.US, "%.1f", averageGrade))

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NotificationsDrawer(
    modifier: Modifier = Modifier,
    onAddReminder: () -> Unit = {}
) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val context = LocalContext.current

    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Reminder?>(null) }

    val authState by auth.authStateFlow().collectAsState(initial = auth.currentUser)

    LaunchedEffect(authState) {
        val currentUser = authState
        if (currentUser != null) {
            db.collection("reminders")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) { return@addSnapshotListener }
                    if (snapshot != null) {
                        reminders = snapshot.documents.mapNotNull { it.toObject(Reminder::class.java) }
                    }
                }

            db.collection("notifications")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) { return@addSnapshotListener }
                    if (snapshot != null) {
                        notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                    }
                }
        } else {
            reminders = emptyList()
            notifications = emptyList()
        }
    }

    if (editingReminder != null) {
        EditReminderDialog(
            reminder = editingReminder!!,
            onDismiss = { editingReminder = null },
            onSave = { updatedReminder ->
                db.collection("reminders").document(updatedReminder.id).set(updatedReminder)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Reminder updated", Toast.LENGTH_SHORT).show()
                    }
                editingReminder = null
            }
        )
    }

    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reminderToDelete = showDeleteConfirmation
                        if (reminderToDelete != null) {
                            db.collection("reminders").document(reminderToDelete.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show()
                                }
                        }
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalDrawerSheet(modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Recent Notifications", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (notifications.isEmpty()) {
                Text("No recent notifications.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    notifications.forEach { notification ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(notification.title, fontWeight = FontWeight.Bold)
                                Text(notification.message, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reminders", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onAddReminder) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Reminder")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (reminders.isEmpty()) {
                Text("No reminders set.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminders.forEach { reminder ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { editingReminder = reminder }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reminder.title, fontWeight = FontWeight.Bold)
                                    Text("${reminder.date} at ${reminder.time}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showDeleteConfirmation = reminder }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Reminder", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditReminderDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(reminder.title) }
    var date by remember { mutableStateOf(reminder.date) }
    var time by remember { mutableStateOf(reminder.time) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(reminder.copy(title = title, date = date, time = time))
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SubjectCard(subject: String, time: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Text(subject, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(time, fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun Categories() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CategoryItem(name = "Assignments", icon = Icons.Filled.List) }
        item { CategoryItem(name = "Grades", icon = Icons.Filled.Assessment) }
        item { CategoryItem(name = "Calendar", icon = Icons.Filled.CalendarToday) }
        item { CategoryItem(name = "Reminders", icon = Icons.Filled.Notifications) }
    }
}

@Composable
fun CategoryItem(name: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier.size(80.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = name, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 12.sp)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(modifier: Modifier = Modifier) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val days = remember(currentMonth) {
        val firstDay = currentMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        (1..firstDayOfWeek).map { "" } + (1..daysInMonth).map { it.toString() }
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}".uppercase(),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val daysOfWeek = DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                daysOfWeek.forEach {
                    Text(it, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f)
            ) {
                items(days.size) { index ->
                    val day = days[index]
                    if (day.isNotEmpty()) {
                        val date = currentMonth.atDay(day.toInt())
                        val isToday = date == LocalDate.now()
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                        ) {
                            Text(
                                text = day,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    STUDENTHUBTheme {
        MainScreen()
    }
}

private fun FirebaseAuth.authStateFlow(): StateFlow<FirebaseUser?> {
    val a = MutableStateFlow(this.currentUser)
    this.addAuthStateListener { a.value = it.currentUser }
    return a
}