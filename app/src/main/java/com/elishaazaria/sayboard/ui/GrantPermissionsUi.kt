package com.elishaazaria.sayboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
public fun GrantPermissionUi(
    mic: State<Boolean>,
    ime: State<Boolean>,
    requestMic: () -> Unit,
    requestIme: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(onClick = requestMic, enabled = !mic.value) {
            if (mic.value) {
                Text(text = "Microphone Permission Granted")
            } else {
                Text(text = "Grant Microphone Permission")
            }

        }
        Button(onClick = requestIme, enabled = !ime.value) {
            if (ime.value) {
                Text(text = "IME enabled")
            } else {
                Text(text = "Enable IME")
            }
        }
    }
}