package com.elishaazaria.sayboard.ime

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.Tools
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource
import com.elishaazaria.sayboard.ime.recognizers.providers.RecognizerSourceProvider
import com.elishaazaria.sayboard.ime.recognizers.providers.VoskLocalProvider
import com.elishaazaria.sayboard.ime.recognizers.providers.VoskServerProvider
import com.elishaazaria.sayboard.sayboardPreferenceModel
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.Locale

class ModelManager(private val ime: IME, private val viewManager: ViewManager) {
    private val prefs by sayboardPreferenceModel()
    private var speechService: MySpeechService? = null
    var isRunning = false
        private set
    private val sourceProviders: MutableList<RecognizerSourceProvider> = ArrayList()
    private var recognizerSources: MutableList<RecognizerSource> = ArrayList()
    private var currentRecognizerSourceIndex = 0
    private lateinit var currentRecognizerSource: RecognizerSource
    private val executor: Executor = Executors.newSingleThreadExecutor()
    fun initializeRecognizer() {
        if (recognizerSources.size == 0) return
        val onLoaded = Observer { r: RecognizerSource? ->
            if (prefs.logicListenImmediately.get()) {
                start() // execute after initialize
            }
        }
        currentRecognizerSource = recognizerSources[currentRecognizerSourceIndex]
        viewManager.recognizerNameLD.postValue(currentRecognizerSource.name)
        currentRecognizerSource.stateLD.observe(ime, viewManager)
        currentRecognizerSource.initialize(executor, onLoaded)
    }

    val currentRecognizerSourceLocale: Locale?
        get() = currentRecognizerSource.locale

    private fun stopRecognizerSource(freeRam: Boolean) {
        currentRecognizerSource.close(freeRam)
        currentRecognizerSource.stateLD.removeObserver(viewManager)
    }

    fun switchToNextRecognizer() {
        if (recognizerSources.size == 0 || recognizerSources.size == 1) return
        stop(true)
        currentRecognizerSourceIndex++
        if (currentRecognizerSourceIndex >= recognizerSources.size) {
            currentRecognizerSourceIndex = 0
        }
        initializeRecognizer()
        if (prefs.logicListenImmediately.get()) {
            start()
        }
    }

    fun start() {
        if (isRunning || speechService != null) {
            speechService!!.stop()
        }
        isRunning = true
        viewManager.stateLD.postValue(ViewManager.STATE_LISTENING)
        try {
            val recognizer = currentRecognizerSource.recognizer
            if (ActivityCompat.checkSelfPermission(
                    ime,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            speechService = MySpeechService(recognizer, recognizer.sampleRate)
            speechService!!.startListening(ime)
        } catch (e: IOException) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_mic_in_use)
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR)
        }
    }

    private var pausedState = false

    init {
        sourceProviders.add(VoskLocalProvider(ime))
        if (Tools.VOSK_SERVER_ENABLED) {
            sourceProviders.add(VoskServerProvider())
        }
        for (provider in sourceProviders) {
            provider.loadSources(recognizerSources)
        }
        if (recognizerSources.size == 0) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_no_recognizers)
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR)
        } else {
            currentRecognizerSourceIndex = 0
            initializeRecognizer()
        }
    }

    fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
            pausedState = checked
            if (checked) {
                viewManager.stateLD.postValue(ViewManager.STATE_PAUSED)
            } else {
                viewManager.stateLD.postValue(ViewManager.STATE_LISTENING)
            }
        } else {
            pausedState = false
        }
    }

    val isPaused: Boolean
        get() = pausedState && speechService != null

    fun stop(forceFreeRam: Boolean = false) {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        isRunning = false
        stopRecognizerSource(forceFreeRam || !prefs.logicKeepModelInRam.get())
    }

    fun onDestroy() {
        stop()
    }

    fun reloadModels() {
        val newModels: MutableList<RecognizerSource> = ArrayList()
        for (provider in sourceProviders) {
            provider.loadSources(newModels)
        }
        val currentModel = recognizerSources[currentRecognizerSourceIndex]
        recognizerSources = newModels
        currentRecognizerSourceIndex = newModels.indexOf(currentModel)
        if (currentRecognizerSourceIndex == -1) {
            currentRecognizerSourceIndex = 0
        }
    }
}
