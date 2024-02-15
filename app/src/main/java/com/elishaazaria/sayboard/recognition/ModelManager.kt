package com.elishaazaria.sayboard.recognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.InstalledModelReference
import com.elishaazaria.sayboard.ime.ViewManager
import com.elishaazaria.sayboard.recognition.recognizers.RecognizerSource
import com.elishaazaria.sayboard.recognition.recognizers.providers.Providers
import com.elishaazaria.sayboard.sayboardPreferenceModel
import org.vosk.android.RecognitionListener
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ModelManager(
    private val context: Context,
    private val listener: Listener
) {
    private val prefs by sayboardPreferenceModel()
    private var speechService: MySpeechService? = null
    var isRunning = false
        private set

    val openSettingsOnMic: Boolean
        get() = recognizerSources.size == 0

    private var recognizerSourceProviders = Providers(context)

    private var recognizerSourceModels: List<InstalledModelReference> = listOf()
    private var recognizerSources: MutableList<RecognizerSource> = ArrayList()
    private var currentRecognizerSourceIndex = 0
    private var currentRecognizerSource: RecognizerSource? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()


    init {
        reloadModels()
    }

    private fun initializeRecognizer(autoStart: Boolean, attributionContext: Context? = null) {
        if (recognizerSources.size == 0) {
            return
        }
        currentRecognizerSource = recognizerSources[currentRecognizerSourceIndex]
        listener.onRecognizerSource(currentRecognizerSource!!)

        val onLoaded = Observer { r: RecognizerSource? ->
            if (autoStart) {
                start(attributionContext) // execute after initialize
            }
        }
        currentRecognizerSource!!.initialize(executor, onLoaded)
    }

    val currentRecognizerSourceAddSpaces: Boolean
        get() = currentRecognizerSource?.addSpaces ?: true

    fun switchToNextRecognizer(autoStart: Boolean) {
        if (recognizerSources.size == 0 || recognizerSources.size == 1) return
        stop(true)
        currentRecognizerSourceIndex++
        if (currentRecognizerSourceIndex >= recognizerSources.size) {
            currentRecognizerSourceIndex = 0
        }
        initializeRecognizer(autoStart) // start is called after the recognizer is initialized
    }

    fun switchToRecognizerOfLocale(
        locale: Locale,
        autoStart: Boolean,
        attributionContext: Context? = null
    ): Boolean {
        var bestSource = -1
        var foundLanguage = false
        var foundCountry = false

        recognizerSources.forEachIndexed { index, recognizerSource ->
            if (recognizerSource.locale.language == locale.language) {
                if (recognizerSource.locale.country == locale.country) {
                    if (recognizerSource.locale.variant == locale.variant) {
                        // Same language, country, and variant
                        bestSource = index
                        foundLanguage = true
                        foundCountry = true
                        return@forEachIndexed
                    } else if (!foundCountry) {
                        // Same language and country, but not variant
                        bestSource = index
                        foundLanguage = true
                        foundCountry = true
                    }
                } else if (!foundLanguage) {
                    // Same language, but not country
                    foundLanguage = true
                    bestSource = index
                }
            } else if (recognizerSource.locale == Locale.ROOT && !foundLanguage && bestSource == -1) {
                // A root locale. Pick it if we didn't find anything.
                bestSource = index
            }
        }

        if (bestSource == -1) {
            return false
        }

        stop(true)
        currentRecognizerSourceIndex = bestSource

        initializeRecognizer(
            autoStart,
            attributionContext
        ) // start is called after the recognizer is initialized

        return true
    }

    fun initializeFirstLocale(autoStart: Boolean, attributionContext: Context? = null): Boolean {
        if (recognizerSources.size == 0) {
            listener.onError(ErrorType.NO_RECOGNIZERS_INSTALLED)
            listener.onStateChanged(State.STATE_ERROR)
            return false
        }

        currentRecognizerSourceIndex = 0
        initializeRecognizer(autoStart)
        return true
    }

    fun start(attributionContext: Context? = null) {
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
        listener.onStateChanged(State.STATE_LISTENING)
        try {
            val recognizer = currentRecognizerSource!!.recognizer
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            speechService = MySpeechService(recognizer, recognizer.sampleRate, attributionContext)
            speechService!!.startListening(listener)
        } catch (e: IOException) {
            listener.onError(ErrorType.MIC_IN_USE)
            listener.onStateChanged(State.STATE_ERROR)
        }
    }

    private var pausedState = false

    fun reloadModels() {

        // TODO: make sure we actually need this
//        val newModels = prefs.modelsOrder.get()
//        if (newModels == recognizerSourceModels) {
//            if (autoStart) {
//                if (currentRecognizerSource != null) {
//                    start()
//                }
//            }
//            return
//        }

        val newModels = prefs.modelsOrder.get()
        if (newModels == recognizerSourceModels)
            return

        recognizerSources.clear()
        recognizerSourceModels = newModels
        recognizerSourceModels.forEach { model ->
            recognizerSourceProviders.recognizerSourceForModel(model)?.let {
                recognizerSources.add(it)
            }
        }

        if (recognizerSources.size == 0) {
            listener.onError(ErrorType.NO_RECOGNIZERS_INSTALLED)
            listener.onStateChanged(State.STATE_ERROR)
        }
    }

    fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
            pausedState = checked
            if (checked) {
                listener.onStateChanged(State.STATE_PAUSED)
            } else {
                listener.onStateChanged(State.STATE_LISTENING)
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
        listener.onStateChanged(State.STATE_STOPPED)
    }

    fun onDestroy() {
        stop(true)
    }

    companion object {
        private const val TAG = "ModelManager"
    }

    interface Listener : RecognitionListener {
        fun onStateChanged(state: State)

        fun onError(type: ErrorType)

        fun onRecognizerSource(source: RecognizerSource)
    }

    enum class State {
        STATE_INITIAL, STATE_LOADING, STATE_READY, STATE_LISTENING, STATE_PAUSED, STATE_ERROR, STATE_STOPPED
    }

    enum class ErrorType {
        MIC_IN_USE, NO_RECOGNIZERS_INSTALLED
    }
}
