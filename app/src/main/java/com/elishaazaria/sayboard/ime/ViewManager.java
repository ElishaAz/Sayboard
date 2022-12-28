package com.elishaazaria.sayboard.ime;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.ime.recognizers.RecognizerState;
import com.elishaazaria.sayboard.preferences.UIPreferences;

public class ViewManager implements Observer<RecognizerState> {
    private final IME ime;

    public static final int STATE_INITIAL = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_READY = 2; // model loaded, ready to start
    public static final int STATE_LISTENING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_ERROR = 5;

    private ConstraintLayout overlayView;
    private ImageButton micButton;
    private ImageButton backButton;
    private ImageButton backspaceButton;
    private ImageButton returnButton;
    private Button modelButton;
    private TextView resultView;

    public final MutableLiveData<Integer> stateLD = new MutableLiveData<>(STATE_INITIAL);
    public final MutableLiveData<Integer> errorMessageLD = new MutableLiveData<>(0);
    public final MutableLiveData<String> recognizerNameLD = new MutableLiveData<>("");

    private Listener listener;

    public ViewManager(IME ime) {
        this.ime = ime;

        stateLD.observe(ime, this::observeState);
        errorMessageLD.observe(ime, this::observeError);
        recognizerNameLD.observe(ime, this::observeRecognizerName);
    }

    public void init() {
        initializeVariables();

        reloadOrientation();

        setUpListeners();


        currentForeground = Integer.MAX_VALUE;
        currentBackground = Integer.MAX_VALUE;
        setUpTheme();
    }

    @SuppressLint("InflateParams")
    private void initializeVariables() {
        overlayView = (ConstraintLayout) ime.getLayoutInflater().inflate(R.layout.ime, null);
        resultView = overlayView.findViewById(R.id.result_text);
        micButton = overlayView.findViewById(R.id.mic_button);
        backButton = overlayView.findViewById(R.id.back_button);
        backspaceButton = overlayView.findViewById(R.id.backspace_button);
        modelButton = overlayView.findViewById(R.id.model_button);
        returnButton = overlayView.findViewById(R.id.return_button);

        resultView.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setUpListeners() {
        micButton.setOnClickListener(v -> {
            if (listener != null) listener.micClick();
        });
        micButton.setOnLongClickListener(v -> listener != null && listener.micLongClick());
        backButton.setOnClickListener(v -> {
            if (listener != null) listener.backClicked();
        });
        backspaceButton.setOnClickListener(v -> {
            if (listener != null) listener.backspaceClicked();
        });
        backspaceButton.setOnTouchListener((v, event) -> {
            if (listener == null) {
                v.performClick();
                return false;
            }
            return listener.backspaceTouched(v, event);
        });
        returnButton.setOnClickListener(v -> {
            if (listener != null) listener.returnClicked();
        });
        modelButton.setOnClickListener(v -> {
            if (listener != null) listener.modelClicked();
        });
    }

    private int currentForeground = Integer.MAX_VALUE;
    private int currentBackground = Integer.MAX_VALUE;

    private void setUpTheme() {
        boolean dark = (ime.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int foreground = UIPreferences.getForegroundColor(dark, ime);
        int background = UIPreferences.getBackgroundColor(dark);

        if (currentForeground == foreground && currentBackground == background) return;

        currentForeground = foreground;
        currentBackground = background;

        overlayView.setBackgroundColor(background);

        ColorStateList foregroundTint = ColorStateList.valueOf(foreground);
        micButton.setImageTintList(foregroundTint);
        backButton.setImageTintList(foregroundTint);
        backspaceButton.setImageTintList(foregroundTint);
        returnButton.setImageTintList(foregroundTint);
        TextViewCompat.setCompoundDrawableTintList(modelButton, foregroundTint);
        modelButton.setTextColor(foreground);
        resultView.setTextColor(foreground);
    }

    private void reloadOrientation() {
        boolean landscape = ime.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        Window window = ime.getMyWindow();
        if (window == null) return;

        int screenHeight = ime.getResources().getDisplayMetrics().heightPixels;

        float percent;
        if (landscape) {
            percent = UIPreferences.getScreenHeightLandscape();
        } else {
            percent = UIPreferences.getScreenHeightPortrait();
        }
        int height = (int) (percent * screenHeight);


        Log.d("ViewManager", "Screen height: " + screenHeight + ", height: " + height);

//        WindowManager.LayoutParams params = window.getAttributes();
//        params.height = height;
//        window.setAttributes(params);
//
        overlayView.setMinHeight(height);
        overlayView.setMaxHeight(height);
//        overlayView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
    }

    public void refresh() {
        setUpTheme();

        reloadOrientation();
    }

    private void observeState(int state) {
        boolean enabled;
        int text;
        int icon;
        switch (state) {
            case STATE_INITIAL:
            case STATE_LOADING:
                text = R.string.mic_info_preparing;
                icon = R.drawable.ic_settings_voice;
                enabled = false;
                break;
            case STATE_READY:
            case STATE_PAUSED:
                text = R.string.mic_info_ready;
                icon = R.drawable.ic_mic_none;
                enabled = true;
                break;
            case STATE_LISTENING:
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
        resultView.setText(text);
        micButton.setImageDrawable(AppCompatResources.getDrawable(ime, icon));
        micButton.setEnabled(enabled);
    }

    private void observeError(int messageId) {
        if (messageId == 0) return;
        resultView.setText(ime.getText(messageId));
    }

    public void observeRecognizerName(String name) {
        modelButton.setText(name);
    }

    public ConstraintLayout getRoot() {
        return overlayView;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void micClick();

        boolean micLongClick();

        void backClicked();

        void backspaceClicked();

        boolean backspaceTouched(View v, MotionEvent event);

        void returnClicked();

        void modelClicked();
    }

    @Override
    public void onChanged(RecognizerState recognizerState) {
        switch (recognizerState) {
            case CLOSED:
            case NONE:
                stateLD.setValue(STATE_INITIAL);
                break;
            case LOADING:
                stateLD.setValue(STATE_LOADING);
                break;
            case READY:
                stateLD.setValue(STATE_READY);
                break;
            case IN_RAM:
                stateLD.setValue(STATE_PAUSED);
                break;
            case ERROR:
                stateLD.setValue(STATE_ERROR);
                break;
        }
    }

    private int convertDpToPixel(float dp) {
        return (int) (dp * (ime.getResources().getDisplayMetrics().densityDpi / 160f));
    }
}
