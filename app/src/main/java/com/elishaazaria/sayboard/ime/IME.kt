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
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.elishaazaria.sayboard.BuildConfig
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.KeepScreenAwakeMode
import com.elishaazaria.sayboard.sayboardPreferenceModel
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.android.RecognitionListener

class IME : InputMethodService(), RecognitionListener, LifecycleOwner {
    private val prefs by sayboardPreferenceModel()

    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var editorInfo: EditorInfo
    private lateinit var viewManager: ViewManager
    private lateinit var modelManager: ModelManager
    private lateinit var actionManager: ActionManager
    private lateinit var textManager: TextManager
    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        LibVosk.setLogLevel(if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.WARNINGS)
        viewManager = ViewManager(this)
        actionManager = ActionManager(this, viewManager)
        checkMicrophonePermission()
        modelManager = ModelManager(this, viewManager)
        textManager = TextManager(this)
    }

    override fun onInitializeInterface() {
        checkMicrophonePermission()
    }

    override fun onBindInput() {
        // when user first clicks e.g. in text field
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        checkMicrophonePermission()

        // text input has started
        editorInfo = info

        // get enter action
        val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        for (a in editorActions) {
            if (action == a) {
                actionManager.setEnterAction(action)
                break
            }
        }
        modelManager.initializeRecognizer()
        viewManager.refresh()
        setKeepScreenOn(prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_OPEN)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        // text input has ended
        setKeepScreenOn(false)
        modelManager.stop()
        if (prefs.logicAutoSwitchBack.get()) {
            // switch back
            actionManager.switchToLastIme(false)
        }
    }

    override fun onCreateInputView(): View {
        viewManager.init()
        viewManager.setListener(object : ViewManager.Listener {
            override fun micClick() {
                if (modelManager.isRunning) {
                    if (modelManager.isPaused) {
                        modelManager.pause(false)
                        if (prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_LISTENING)
                            setKeepScreenOn(true)
                    } else {
                        modelManager.pause(true)
                        if (prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_LISTENING)
                            setKeepScreenOn(false)
                    }
                } else {
                    modelManager.start()
                    if (prefs.logicKeepScreenAwake.get() == KeepScreenAwakeMode.WHEN_LISTENING)
                        setKeepScreenOn(true)
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
            override fun backspaceTouched(v: View, event: MotionEvent): Boolean {
                var x = event.x - initX
                val y = event.y - initY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = event.x
                        initY = event.y
                        swiping = false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (x < -threshold) {
                            swiping = true
                        }
                        if (swiping) {
                            x = -x // x is negative
                            val amount = Math.round((x - threshold) / charLen)
                            actionManager.selectCharsBack(amount)
                        }
                    }

                    MotionEvent.ACTION_UP -> if (swiping) {
                        actionManager.deleteSelection()
                    } else {
                        v.performClick()
                    }
                }
                return true
            }

            override fun returnClicked() {
                actionManager.sendEnter()
            }

            override fun modelClicked() {
                modelManager.switchToNextRecognizer()
            }
        })
        actionManager.onCreateInputView()
        return viewManager.root
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
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        actionManager.updateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

//        viewManager.orientationChanged(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
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
        if (ActivityCompat.checkSelfPermission(
                this@IME,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this@IME, "Microphone permission is required!", Toast.LENGTH_SHORT)
                .show()
            actionManager.openSettings()
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
        //            resultView.setText(partialText);
        textManager.onText(partialText, TextManager.Mode.PARTIAL)
        //
//        InputConnection ic = getCurrentInputConnection();
//        if (ic == null) return;
//        String lastChar = ic.getTextBeforeCursor(1, 0).toString();
//        if (lastChar.length() == 1) { // do not append two words without space
//            if (!lastChar.equals(" ")) {
//                partialText = " " + partialText;
//            }
//        }
//        ic.setComposingText(partialText, 1);
    }

    override fun onError(e: Exception) {
        viewManager.errorMessageLD.postValue(R.string.mic_error_recognizer_error)
        viewManager.stateLD.postValue(ViewManager.STATE_ERROR)
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

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}