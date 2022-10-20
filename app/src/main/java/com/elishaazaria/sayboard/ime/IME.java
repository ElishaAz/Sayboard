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
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.Tools;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class IME extends InputMethodService implements
        RecognitionListener {

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private EditorInfo editorInfo;
    private int enterAction = EditorInfo.IME_ACTION_UNSPECIFIED;

    private ViewManager viewManager;
    private ModelManager modelManager;

    private InputMethodManager mInputMethodManager;

    @Override
    public void onCreate() {
        super.onCreate();

        viewManager = new ViewManager(this);

        modelManager = new ModelManager(this, viewManager);


        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    @Override
    public void onInitializeInterface() {
//        super.onCreate(state);
//        setContentView(R.layout.main);
//
//        // Setup layout
//        resultView = findViewById(R.id.result_text);
//        setUiState(STATE_START);
//
//        findViewById(R.id.recognize_file).setOnClickListener(view -> recognizeFile());
//        findViewById(R.id.recognize_mic).setOnClickListener(view -> recognizeMicrophone());
//        ((ToggleButton) findViewById(R.id.pause)).setOnCheckedChangeListener((view, isChecked) -> pause(isChecked));

        LibVosk.setLogLevel(LogLevel.INFO);

        // Check if user has given permission to record audio, init the model after permission is granted
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // TODO: error => has to open settings first
            return;
        }
    }

    @Override
    public void onBindInput() {
        // when user first clicks e.g. in text field
    }

    private static final int[] editorActions = new int[]
            {
                    EditorInfo.IME_ACTION_UNSPECIFIED,
                    EditorInfo.IME_ACTION_NONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH,
                    EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_DONE,
                    EditorInfo.IME_ACTION_PREVIOUS,
            };

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        // text input has started
        this.editorInfo = info;

        int action = editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
        for (int a : editorActions) {
            if (action == a) {
                enterAction = action;
                break;
            }
        }

        modelManager.reloadModels();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        // text input has ended
    }

    @Override
    public View onCreateInputView() {
        viewManager.init();

        viewManager.setListener(new ViewManager.Listener() {
            @Override
            public void micClick() {
                if (modelManager.isRunning()) {
                    modelManager.stop();
                    viewManager.setUiState(ViewManager.STATE_DONE);
                } else {
                    modelManager.start();
                }
            }

            @Override
            public boolean micLongClick() {
                InputMethodManager imeManager = (InputMethodManager)
                        getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                imeManager.showInputMethodPicker();
                return true;
            }

            @Override
            public void backClicked() {
                switchToLastIme();
            }

            @Override
            public void backspaceClicked() {
                deleteLastChar();
            }

            private float initX, initY;
            private final float threshold = getResources().getDisplayMetrics().densityDpi / 6f;
            private final float charLen = getResources().getDisplayMetrics().densityDpi / 32f;

            private boolean swiping = false;
            private int lastAmount = 0;

            @Override
            public boolean backspaceTouched(View v, MotionEvent event) {
                float x = event.getX() - initX;
                float y = event.getY() - initY;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = event.getX();
                        initY = event.getY();
                        swiping = false;
                        lastAmount = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (x < -threshold) {
                            swiping = true;
                        }

                        if (swiping) {
                            x = -x; // x is negative
                            int amount = Math.round((x - threshold) / charLen);
                            selectCharsBack(amount);
                            lastAmount = amount;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (swiping) {
                            deleteSelection();
                        } else {
                            v.performClick();
                        }
                        break;
                }
                return true;
            }

            @Override
            public void returnClicked() {
                sendEnter();
            }

            @Override
            public void modelClicked() {
                modelManager.loadNextModel();
            }
        });

        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
            if (et != null) {
                selectionStart = et.selectionStart;
                selectionEnd = et.selectionEnd;
            } else {
                selectionStart = 0;
                selectionEnd = 0;
            }
        }


        return viewManager.getRoot();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        selectionStart = newSelStart;
        selectionEnd = newSelEnd;
        Log.d("VoskIME", "selection update: " + selectionStart + ", " + selectionEnd);
    }


    private int selectionStart = 0;
    private int selectionEnd = 0;

    private void selectCharsBack(int chars) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        int start = selectionEnd - chars;
        if (start < 0) start = 0;
        ic.setSelection(start, selectionEnd);
    }

    private void deleteSelection() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText("", 1);
    }

    private void deleteLastChar() {
        // delete last char
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence selectedChars = ic.getSelectedText(0);
        if (selectedChars == null) {
            ic.deleteSurroundingText(1, 0);
        } else if (selectedChars.toString().isEmpty()) {
            ic.deleteSurroundingText(1, 0);
        } else {
            ic.performContextMenuAction(android.R.id.cut);
        }
    }

    private void sendEnter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        if (enterAction == EditorInfo.IME_ACTION_UNSPECIFIED) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
        } else {
            ic.performEditorAction(enterAction);
        }
    }

    private void appendSpecial(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText(text, 1);
    }

    /**
     * Switch to the previous IME, either when the user tries to edit an unsupported field (e.g. password),
     * or when they explicitly want to be taken back to the previous IME e.g. in case of a one-shot
     * speech input.
     */
    private void switchToLastIme() {
        boolean result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            result = switchToPreviousInputMethod();
        } else {
            result = mInputMethodManager.switchToLastInputMethod(getToken());
        }
        if (!result) {
            Toast.makeText(this, "No Previous IME", Toast.LENGTH_SHORT).show();
        }
    }

    private IBinder getToken() {
        Window window = getMyWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private Window getMyWindow() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        return dialog.getWindow();
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Recognizer initialization is a time-consuming and it involves IO,
//                // so we execute it in async task
//                initModel();
//            } else {
//                finish();
//            }
//        }
//    }

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
            if (ic != null)
                ic.commitText(" " + finalText, 1);
        } catch (Exception e) {
            System.out.println("ERROR: Json parse exception");
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
//        resultView.append(hypothesis + "\n");
//        setUiState(STATE_DONE);
//        if (speechStreamService != null) {
//            speechStreamService = null;
//        }
        onResult(hypothesis);
    }

    @Override
    public void onPartialResult(String hypothesis) {
//        resultView.append(hypothesis + "\n");
        Log.d("VoskIME", "Partial result: " + hypothesis);
        try {
            JSONObject partialResult = new JSONObject(hypothesis);
            String partialText = partialResult.getString("partial").trim();
            if (partialText.equals("") || partialText.equals("nun")) return;
//            resultView.setText(partialText);
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            String lastChar = ic.getTextBeforeCursor(1, 0).toString();
            if (lastChar != null) { // do not append two words without space
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
        viewManager.setUiState(ViewManager.STATE_DONE);
    }


    private void pause(boolean checked) {
        modelManager.pause(checked);
    }
}
