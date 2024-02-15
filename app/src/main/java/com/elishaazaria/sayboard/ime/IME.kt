// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.elishaazaria.sayboard.ime

import android.Manifest
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.core.app.ActivityCompat
import com.elishaazaria.sayboard.BuildConfig
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.KeepScreenAwakeMode
import com.elishaazaria.sayboard.recognition.ModelManager
import com.elishaazaria.sayboard.recognition.recognizers.RecognizerSource
import com.elishaazaria.sayboard.sayboardPreferenceModel
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.android.RecognitionListener
import kotlin.math.roundToInt

class IME : InputMethodService(), ModelManager.Listener {
    private val prefs by sayboardPreferenceModel()

    private var hasMicPermission: Boolean = false

    public val lifecycleOwner = IMELifecycleOwner()
    private lateinit var editorInfo: EditorInfo
    private lateinit var viewManager: ViewManager
    private lateinit var modelManager: ModelManager
    private lateinit var actionManager: ActionManager
    private lateinit var textManager: TextManager

    private var currentRecognizerSource: RecognizerSource? = null


    public var enterAction = EditorInfo.IME_ACTION_UNSPECIFIED
        private set

    var isRichTextEditor = true
        private set

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        LibVosk.setLogLevel(if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.WARNINGS)
        viewManager = ViewManager(this)
        actionManager = ActionManager(this, viewManager)
        checkMicrophonePermission()
        modelManager = ModelManager(this, this)
        textManager = TextManager(this, modelManager)

        modelManager.initializeFirstLocale(prefs.logicListenImmediately.get())
    }

    override fun onInitializeInterface() {
        checkMicrophonePermission()
    }

    override fun onBindInput() {
        // when user first clicks e.g. in text field
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        lifecycleOwner.onResume()
        checkMicrophonePermission()
        editorInfo = info
        enterAction = findEnterAction()
        viewManager.enterActionLD.postValue(enterAction)
        isRichTextEditor =
            editorInfo.inputType and InputType.TYPE_MASK_CLASS != EditorInfo.TYPE_NULL ||
                    editorInfo.initialSelStart >= 0 && editorInfo.initialSelEnd >= 0 // based on florisboard code
        modelManager.reloadModels()
        modelManager.initializeFirstLocale(prefs.logicListenImmediately.get())
        textManager.onResume()
        setKeepScreenOn(prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_OPEN)
    }

    private fun findEnterAction(): Int {
        val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        if (editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION == 0 && action in editorActions) {
            return action
        }

        return EditorInfo.IME_ACTION_UNSPECIFIED
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        lifecycleOwner.onPause()
        // text input has ended
        setKeepScreenOn(false)
        modelManager.stop()
        if (prefs.logicAutoSwitchBack.get()) {
            // switch back
            actionManager.switchToLastIme(false)
        }
    }

    override fun onCreateInputView(): View {
        lifecycleOwner.attachToDecorView(
            window?.window?.decorView
        )

        viewManager.setListener(object : ViewManager.Listener {
            override fun micClick() {
                if (!hasMicPermission || modelManager.openSettingsOnMic) {
                    // errors! open settings
                    actionManager.openSettings()
                } else if (modelManager.isRunning) {
                    if (modelManager.isPaused) {
                        modelManager.pause(false)
                        if (prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_LISTENING) setKeepScreenOn(
                            true
                        )
                    } else {
                        modelManager.pause(true)
                        if (prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_LISTENING) setKeepScreenOn(
                            false
                        )
                    }
                } else {
                    modelManager.start()
                    if (prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_LISTENING) setKeepScreenOn(
                        true
                    )
                }
            }

            override fun micLongClick(): Boolean {
                val imeManager =
                    applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imeManager.showInputMethodPicker()
                return true
            }

            override fun backClicked() {
                actionManager.switchToLastIme(true)
            }

            override fun backspaceClicked() {
                actionManager.deleteLastChar()
            }

            private var initX = 0f
            private var initY = 0f
            private val threshold = resources.displayMetrics.densityDpi / 6f
            private val charLen = resources.displayMetrics.densityDpi / 32f
            private var swiping = false
            private var restart = false

            override fun backspaceTouchStart(offset: Offset) {
                restart = true
                swiping = false
            }

            override fun backspaceTouched(change: PointerInputChange, dragAmount: Float) {
                if (restart) {
                    restart = false
                    initX = change.position.x
                    initY = change.position.y
                }

                var x = change.position.x - initX
                val y = change.position.y - initY

                Log.d("IME", "$x,$y")

                if (x < -threshold) {
                    swiping = true
                }
                if (swiping) {
                    x = -x // x is negative
                    val amount = ((x - threshold) / charLen).roundToInt()
                    actionManager.selectCharsBack(amount)
                }
            }

            override fun backspaceTouchEnd() {
                if (swiping) actionManager.deleteSelection()
            }

            override fun returnClicked() {
                actionManager.sendEnter()
            }

            override fun modelClicked() {
                modelManager.switchToNextRecognizer(prefs.logicListenImmediately.get())
            }

            override fun settingsClicked() {
                actionManager.openSettings()
            }

            override fun buttonClicked(text: String) {
                textManager.onText(text, TextManager.Mode.INSERT)
            }
        })
        actionManager.onCreateInputView()
        return viewManager
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        actionManager.updateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        textManager.onUpdateSelection(newSelStart, newSelEnd)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.onDestroy()
        modelManager.onDestroy()
    }

    val token: IBinder?
        get() {
            val window = myWindow ?: return null
            return window.attributes.token
        }
    val myWindow: Window?
        get() {
            val dialog = window ?: return null
            return dialog.window
        }

    private fun setKeepScreenOn(keepScreenOn: Boolean) {
        val window = myWindow ?: return
        if (keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    private fun checkMicrophonePermission() {
        hasMicPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasMicPermission) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_no_permission)
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR)
        }
    }

    override fun onResult(text: String) {
        Log.d("VoskIME", "Result: $text")
        if (text.isEmpty()) return
        textManager.onText(text, TextManager.Mode.STANDARD)
    }

    override fun onFinalResult(text: String) {
        Log.d("VoskIME", "Final result: $text")
        if (text.isEmpty()) return
        textManager.onText(text, TextManager.Mode.FINAL)
    }

    override fun onPartialResult(partialText: String) {
        Log.d("VoskIME", "Partial result: $partialText")
        if (partialText == "") return

        textManager.onText(partialText, TextManager.Mode.PARTIAL)
    }

    override fun onStateChanged(state: ModelManager.State) {
        if (state == ModelManager.State.STATE_STOPPED) {
            currentRecognizerSource?.stateLD?.removeObserver(viewManager)
        } else {

            viewManager.stateLD.postValue(
                when (state) {
                    ModelManager.State.STATE_INITIAL -> ViewManager.STATE_INITIAL
                    ModelManager.State.STATE_LOADING -> ViewManager.STATE_LOADING
                    ModelManager.State.STATE_READY -> ViewManager.STATE_READY
                    ModelManager.State.STATE_LISTENING -> ViewManager.STATE_LISTENING
                    ModelManager.State.STATE_PAUSED -> ViewManager.STATE_PAUSED
                    ModelManager.State.STATE_ERROR -> ViewManager.STATE_ERROR
                    else -> TODO()
                }
            )
        }
    }

    override fun onError(type: ModelManager.ErrorType) {
        viewManager.errorMessageLD.postValue(
            when (type) {
                ModelManager.ErrorType.MIC_IN_USE -> R.string.mic_error_mic_in_use
                ModelManager.ErrorType.NO_RECOGNIZERS_INSTALLED -> R.string.mic_error_no_recognizers
            }
        )
    }

    override fun onError(e: Exception) {
        viewManager.errorMessageLD.postValue(R.string.mic_error_recognizer_error)
        viewManager.stateLD.postValue(ViewManager.STATE_ERROR)
    }

    override fun onRecognizerSource(source: RecognizerSource) {
        currentRecognizerSource?.stateLD?.removeObserver(viewManager)
        currentRecognizerSource = source
        source.stateLD.observe(lifecycleOwner, viewManager)
    }

    override fun onTimeout() {
        viewManager.stateLD.postValue(ViewManager.STATE_PAUSED)
    }

    companion object {
        private val editorActions = intArrayOf(
            EditorInfo.IME_ACTION_UNSPECIFIED,
            EditorInfo.IME_ACTION_NONE,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_PREVIOUS
        )
    }
}