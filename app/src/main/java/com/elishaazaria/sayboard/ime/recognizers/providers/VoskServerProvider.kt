package com.elishaazaria.sayboard.ime.recognizers.providers

import com.elishaazaria.sayboard.data.InstalledModelReference
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource

class VoskServerProvider : RecognizerSourceProvider {
    override fun getInstalledModels(): List<InstalledModelReference> {
        TODO("Not yet implemented")
    }

    override fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource? {
        TODO("Not yet implemented")
    }
}