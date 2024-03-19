package com.example.composedatetimepicker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                            // Add reminder logic here
                        }

                        negativeButton(text = "Cancel") {
                            isReminderSet = false
                        }
                    }
                ) {
                    // Add content for reminder dialog here
                    Text(text = "Set reminder content")
                }
            }
        }
    }
}


