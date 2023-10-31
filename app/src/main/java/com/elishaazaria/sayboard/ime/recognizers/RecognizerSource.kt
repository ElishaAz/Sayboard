package com.elishaazaria.sayboard.ime.recognizers

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.Executor
import java.util.*

interface RecognizerSource {
    fun initialize(executor: Executor, onLoaded: Observer<RecognizerSource?>)
    val recognizer: Recognizer
    fun close(freeRAM: Boolean)
    val stateLD: LiveData<RecognizerState>
    val locale: Locale?

    val closed: Boolean

    @get:StringRes
    val errorMessage: Int
    val name: String
}