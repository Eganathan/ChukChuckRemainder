package dev.eknath.chukchukreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(modifier: Modifier = Modifier) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("ChukChuk-Remainder") })
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val datePicker = rememberDatePickerState()
            val bookingStartsOn by remember {
                derivedStateOf {
                    (datePicker.selectedDateMillis ?: 0L) - TimeUnit.DAYS.toMillis(60)
                }
            }

            DatePicker(
                state = datePicker,
                showModeToggle = false,
                title = { Text(" select the train start date:") },
            )


            val bookingStartDate = bookingStartsOn.takeIf { it > 0L }?.let { millis ->
                SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).format(Date(millis))
            } ?: "No date selected"

            val selectedDateMillis = datePicker.selectedDateMillis ?: System.currentTimeMillis()
            val newDateMillis = selectedDateMillis - TimeUnit.DAYS.toMillis(60)

            Spacer(modifier = Modifier.height(10.dp))

            if (bookingStartsOn > 0L)
                Card(
                    modifier = Modifier.padding(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Green)
                ) {
                    Text(
                        text = "Booking Starts on: $bookingStartDate at 08:30 am",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
        }
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