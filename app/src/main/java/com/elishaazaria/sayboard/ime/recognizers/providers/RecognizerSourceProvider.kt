package com.elishaazaria.sayboard.ime.recognizers.providers

import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource

interface RecognizerSourceProvider {
    fun loadSources(recognizerSources: MutableList<RecognizerSource>)
}