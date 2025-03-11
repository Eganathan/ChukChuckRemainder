package dev.eknath.chukchukreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                rescheduleReminders(context)
            }
            else -> {
                showReminderNotification(context, "Reminder Alert", "Time to check your task!")
            }
        }
    }

    private fun showReminderNotification(context: Context, title: String, message: String) {
        val channelId = "reminder_channel"
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Create Notification Channel (Only required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the app when clicking the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your actual icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Remove notification after clicking
            .setContentIntent(pendingIntent) // Opens app when clicked
            .build()

        notificationManager.notify(1, notification)
    }

    private fun rescheduleReminders(context: Context) {
        // TODO: Fetch stored reminders from SharedPreferences/Database and reschedule them using AlarmManager
    }
}