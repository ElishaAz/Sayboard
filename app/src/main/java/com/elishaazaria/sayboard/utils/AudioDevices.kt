package com.elishaazaria.sayboard.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

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