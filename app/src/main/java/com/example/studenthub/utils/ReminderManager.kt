package com.example.studenthub.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import java.util.Calendar

object ReminderManager {

    fun scheduleReminder(context: Context, id: String, title: String, message: String, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", id.hashCode())
        }

        // Schedule at the exact time
        scheduleAlarm(context, alarmManager, intent, timeInMillis, id.hashCode())

        // Schedule 1h before
        scheduleAlarm(context, alarmManager, intent, timeInMillis - 60 * 60 * 1000, id.hashCode() + 1)

        // Schedule 15 min before
        scheduleAlarm(context, alarmManager, intent, timeInMillis - 15 * 60 * 1000, id.hashCode() + 2)

        // Schedule 5 min before
        scheduleAlarm(context, alarmManager, intent, timeInMillis - 5 * 60 * 1000, id.hashCode() + 3)
    }

    private fun scheduleAlarm(context: Context, alarmManager: AlarmManager, intent: Intent, triggerTime: Long, requestCode: Int) {
        if (triggerTime <= System.currentTimeMillis()) return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                 alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
