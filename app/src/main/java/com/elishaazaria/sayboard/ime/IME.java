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

package com.elishaazaria.sayboard.ime;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.android.RecognitionListener;

import com.elishaazaria.sayboard.BuildConfig;
import com.elishaazaria.sayboard.preferences.LogicPreferences;

import androidx.core.content.ContextCompat;

public class IME extends InputMethodService implements RecognitionListener {
    private EditorInfo editorInfo;

    private ViewManager viewManager;
    private ModelManager modelManager;
    private ActionManager actionManager;


    @Override
    public void onCreate() {
        super.onCreate();
        LibVosk.setLogLevel(BuildConfig.DEBUG ? LogLevel.INFO : LogLevel.WARNINGS);

        viewManager = new ViewManager(this);

        modelManager = new ModelManager(this, viewManager);

        actionManager = new ActionManager(this, viewManager);
    }

    @Override
    public void onInitializeInterface() {
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            actionManager.openSettings();
        }
    }

    @Override
    public void onBindInput() {
        // when user first clicks e.g. in text field
    }

    private static final int[] editorActions = new int[]{EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_NONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_PREVIOUS,};

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        // text input has started
        this.editorInfo = info;

        // get enter action
        int action = editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
        for (int a : editorActions) {
            if (action == a) {
                actionManager.setEnterAction(action);
                break;
            }
        }

        modelManager.reloadModels();

        if (modelManager.modelLoaded()) {
            if (LogicPreferences.isListenImmediately()) {
                modelManager.start();
            }
        } else {
            modelManager.loadModel();
        }

        viewManager.refresh();
        setKeepScreenOn(LogicPreferences.getKeepScreenAwake() == LogicPreferences.KEEP_SCREEN_AWAKE_WHEN_OPEN);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        // text input has ended
        setKeepScreenOn(false);
        modelManager.stop();

        if (LogicPreferences.isWeakRefModel()) {
            modelManager.unloadModel();
        }

        if (LogicPreferences.isAutoSwitchBack()) {
            // switch back
            actionManager.switchToLastIme(false);
        }
    }

    @Override
    public View onCreateInputView() {
        viewManager.init();

        viewManager.setListener(new ViewManager.Listener() {
            @Override
            public void micClick() {
                if (modelManager.isRunning()) {
                    if (modelManager.isPaused()) {
                        modelManager.pause(false);
                        if (LogicPreferences.getKeepScreenAwake() == LogicPreferences.KEEP_SCREEN_AWAKE_WHEN_LISTENING)
                            setKeepScreenOn(true);
                    } else {
                        modelManager.pause(true);
                        if (LogicPreferences.getKeepScreenAwake() == LogicPreferences.KEEP_SCREEN_AWAKE_WHEN_LISTENING)
                            setKeepScreenOn(false);
                    }
                } else {
                    modelManager.start();
                    if (LogicPreferences.getKeepScreenAwake() == LogicPreferences.KEEP_SCREEN_AWAKE_WHEN_LISTENING)
                        setKeepScreenOn(true);
                }
            }

            @Override
            public boolean micLongClick() {
                InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                imeManager.showInputMethodPicker();
                return true;
            }

            @Override
            public void backClicked() {
                actionManager.switchToLastIme(true);
            }

            @Override
            public void backspaceClicked() {
                actionManager.deleteLastChar();
            }

            private float initX, initY;
            private final float threshold = getResources().getDisplayMetrics().densityDpi / 6f;
            private final float charLen = getResources().getDisplayMetrics().densityDpi / 32f;

            private boolean swiping = false;

            @Override
            public boolean backspaceTouched(View v, MotionEvent event) {
                float x = event.getX() - initX;
                float y = event.getY() - initY;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = event.getX();
                        initY = event.getY();
                        swiping = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (x < -threshold) {
                            swiping = true;
                        }

                        if (swiping) {
                            x = -x; // x is negative
                            int amount = Math.round((x - threshold) / charLen);
                            actionManager.selectCharsBack(amount);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (swiping) {
                            actionManager.deleteSelection();
                        } else {
                            v.performClick();
                        }
                        break;
                }
                return true;
            }

            @Override
            public void returnClicked() {
                actionManager.sendEnter();
            }

            @Override
            public void modelClicked() {
                modelManager.loadNextModel();
            }
        });

        actionManager.onCreateInputView();

        return viewManager.getRoot();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        actionManager.updateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        viewManager.orientationChanged(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }


    public IBinder getToken() {
        Window window = getMyWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    public Window getMyWindow() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        return dialog.getWindow();
    }

    private void setKeepScreenOn(boolean keepScreenOn) {
        Window window = getMyWindow();
        if (window == null) return;

        if (keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        modelManager.onDestroy();
    }

    @Override
    public void onResult(String hypothesis) {
        Log.d("VoskIME", "Result: " + hypothesis);
//        resultView.append(hypothesis + "\n");
        try {
            JSONObject result = new JSONObject(hypothesis);
            String text = result.getString("text").trim();
            String finalText = text;
            if (finalText.equals("")) return;
            if ("punkt".equals(text)) {
                finalText = ".";
            }
//            resultView.setText(finalText);
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(" " + finalText, 1);
        } catch (Exception e) {
            System.out.println("ERROR: Json parse exception");
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        onResult(hypothesis);
    }

    @Override
    public void onPartialResult(String hypothesis) {
        Log.d("VoskIME", "Partial result: " + hypothesis);
        try {
            JSONObject partialResult = new JSONObject(hypothesis);
            String partialText = partialResult.getString("partial").trim();
            if (partialText.equals("") || partialText.equals("nun")) return;
//            resultView.setText(partialText);
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            String lastChar = ic.getTextBeforeCursor(1, 0).toString();
            if (lastChar.length() == 1) { // do not append two words without space
                if (!lastChar.equals(" ")) {
                    partialText = " " + partialText;
                }
            }
            ic.setComposingText(partialText, 1);
        } catch (Exception e) {
            System.out.println("ERROR: Json parse exception");
        }
    }

    @Override
    public void onError(Exception e) {
        viewManager.setErrorState(e.getMessage());
    }

    @Override
    public void onTimeout() {
        viewManager.setUiState(ViewManager.STATE_PAUSED);
    }
}
