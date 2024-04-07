package com.example.composedatetimepicker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {
    companion object {
        private const val CHANNEL_ID = "my_notification_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the notification channel
        createNotificationChannel()

        setContent {
            val navController = rememberNavController()
            val alarms = remember { mutableStateListOf<String>() }

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainActivityContent(navController = navController, alarms = alarms)
                }
                composable("reminder") {
                    ReminderView(navController = navController, alarms = alarms)
                }
            }
        }
    }

    @Composable
    fun MainActivityContent(navController: NavHostController, alarms: MutableList<String>) {
        var isDateTimeSelected by remember { mutableStateOf(false) }
        var isReminderSet by remember { mutableStateOf(false) }
        var description by remember { mutableStateOf("") }

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val lifecycle = lifecycleOwner.lifecycle

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
                    .ofPattern("hh:mm a")
                    .format(pickedTime)
            }
        }

        val dateDialogState = rememberMaterialDialogState()
        val timeDialogState = rememberMaterialDialogState()
        val reminderDialogState = rememberMaterialDialogState()

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

            // Navigation button always visible
            Button(
                onClick = {
                    navController.navigate("reminder")
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(text = "View Reminders")
            }

            if (isDateTimeSelected) {
                Button(
                    onClick = {
                        reminderDialogState.show()

                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
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
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Text(text = "Set a reminder")
                        }
                    }
                    // AlarmList(alarms = alarms.toMutableList())

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
                    val newAlarm =
                        "$formattedDate at $formattedTime" // Create new alarm string
                    val alarmDateTime = LocalDateTime.of(
                        pickedDate,
                        pickedTime
                    ) // Combine date and time into LocalDateTime
                    alarms.add(newAlarm) // Add alarm to the list
                    isDateTimeSelected = false // Reset flag

                    createNotification(context, newAlarm)

                    // Start countdown timer for the reminder
                    lifecycle.coroutineScope.launch {
                        startCountdown(context, alarmDateTime, description)
                        Toast.makeText(context, "Alarm set for $formattedDate at $formattedTime", Toast.LENGTH_SHORT).show()

                    }
                }

                negativeButton(text = "Cancel") {
                    isReminderSet = false
                    isDateTimeSelected = false // Reset flag
                }
            }
        ) {
            Column {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Enter description") }
                )
            }
        }
    }

    @Composable
    fun ReminderView(navController: NavHostController, alarms: List<String>) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display alarms in a list
            AlarmList(alarms = alarms)


            // Add any other content specific to the ReminderView
            // For example, you can add a button to navigate back to the main screen

        }
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .padding(top = 32.dp, start = 32.dp)

        ) {
            Text(text = "Go Back")
        }
    }

    private fun createNotification(context: Context, newAlarm: String) {
        // Create and send the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 2 // Unique ID for the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Your reminder has been created")
            .setContentText("You have set a reminder for $newAlarm.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun createSecondNotification(context: Context, description: String) {
        // Create and send the second notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 2 // Unique ID for the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Timed reminder")
            .setContentText("Your timed reminder is here: $description.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun startCountdown(
        context: Context,
        alarmTime: LocalDateTime,
        description: String
    ) {
        val currentDateTime = LocalDateTime.now()
        val delayMillis = calculateDelayMillis(currentDateTime, alarmTime)

        // Launch a coroutine to delay execution
        lifecycle.coroutineScope.launch {
            delay(delayMillis)
            // Trigger second notification after countdown
            createSecondNotification(context, description)
        }
    }

    private fun calculateDelayMillis(
        currentDateTime: LocalDateTime,
        alarmTime: LocalDateTime
    ): Long {
        val currentMillis = currentDateTime.toInstant().toEpochMilli()
        val alarmMillis = alarmTime.toInstant().toEpochMilli()
        return alarmMillis - currentMillis
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
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun LocalDateTime.toInstant(): Instant {
        val zoneId = ZoneId.systemDefault() // You can change the zone ID as needed
        val zonedDateTime = this.atZone(zoneId)
        return zonedDateTime.toInstant()
    }
}

private fun <E> List<E>.add(newAlarm: E) {
    this.add(newAlarm)
}
