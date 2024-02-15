package com.elishaazaria.sayboard.recognition.recognizers.providers

import com.elishaazaria.sayboard.data.InstalledModelReference
import com.elishaazaria.sayboard.recognition.recognizers.RecognizerSource

interface RecognizerSourceProvider {
    fun getInstalledModels(): Collection<InstalledModelReference>

    fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource?
}