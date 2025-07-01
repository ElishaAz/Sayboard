package com.elishaazaria.sayboard.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector

object AudioDevices {
    private val validTypes = listOf(
        AudioDeviceInfo.TYPE_BUILTIN_MIC,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_BLE_HEADSET
    )

    fun validAudioDevices(context: Context): List<AudioDeviceInfo> {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).filter {
            it.isSource
                    && (it.channelCounts.isEmpty() || 1 in it.channelCounts)
                    && (it.sampleRates.isEmpty() || 16000 in it.sampleRates)
        }
    }
}

fun AudioDeviceInfo.toIcon() : ImageVector {
    return when (this.type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> Icons.Default.PhoneAndroid
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> Icons.Default.HeadsetMic
        AudioDeviceInfo.TYPE_BLE_HEADSET -> Icons.Default.Bluetooth
        AudioDeviceInfo.TYPE_USB_HEADSET -> Icons.Default.MicExternalOn
        AudioDeviceInfo.TYPE_TELEPHONY -> Icons.Default.Call
        else -> Icons.Default.MicNone
    }
}

fun AudioDeviceInfo.describe(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && address.isNotBlank()) {
        return "$productName ($address)"
    } else {
        return "$productName"
    }
}