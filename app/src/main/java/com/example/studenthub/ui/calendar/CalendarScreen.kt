package com.example.studenthub.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studenthub.data.Assignment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    var assignments by remember { mutableStateOf<Map<LocalDate, List<Assignment>>>(emptyMap()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val user = Firebase.auth.currentUser

    LaunchedEffect(user, currentMonth) {
        if (user != null) {
            val db = Firebase.firestore
            val firstDayOfMonth = currentMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val lastDayOfMonth = currentMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

            db.collection("assignments")
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("dueDate", firstDayOfMonth)
                .whereLessThanOrEqualTo("dueDate", lastDayOfMonth)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val newAssignments = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Assignment::class.java)
                        }.groupBy { LocalDate.parse(it.dueDate) }
                        assignments = newAssignments
                    }
                }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        CalendarView(
            currentMonth = currentMonth,
            onMonthChange = { currentMonth = it },
            assignments = assignments,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AssignmentsForDay(
            date = selectedDate,
            assignments = assignments[selectedDate] ?: emptyList()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    assignments: Map<LocalDate, List<Assignment>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val days = (1..firstDayOfMonth.dayOfWeek.value % 7).map { "" } + (1..daysInMonth).map { it.toString() }

    Card {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }
                Text(
                    "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false) {
                items(days.size) { index ->
                    val day = days[index]
                    if (day.isNotEmpty()) {
                        val date = currentMonth.atDay(day.toInt())
                        val hasAssignment = assignments.containsKey(date)
                        val isSelected = selectedDate == date

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) }
                        ) {
                            Text(
                                text = day,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    hasAssignment -> Color.Red
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (hasAssignment) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AssignmentsForDay(date: LocalDate, assignments: List<Assignment>) {
    Column {
        Text(
            "Assignments for ${date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (assignments.isEmpty()) {
            Text("No assignments for this day.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(assignments.size) { index ->
                    val assignment = assignments[index]
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(assignment.name, fontWeight = FontWeight.Bold)
                            Text(assignment.subjectName, style = MaterialTheme.typography.bodySmall)
                            Text("Due at: ${assignment.dueTime}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
