package com.example.studenthub.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studenthub.ui.theme.STUDENTHUBTheme
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

            ProfileInfo("Subjects", "2")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfo("Assignments", "2/2")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileInfo("Average", "4.0")

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
fun NotificationsDrawer(modifier: Modifier = Modifier) {
    ModalDrawerSheet(modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight()
        ) {
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Alert", fontWeight = FontWeight.Bold)
                    Text("Alert text goes here")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reminders", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Reminder")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Remind title", fontWeight = FontWeight.Bold)
                    Text("Remind date-hour")
                }
            }
        }
    }
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
