package com.elishaazaria.sayboard.ime.recognizers.providers

import com.elishaazaria.sayboard.Tools.getInstalledModelsList
import com.elishaazaria.sayboard.ime.IME
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource
import com.elishaazaria.sayboard.ime.recognizers.VoskLocal

class VoskLocalProvider(private val ime: IME) : RecognizerSourceProvider {
    override fun loadSources(recognizerSources: MutableList<RecognizerSource>) {
        for (localModel in getInstalledModelsList(ime)) {
            recognizerSources.add(VoskLocal(localModel))
        }
    }
}