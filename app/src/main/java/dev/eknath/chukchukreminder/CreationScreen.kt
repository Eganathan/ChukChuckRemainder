package dev.eknath.chukchukreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
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
            val datePicker = rememberDatePickerState(
                initialSelectedDateMillis = (getTodayInMillisAt() + TimeUnit.DAYS.toMillis(61))
            )
            val bookableInfo by remember {
                derivedStateOf {
                    SelectedDateInfo(
                        requiredBookingDate = datePicker.selectedDateMillis ?: 0L,
                    )
                }
            }

            DatePicker(
                state = datePicker,
                showModeToggle = false,
                title = { Text(" select the train start date:") },
            )


            Spacer(modifier = Modifier.height(10.dp))

            if (bookableInfo.formattedDate != null)
                Card(
                    modifier = Modifier.padding(5.dp),
                ) {
                    Text(
                        text = if (bookableInfo.isBookableNow) {
                            "Already Bookable"
                        } else "Booking Starts on: ${bookableInfo.formattedDate} at 08:30 am",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
        }
    }
}

data class SelectedDateInfo(
    val requiredBookingDate: Long = getTodayInMillisAt() + TimeUnit.DAYS.toMillis(61)
) {
    val actualBookingDate: Long
        get() = resetTimeTo(requiredBookingDate)

    val bookingOpenOn: Long
        get() = actualBookingDate - TimeUnit.DAYS.toMillis(61)

    val isBookableNow: Boolean
        get() = (System.currentTimeMillis() >= bookingOpenOn)

    val formattedDate: String?
        get() = bookingOpenOn.takeIf { it > 0L }?.let { millis ->
            val bookingDate = Calendar.getInstance().apply { timeInMillis = millis }
            val today = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

            when {
                isSameDay(bookingDate, today) -> "Today"
                isSameDay(bookingDate, tomorrow) -> "Tomorrow"
                else -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(millis))
            }
        }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

private fun getTodayInMillisAt(hour: Int = 8, minute: Int = 30): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

private fun resetTimeTo(time: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
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