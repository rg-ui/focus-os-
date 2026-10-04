package com.focusos.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.focusos.app.MainActivity

object NotificationHelper {

    const val CHANNEL_CLASSES = "focus_os_classes"
    const val CHANNEL_DEADLINES = "focus_os_deadlines"
    const val CHANNEL_CHECKINS = "focus_os_checkins"
    const val CHANNEL_FOCUS = "focus_os_focus_timer"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val classChannel = NotificationChannel(
                CHANNEL_CLASSES,
                "Classes & Timetable",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for ITEP and IITM scheduled classes"
            }

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINES,
                "Academic Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Assignment submission and exam date alerts"
            }

            val checkinChannel = NotificationChannel(
                CHANNEL_CHECKINS,
                "Daily Brief & Evening Check-In",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning briefing and evening reflection prompts"
            }

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active Pomodoro / Deep Work timer notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(classChannel, deadlineChannel, checkinChannel, focusChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        content: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+
        }
    }
}
