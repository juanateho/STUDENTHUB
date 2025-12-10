package com.example.studenthub.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.studenthub.MainActivity
import com.example.studenthub.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Random
import java.util.UUID

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a reminder!"
        val notificationId = intent.getIntExtra("id", Random().nextInt())
        val userId = Firebase.auth.currentUser?.uid

        // We must have a user to save the notification
        if (userId == null) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            saveNotificationToFirestore(userId, title, message)
        }
    }

    private fun saveNotificationToFirestore(userId: String, title: String, message: String) {
        val db = Firebase.firestore
        val notificationData = hashMapOf(
            "id" to UUID.randomUUID().toString(),
            "userId" to userId,
            "title" to title,
            "message" to message,
            "timestamp" to Date()
        )
        db.collection("notifications").add(notificationData)
    }

    companion object {
        const val CHANNEL_ID = "studenthub_reminders"
    }
}
