package com.example.studenthub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.example.studenthub.utils.ReminderReceiver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()

        enableEdgeToEdge()
        setContent {
            STUDENTHUBTheme {
                AppNavigation()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "StudentHub Reminders"
            val descriptionText = "Notifications for assignment reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(ReminderReceiver.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    // Explain to the user why the permission is needed
                }
                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can now show notifications.
        } else {
            // Permission is denied.
        }
    }
}
