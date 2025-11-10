package com.example.studenthub.data.model

import java.util.Date

object MockData {
    val subjects = listOf(
        Subject(id = "1", name = "Math"),
        Subject(id = "2", name = "Chemistry"),
        Subject(id = "3", name = "Physics"),
        Subject(id = "4", name = "History")
    )

    val assignments = listOf(
        Assignment(id = "1", name = "Algebra Task", subjectId = "1", dueDate = Date(), grade = 4.5f),
        Assignment(id = "2", name = "Organic Chemistry", subjectId = "2", dueDate = Date(), grade = 3.8f),
        Assignment(id = "3", name = "Quantum Physics", subjectId = "3", dueDate = Date(), grade = 4.2f),
        Assignment(id = "4", name = "World War II", subjectId = "4", dueDate = Date(), grade = 4.9f),
        Assignment(id = "5", name = "Calculus Exam", subjectId = "1", dueDate = Date(), grade = 4.1f),
        Assignment(id = "6", name = "Inorganic Chemistry", subjectId = "2", dueDate = Date(), grade = 3.5f)
    )
}
