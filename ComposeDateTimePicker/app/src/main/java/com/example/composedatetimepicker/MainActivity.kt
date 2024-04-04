package com.example.composedatetimepicker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.example.composedatetimepicker.ui.theme.ComposeDateTimePickerTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the notification channel
        createNotificationChannel()

        setContent {
            var isDateTimeSelected by remember { mutableStateOf(false) }
            var isReminderSet by remember { mutableStateOf(false) }

            ComposeDateTimePickerTheme {
                var pickedDate by remember {
                    mutableStateOf(LocalDate.now())
                }
                var pickedTime by remember {
                    mutableStateOf(LocalTime.NOON)
                }
                val formattedDate by remember {
                    derivedStateOf {
                        DateTimeFormatter
                            .ofPattern("MMM dd yyyy")
                            .format(pickedDate)
                    }
                }
                val formattedTime by remember {
                    derivedStateOf {
                        DateTimeFormatter
                            .ofPattern("hh:mm a") //
                            .format(pickedTime)
                    }
                }

                val dateDialogState = rememberMaterialDialogState()
                val timeDialogState = rememberMaterialDialogState()
                val reminderDialogState = rememberMaterialDialogState()

                val alarms = remember { mutableStateListOf<String>() } // List to store alarms

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            dateDialogState.show()
                        }) {
                            Text(text = "Pick date")
                        }
                        Text(text = formattedDate)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            timeDialogState.show()
                        }) {
                            Text(text = "Pick time")
                        }
                        Text(text = formattedTime)
                    }

                    if (isDateTimeSelected) {
                        Button(
                            onClick = {
                                reminderDialogState.show()
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text(text = "Set a reminder")
                        }
                    }
                }

                MaterialDialog(
                    dialogState = dateDialogState,
                    buttons = {
                        positiveButton(text = "Ok") {
                            isDateTimeSelected = true
                        }
                        negativeButton(text = "Clear") {
                            pickedDate = LocalDate.now()
                            isDateTimeSelected = false
                        }
                        negativeButton(text = "Cancel")
                    }
                ) {
                    datepicker(
                        initialDate = LocalDate.now(),
                        title = ""
                    ) {
                        pickedDate = it
                    }
                }

                MaterialDialog(
                    dialogState = timeDialogState,
                    buttons = {
                        positiveButton(text = "Ok") {
                            isDateTimeSelected = true
                        }
                        negativeButton(text = "Clear") {
                            pickedTime = LocalTime.NOON
                            isDateTimeSelected = false
                        }
                        negativeButton(text = "Cancel")
                    }
                ) {
                    timepicker(
                        initialTime = LocalTime.NOON,
                        title = "Pick a time",
                        timeRange = LocalTime.MIDNIGHT..LocalTime.MAX
                    ) {
                        pickedTime = it
                    }
                }

                MaterialDialog(
                    dialogState = reminderDialogState,
                    buttons = {
                        positiveButton(text = "Set Reminder") {
                            isReminderSet = true
                            val newAlarm = "$formattedDate at $formattedTime" // Create new alarm string
                            alarms.add(newAlarm) // Add alarm to the list
                            isDateTimeSelected = false // Reset flag

                            // Create and send the notification
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val notificationId = 1 // Unique ID for the notification
                            val notification = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Reminder Set")
                                .setContentText("You have set a reminder for $newAlarm")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .build()
                            notificationManager.notify(notificationId, notification)
                        }

                        negativeButton(text = "Cancel") {
                            isReminderSet = false
                            isDateTimeSelected = false // Reset flag
                        }
                    }
                ) {
                    // Add content for reminder dialog here
                    Text(text = "Set reminder content")
                }

                //Alarm cards
                AlarmList(alarms = alarms)
            }
        }
    }

    private fun createNotificationChannel() {
        // Check if the device is running Android 8.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Notification Channel"
            val descriptionText = "Channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channelId = "my_notification_channel" // Replace with your channel ID
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "my_notification_channel"
    }
}
