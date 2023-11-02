package com.elishaazaria.sayboard.ime.recognizers.providers

import com.elishaazaria.sayboard.data.InstalledModelReference
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource

interface RecognizerSourceProvider {
    fun getInstalledModels(): Collection<InstalledModelReference>

    fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource?
}