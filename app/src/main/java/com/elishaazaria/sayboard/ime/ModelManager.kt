package com.elishaazaria.sayboard.ime

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.InstalledModelReference
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource
import com.elishaazaria.sayboard.ime.recognizers.providers.Providers
import com.elishaazaria.sayboard.sayboardPreferenceModel
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ModelManager(
    private val ime: IME,
    private val viewManager: ViewManager
) {
    private val prefs by sayboardPreferenceModel()
    private var speechService: MySpeechService? = null
    var isRunning = false
        private set

    val openSettingsOnMic: Boolean
        get() = recognizerSources.size == 0

    private var recognizerSourceProviders = Providers(ime)

    private var recognizerSourceModels: List<InstalledModelReference> = listOf()
    private var recognizerSources: MutableList<RecognizerSource> = ArrayList()
    private var currentRecognizerSourceIndex = 0
    private var currentRecognizerSource: RecognizerSource? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private fun initializeRecognizer() {
        if (recognizerSources.size == 0) {
            return
        }
        val onLoaded = Observer { r: RecognizerSource? ->
            if (prefs.logicListenImmediately.get()) {
                start() // execute after initialize
            }
        }
        currentRecognizerSource = recognizerSources[currentRecognizerSourceIndex]
        viewManager.recognizerNameLD.postValue(currentRecognizerSource!!.name)
        currentRecognizerSource!!.stateLD.observe(ime.lifecycleOwner, viewManager)
        currentRecognizerSource!!.initialize(executor, onLoaded)
    }

    val currentRecognizerSourceAddSpaces: Boolean
        get() = currentRecognizerSource?.addSpaces ?: true

    fun switchToNextRecognizer() {
        if (recognizerSources.size == 0 || recognizerSources.size == 1) return
        stop(true)
        currentRecognizerSourceIndex++
        if (currentRecognizerSourceIndex >= recognizerSources.size) {
            currentRecognizerSourceIndex = 0
        }
        initializeRecognizer() // start is called after the recognizer is initialized
    }

    fun start() {
        if (currentRecognizerSource == null) {
            Log.w(
                TAG,
                "currentRecognizerSource is null!"
            )
            return
        }
        if (currentRecognizerSource!!.closed) {
            Log.w(
                TAG,
                "Trying to start a closed Recognizer Source: ${currentRecognizerSource!!.name}"
            )
            return
        }
        if (isRunning || speechService != null) {
            speechService!!.stop()
        }
        isRunning = true
        viewManager.stateLD.postValue(ViewManager.STATE_LISTENING)
        try {
            val recognizer = currentRecognizerSource!!.recognizer
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
        reloadModels()
    }

    private fun reloadModels() {
        val newModels = prefs.modelsOrder.get()
        if (newModels == recognizerSourceModels) {
            if (prefs.logicListenImmediately.get()) {
                if (currentRecognizerSource != null) {
                    start()
                }
            }
            return
        }

        recognizerSources.clear()
        recognizerSourceModels = newModels
        recognizerSourceModels.forEach { model ->
            recognizerSourceProviders.recognizerSourceForModel(model)?.let {
                recognizerSources.add(it)
            }
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
        speechService?.let {
            executor.execute {
                it.stop()
                it.shutdown()
            }
        }
        speechService = null
        isRunning = false
        stopRecognizerSource(forceFreeRam || !prefs.logicKeepModelInRam.get())
    }

    private fun stopRecognizerSource(freeRam: Boolean) {
        currentRecognizerSource?.let {
            executor.execute {
                it.close(freeRam)
            }
        }
        currentRecognizerSource?.stateLD?.removeObserver(viewManager)
    }

    fun onDestroy() {
        stop(true)
    }

    fun onResume() {
        reloadModels()
    }

    companion object {
        private const val TAG = "ModelManager"
    }
}
