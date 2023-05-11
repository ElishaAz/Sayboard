package com.elishaazaria.sayboard.ime.recognizers.providers

import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource
import com.elishaazaria.sayboard.ime.recognizers.VoskServer
import com.elishaazaria.sayboard.preferences.ModelPreferences.voskServers

class VoskServerProvider : RecognizerSourceProvider {
    override fun loadSources(recognizerSources: MutableList<RecognizerSource>) {
        for (voskServer in voskServers) {
            recognizerSources.add(VoskServer(voskServer))
        }
    }
}