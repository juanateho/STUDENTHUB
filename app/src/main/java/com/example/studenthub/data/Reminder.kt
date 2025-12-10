package com.example.studenthub.data

data class Reminder(
    val id: String = "",
    val title: String = "",
    val date: String = "", // Format: yyyy-MM-dd
    val time: String = "", // Format: HH:mm
    val userId: String = ""
)
