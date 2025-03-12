package dev.eknath.chukchukreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(modifier: Modifier = Modifier) {
    Scaffold(topBar = {
//        TopAppBar(title = { Text("ChukChuk-Remainder") }
//        )
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val datePicker = rememberDatePickerState(
                initialSelectedDateMillis = (getTodayInMillisAt() + TimeUnit.DAYS.toMillis(61)),
                initialDisplayMode =  DisplayMode.Picker,
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
                showModeToggle = true,
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
                    BookingCountdown(bookableInfo.bookingOpenOn)
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

@Composable
fun BookingCountdown(targetTimeMillis: Long) {
    val timeLeft by produceState(calculateTimeLeft(targetTimeMillis), targetTimeMillis) {
        while (true) {
            value = calculateTimeLeft(targetTimeMillis)
            delay(1000L)
        }
    }

    AnimatedContent(
        targetState = timeLeft,
    ) { time ->
        Text(
            text = time,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
    }
}

private fun calculateTimeLeft(targetTimeMillis: Long): String {
    val now = System.currentTimeMillis()
    var diff = targetTimeMillis - now

    if (diff <= 0) return "Booking Open!"

    val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
    if (years > 0) return "$years year${if (years > 1) "s" else ""}"

    val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
    if (months > 0) return "$months month${if (months > 1) "s" else ""}"

    val days = TimeUnit.MILLISECONDS.toDays(diff)
    if (days > 0) return "$days day${if (days > 1) "s" else ""}"

    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    diff -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    diff -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)

    return return when {
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}, $minutes min, $seconds sec"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} " + "$seconds second${if (seconds > 1) "s" else ""}"
        else -> "$seconds second${if (seconds > 1) "s" else ""}"
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