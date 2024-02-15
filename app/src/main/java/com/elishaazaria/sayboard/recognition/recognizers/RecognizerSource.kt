package com.elishaazaria.sayboard.recognition.recognizers

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.Locale
import java.util.concurrent.Executor

interface RecognizerSource {
    fun initialize(executor: Executor, onLoaded: Observer<RecognizerSource?>)
    val recognizer: Recognizer
    fun close(freeRAM: Boolean)
    val stateLD: LiveData<RecognizerState>
    
    val addSpaces: Boolean

    val closed: Boolean

    @get:StringRes
    val errorMessage: Int
    val name: String

    val locale: Locale
}