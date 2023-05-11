/*
 * org.vosk.SpeechService, extended to support other recognizers.
 */
package com.elishaazaria.sayboard.ime

import android.Manifest
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.elishaazaria.sayboard.ime.recognizers.Recognizer
import org.vosk.android.RecognitionListener
import java.io.IOException

class MySpeechService @RequiresPermission(Manifest.permission.RECORD_AUDIO) constructor(
    private val recognizer: Recognizer, sampleRate: Float
) {
    private val sampleRate: Int
    private val bufferSize: Int
    private val recorder: AudioRecord
    private var recognizerThread: RecognizerThread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        this.sampleRate = sampleRate.toInt()
        bufferSize = Math.round(this.sampleRate.toFloat() * BUFFER_SIZE_SECONDS)
        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            this.sampleRate,
            16,
            2,
            bufferSize * 2
        )
        if (recorder.state == 0) {
            recorder.release()
            throw IOException("Failed to initialize recorder. Microphone might be already in use.")
        }
    }

    fun startListening(listener: RecognitionListener): Boolean {
        return if (null != recognizerThread) {
            false
        } else {
            recognizerThread =
                RecognizerThread(listener)
            recognizerThread!!.start()
            true
        }
    }

    fun startListening(listener: RecognitionListener, timeout: Int): Boolean {
        return if (null != recognizerThread) {
            false
        } else {
            recognizerThread =
                RecognizerThread(listener, timeout)
            recognizerThread!!.start()
            true
        }
    }

    private fun stopRecognizerThread(): Boolean {
        return if (null == recognizerThread) {
            false
        } else {
            try {
                recognizerThread!!.interrupt()
                recognizerThread!!.join()
            } catch (var2: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            recognizerThread = null
            true
        }
    }

    fun stop(): Boolean {
        return stopRecognizerThread()
    }

    fun cancel(): Boolean {
        if (recognizerThread != null) {
            recognizerThread!!.setPause(true)
        }
        return stopRecognizerThread()
    }

    fun shutdown() {
        recorder.release()
    }

    fun setPause(paused: Boolean) {
        if (recognizerThread != null) {
            recognizerThread!!.setPause(paused)
        }
    }

    fun reset() {
        if (recognizerThread != null) {
            recognizerThread!!.reset()
        }
    }

    private inner class RecognizerThread @JvmOverloads constructor(
        var listener: RecognitionListener,
        timeout: Int = -1
    ) : Thread() {
        private var remainingSamples: Int
        private val timeoutSamples: Int

        @Volatile
        private var paused = false

        @Volatile
        private var reset = false

        init {
            if (timeout != -1) {
                timeoutSamples = timeout * sampleRate / 1000
            } else {
                timeoutSamples = -1
            }
            remainingSamples = timeoutSamples
        }

        fun setPause(paused: Boolean) {
            this.paused = paused
        }

        fun reset() {
            reset = true
        }

        override fun run() {
            recorder.startRecording()
            if (recorder.recordingState == 1) {
                recorder.stop()
                val ioe =
                    IOException("Failed to start recording. Microphone might be already in use.")
                mainHandler.post { listener.onError(ioe) }
            }
            val buffer = ShortArray(bufferSize)
            while (!interrupted() && (timeoutSamples == -1 || remainingSamples > 0)) {
                val nread = recorder.read(buffer, 0, buffer.size)
                if (!paused) {
                    if (reset) {
                        recognizer.reset()
                        reset = false
                    }
                    if (nread < 0) {
                        throw RuntimeException("error reading audio buffer")
                    }
                    var result: String?
                    if (recognizer.acceptWaveForm(buffer, nread)) {
                        result = recognizer.getResult()
                        mainHandler.post { listener.onResult(result) }
                    } else {
                        result = recognizer.getPartialResult()
                        mainHandler.post { listener.onPartialResult(result) }
                    }
                    if (timeoutSamples != -1) {
                        remainingSamples -= nread
                    }
                }
            }
            recorder.stop()
            if (!paused) {
                if (timeoutSamples != -1 && remainingSamples <= 0) {
                    mainHandler.post { listener.onTimeout() }
                } else {
                    val finalResult = recognizer.getFinalResult()
                    mainHandler.post { listener.onFinalResult(finalResult) }
                }
            }
        }
    }

    companion object {
        private const val NO_TIMEOUT = -1
        private const val BUFFER_SIZE_SECONDS = 0.2f
    }
}