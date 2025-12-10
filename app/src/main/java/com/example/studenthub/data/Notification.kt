package com.example.studenthub.data

import java.util.Date

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Date = Date()
)
