package com.example.studenthub.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
    val assignments = MockData.assignments

    var expanded by remember { mutableStateOf(false) }
    var selectedSubjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Estadísticas de Notas", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = if (selectedSubjects.isEmpty()) "Todas las materias" else selectedSubjects.joinToString { it.name },
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtrar por materia") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas las materias") },
                    onClick = {
                        selectedSubjects = emptyList()
                        expanded = false
                    }
                )
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject.name) },
                        onClick = {
                            selectedSubjects = listOf(subject)
                            expanded = false
                        }
                    )
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

    val barData: List<BarData> = when {
        selectedSubjects.size == 1 -> {
            val subject = selectedSubjects.first()
            assignments.filter { it.subjectId == subject.id }.mapIndexed { index, assignment ->
                BarData(
                    point = Point(index.toFloat(), assignment.grade),
                    label = "Trabajo ${index + 1}",
                    color = Color(0xFF1976D2)
                )
            }
        }
        else -> {
            val allSubjects = MockData.subjects
            val subjectsToDisplay = if (selectedSubjects.isEmpty()) allSubjects else selectedSubjects

            subjectsToDisplay.mapIndexed { index, subject ->
                val subjectAssignments = assignments.filter { it.subjectId == subject.id }
                val average = if (subjectAssignments.isNotEmpty()) {
                    subjectAssignments.map { it.grade }.average().toFloat()
                } else {
                    0f
                }
                BarData(
                    point = Point(index.toFloat(), average),
                    label = subject.name,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }

    if (barData.isNotEmpty()) {
        val xAxisData = AxisData.Builder()
            .axisStepSize(100.dp)
            .steps((barData.size - 1).coerceAtLeast(0))
            .bottomPadding(40.dp)
            .labelData { index -> barData[index].label }
            .build()

        val yAxisData = AxisData.Builder()
            .steps(5)
            .labelAndAxisLinePadding(20.dp)
            .axisOffset(20.dp)
            .labelData { index -> (index * (5.0 / 5.0)).toFloat().toString() } // Assuming max grade is 5.0
            .build()

        val barChartData = BarChartData(
            chartData = barData,
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            barStyle = BarStyle(
                barWidth = 35.dp
            ),
            backgroundColor = Color.White
        )

        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            barChartData = barChartData
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay datos para mostrar")
        }
    }
}
