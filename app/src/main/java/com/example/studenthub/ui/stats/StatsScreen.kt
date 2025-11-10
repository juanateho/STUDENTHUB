package com.example.studenthub.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import com.example.studenthub.data.model.MockData
import com.example.studenthub.data.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val subjects = MockData.subjects

    var expanded by remember { mutableStateOf(false) }
    var selectedSubjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Semester grades", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Sort by:")

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = if (selectedSubjects.isEmpty()) "All subjects" else selectedSubjects.joinToString { it.name },
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by subject") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                subjects.forEach { subject ->
                    val allSelected = selectedSubjects.isEmpty()
                    val isSelected = allSelected || selectedSubjects.contains(subject)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    // Uncheck
                                    if (allSelected) {
                                        selectedSubjects = subjects.filter { it != subject }
                                    } else {
                                        selectedSubjects = selectedSubjects - subject
                                    }
                                } else {
                                    // Check
                                    val newSelection = selectedSubjects + subject
                                    if (newSelection.size == subjects.size) {
                                        selectedSubjects = emptyList()
                                    } else {
                                        selectedSubjects = newSelection
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null // The row is clickable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(subject.name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ChartContent(selectedSubjects = selectedSubjects)
    }
}

@Composable
fun ChartContent(selectedSubjects: List<Subject>) {
    val assignments = MockData.assignments

    // Phantom data point to create space at the start of the chart
    val phantomData = BarData(
        point = Point(0f, 0f),
        label = "",
        color = Color.Transparent
    )

    val barData: List<BarData> = when {
        selectedSubjects.size == 1 -> {
            val subject = selectedSubjects.first()
            val realData = assignments.filter { it.subjectId == subject.id }.mapIndexed { index, assignment ->
                BarData(
                    point = Point((index + 1).toFloat(), assignment.grade),
                    label = "Trabajo ${index + 1}",
                    color = Color(0xFF1976D2)
                )
            }
            listOf(phantomData) + realData
        }
        else -> {
            val allSubjects = MockData.subjects
            val subjectsToDisplay = if (selectedSubjects.isEmpty()) allSubjects else selectedSubjects

            val realData = subjectsToDisplay.mapIndexed { index, subject ->
                val subjectAssignments = assignments.filter { it.subjectId == subject.id }
                val average = if (subjectAssignments.isNotEmpty()) {
                    subjectAssignments.map { it.grade }.average().toFloat()
                } else {
                    0f
                }
                BarData(
                    point = Point((index + 1).toFloat(), average),
                    label = subject.name,
                    color = Color(0xFF1976D2)
                )
            }
            listOf(phantomData) + realData
        }
    }

    if (barData.isNotEmpty()) {
        val scrollState = rememberScrollState()

        val xAxisData = AxisData.Builder()
            .axisStepSize(100.dp)
            .steps(barData.size - 1)
            .bottomPadding(80.dp)
            .labelData { index ->
                // The label for axis position 'index' corresponds to the data at 'index'
                if (index < barData.size) barData[index].label else ""
            }
            .axisLabelAngle(90f)
            .axisLabelColor(MaterialTheme.colorScheme.onSurface)
            .build()

        val yAxisData = AxisData.Builder()
            .steps(5)
            .labelAndAxisLinePadding(20.dp)
            .labelData { index -> (index * (5.0 / 5.0)).toFloat().toString() }
            .axisLabelColor(MaterialTheme.colorScheme.onSurface)
            .build()

        val barChartData = BarChartData(
            chartData = barData,
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            barStyle = BarStyle(
                barWidth = 35.dp
            ),
            backgroundColor = MaterialTheme.colorScheme.surface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            val chartWidth = (barData.size * 100).dp
            BarChart(
                modifier = Modifier
                    .width(chartWidth)
                    .height(400.dp),
                barChartData = barChartData
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay datos para mostrar")
        }
    }
}
