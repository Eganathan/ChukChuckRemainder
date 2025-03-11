package dev.eknath.chukchukreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {

        val datePicker = rememberDatePickerState()
        val bookingStartsOn by remember { derivedStateOf { (datePicker.selectedDateMillis ?: 0L) - TimeUnit.DAYS.toMillis(60) } }

        DatePicker(state = datePicker, showModeToggle = true)


        val bookingStartDate = bookingStartsOn.takeIf { it > 0L } ?.let { millis ->
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(Date(millis))
        } ?: "No date selected"

        val selectedDateMillis = datePicker.selectedDateMillis ?: System.currentTimeMillis()
        val newDateMillis = selectedDateMillis - TimeUnit.DAYS.toMillis(60)

        Text(text = "Booking Starts on: $bookingStartDate", style = MaterialTheme.typography.bodyLarge)
    }
}

// Notification with AlarmManager mode
fun scheduleReminder(context: Context, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Ensures alarm fires even in Doze Mode
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}