package com.elishaazaria.sayboard.services

import android.content.Context
import android.content.ContextParams
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.elishaazaria.sayboard.recognition.ModelManager
import com.elishaazaria.sayboard.recognition.recognizers.RecognizerSource
import java.util.Locale


class SayboardRecognitionService : RecognitionService(), ModelManager.Listener {

    init {
        Log.d(TAG, "init")
    }

    private val modelManager: ModelManager = ModelManager(this, this)

    private var listener: Callback? = null

    private var lastPartialResult: String? = null

    /**************** RecognitionService functions ***************/

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        Log.d(TAG, "onStartListening")
        listener ?: return
        recognizerIntent ?: return
        this.listener = listener

        val locale = recognizerIntent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)
            ?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()

        val attributionContext: Context? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.createContext(
                ContextParams.Builder().setNextAttributionSource(listener.callingAttributionSource)
                    .build()
            )
        } else {
            null
        }

        modelManager.reloadModels()

        if (!modelManager.switchToRecognizerOfLocale(locale, true, attributionContext)) {
            Log.w(
                TAG,
                "Could not find a Model for locale '${locale.toLanguageTag()}'. Using default"
            )
            modelManager.initializeFirstLocale(true, attributionContext)
        }
    }

    override fun onCancel(listener: Callback?) {
        Log.d(TAG, "onCancel")
        this.listener = listener
        modelManager.stop(true)
    }

    override fun onStopListening(listener: Callback?) {
        Log.d(TAG, "onStopListening")
        this.listener = listener
        modelManager.stop(true)

        lastPartialResult?.let {
            onResult(it)
        }

        try {
            listener?.endOfSpeech()
        } catch (e: RemoteException) {
            Log.e(TAG, "Exception from caller", e)
        }
    }

    /************* ModelManager.Listener functions ***********/

    override fun onStateChanged(state: ModelManager.State) {
        if (state == ModelManager.State.STATE_LISTENING) {
            try {
                listener?.readyForSpeech(Bundle())
                listener?.beginningOfSpeech()
            } catch (e: RemoteException) {
                Log.e(TAG, "Exception from caller", e)
            }
        }
    }

    override fun onError(type: ModelManager.ErrorType) {
        Log.d(TAG, "onError: $type")
        try {
            listener?.error(
                when (type) {
                    ModelManager.ErrorType.MIC_IN_USE -> SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                    ModelManager.ErrorType.NO_RECOGNIZERS_INSTALLED -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
                    } else {
                        SpeechRecognizer.ERROR_CLIENT
                    }
                }
            )
        } catch (e: RemoteException) {
            Log.e(TAG, "Exception from caller", e)
        }
    }

    override fun onError(exception: Exception?) {
        Log.e(TAG, "onError", exception)
        try {
            listener?.error(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
        } catch (e: RemoteException) {
            Log.e(TAG, "Exception from caller", e)
        }
    }

    override fun onRecognizerSource(source: RecognizerSource) {
    }

    override fun onPartialResult(hypothesis: String?) {
        Log.d(TAG, "onPartialResult $hypothesis")
        lastPartialResult = hypothesis
        try {
            listener?.partialResults(Bundle().apply {
                putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(hypothesis))
            })
        } catch (e: RemoteException) {
            Log.e(TAG, "Exception from caller", e)
        }
    }

    override fun onResult(hypothesis: String?) {
        Log.d(TAG, "onResult $hypothesis")
        // Konele seems to assume that results -> end of speech, so call onFinalResult to clean up too.
        onFinalResult(hypothesis)
//        lastPartialResult = null
//        try {
//            listener?.results(Bundle().apply {
//                putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(hypothesis))
//            })
//        } catch (e: RemoteException) {
//            Log.e(TAG, "Exception from caller", e)
//        }
    }

    override fun onFinalResult(hypothesis: String?) {
        Log.d(TAG, "onFinalResult $hypothesis")
        lastPartialResult = null
        try {
            listener?.results(Bundle().apply {
                putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(hypothesis))
            })
            listener?.endOfSpeech()
        } catch (e: RemoteException) {
            Log.e(TAG, "Exception from caller", e)
        }
        modelManager.stop(true)
    }

    override fun onTimeout() {
        Log.d(TAG, "onTimeout")
        try {
            listener?.error(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
        } catch (e: RemoteException) {
            Log.e(TAG, "Exception from caller", e)
        }
    }

    companion object {
        private const val TAG = "SayboardRecognitionService"
    }
}