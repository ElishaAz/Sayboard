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

package org.vosk.ime;

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
import android.widget.Spinner;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class VoskIME extends InputMethodService implements
        RecognitionListener {

    static private final int STATE_START = 0;
    static private final int STATE_READY = 1;
    static private final int STATE_DONE = 2;
    static private final int STATE_ERROR = 3;
    static private final int STATE_MIC = 4;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private EditorInfo editorInfo;

    private int enterAction = EditorInfo.IME_ACTION_UNSPECIFIED;

    private TextView resultView;

    private int currentState = STATE_START;
    private String currentErrorMessage = "";
    private String loadedModel = "";

    private ConstraintLayout overlayView;

    private ImageButton micButton;
    private ImageView fabAnimation;
    private Button modelButton;

    private InputMethodManager mInputMethodManager;

    private List<org.vosk.ime.Model> models;
    private int currentModelIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        models = Tools.getInstalledModelsList(this);

        if (models.size() == 0) {
            setErrorState("No Model installed!");
        } else {
            currentModelIndex = 0;
            initModel(models.get(0));
        }

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

    private final Executor executor = Executors.newSingleThreadExecutor();

    private void initModel(org.vosk.ime.Model myModel) {
        loadedModel = myModel.locale.getDisplayName();
        if (modelButton != null)
            modelButton.setText(loadedModel);
        setUiState(STATE_START);

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Model model = new Model(myModel.path);
            handler.post(() -> {
                this.model = model;
                setUiState(STATE_READY);
            });
        });
//        StorageService.unpack(this, "model-en-us", "model",
//                (model) -> {
//                    this.model = model;
//                    setUiState(STATE_READY);
//                },
//                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }

    private void loadNextModel() {
        if (models.size() == 0) return;

        currentModelIndex++;
        if (currentModelIndex >= models.size())
            currentModelIndex = 0;
        initModel(models.get(currentModelIndex));
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
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        // text input has ended
    }

    @Override
    public View onCreateInputView() {
        // create view
        overlayView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.ime, null);

        resultView = overlayView.findViewById(R.id.result_text);
        micButton = overlayView.findViewById(R.id.mic_button);
        modelButton = overlayView.findViewById(R.id.model_button);

        overlayView.setMinHeight(convertDpToPixel(300));


        resultView.setMovementMethod(new ScrollingMovementMethod());

        // Setup layout
        setUiState(currentState);
        if (!currentErrorMessage.isEmpty()) {
            setErrorState(currentErrorMessage);
        }

//        overlayView.findViewById(R.id.recognize_file).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                recognizeFile();
//            }
//        });

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model != null)
                    recognizeMicrophone();
            }
        });

        micButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                InputMethodManager imeManager = (InputMethodManager)
                        getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                imeManager.showInputMethodPicker();
                return true;
            }
        });

        overlayView.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToLastIme();
            }
        });
        overlayView.findViewById(R.id.backspace_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLastChar();
            }
        });
        overlayView.findViewById(R.id.backspace_button).setOnTouchListener(new View.OnTouchListener() {
            private float initX, initY;
            private final float threshold = getResources().getDisplayMetrics().densityDpi / 6f;
            private final float charLen = getResources().getDisplayMetrics().densityDpi / 32f;

            private boolean swiping = false;
            private int lastAmount = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
        });
//        overlayView.findViewById(R.id.colon).
//
//                setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        appendSpecial(":");
//                    }
//                });
//        overlayView.findViewById(R.id.exclamation).
//
//                setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        appendSpecial("!");
//                    }
//                });
//        overlayView.findViewById(R.id.question).
//
//                setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        appendSpecial("?");
//                    }
//                });
//        overlayView.findViewById(R.id.comma).
//
//                setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        appendSpecial(",");
//                    }
//                });
//        overlayView.findViewById(R.id.dot).
//
//                setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        appendSpecial(".");
//                    }
//                });
        modelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showModelPicker();
                loadNextModel();
            }
        });
        modelButton.setText(loadedModel);

        overlayView.findViewById(R.id.return_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEnter();
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


        return overlayView;
    }

    private void showModelPicker() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.model_dialog_title);

        models = Tools.getInstalledModelsList(this);

        String[] names = new String[models.size()];
        for (int i = 0; i < models.size(); i++) {
            names[i] = models.get(i).locale.getDisplayLanguage();
        }

        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initModel(models.get(which));
            }
        });

// create and show the alert dialog
        AlertDialog dialog = builder.show();
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

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
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
            if (partialText.equals("") || partialText.equals("nun")) return; // wtf nun ?!
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
        setErrorState(e.getMessage());
    }

    @Override
    public void onTimeout() {
        setUiState(STATE_DONE);
    }

    private void setUiState(int state) {
        boolean enabled;
        int text;
        int icon;
        switch (state) {
            case STATE_START:
                text = R.string.mic_info_preparing;
                icon = R.drawable.ic_settings_voice;
                enabled = false;
                break;
            case STATE_READY:
            case STATE_DONE:
                text = R.string.mic_info_ready;
                icon = R.drawable.ic_mic_none;
                enabled = true;
                break;
            case STATE_MIC:
                text = R.string.mic_info_recording;
                icon = R.drawable.ic_mic;
                enabled = true;
                break;
            case STATE_ERROR:
                text = R.string.mic_info_error;
                icon = R.drawable.ic_mic_off;
                enabled = false;
                break;
            default:
                return;
        }
        if (resultView != null)
            resultView.setText(text);
        if (micButton != null) {
            micButton.setImageDrawable(AppCompatResources.getDrawable(this, icon));
            micButton.setEnabled(enabled);
        }
        currentState = state;
    }

    private void setErrorState(String message) {
        setUiState(STATE_ERROR);
        if (resultView != null)
            resultView.setText(message);
        currentErrorMessage = message;
    }

    private void recognizeMicrophone() {
        if (speechService != null) {
            setUiState(STATE_DONE);
            speechService.stop();
            speechService = null;
        } else {
            setUiState(STATE_MIC);
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }


    private void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }


    public int convertDpToPixel(float dp) {
        return (int) (dp * (getResources().getDisplayMetrics().densityDpi / 160f));
    }
}
