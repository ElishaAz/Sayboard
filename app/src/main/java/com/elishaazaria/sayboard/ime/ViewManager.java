package com.elishaazaria.sayboard.ime;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.preferences.ThemePreferences;

public class ViewManager {
    private final IME ime;

    public static final int STATE_PREPARE = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_DONE = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_MIC = 4;

    private ConstraintLayout overlayView;
    private ImageButton micButton;
    private ImageButton backButton;
    private ImageButton backspaceButton;
    private ImageButton returnButton;
    private Button modelButton;
    private TextView resultView;

    private boolean initialized;

    private int currentState = ViewManager.STATE_PREPARE;
    private String currentErrorMessage = "";
    private String modelName = "";

    private Listener listener;

    public ViewManager(IME ime) {
        this.ime = ime;
        initialized = false;
    }

    public void init() {
        overlayView = (ConstraintLayout) ime.getLayoutInflater().inflate(R.layout.ime, null);
        resultView = overlayView.findViewById(R.id.result_text);
        micButton = overlayView.findViewById(R.id.mic_button);
        backButton = overlayView.findViewById(R.id.back_button);
        backspaceButton = overlayView.findViewById(R.id.backspace_button);
        modelButton = overlayView.findViewById(R.id.model_button);
        returnButton = overlayView.findViewById(R.id.return_button);

        overlayView.setMinHeight(convertDpToPixel(300));

        resultView.setMovementMethod(new ScrollingMovementMethod());

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


        initialized = true;

        if (currentState == STATE_ERROR && !currentErrorMessage.isEmpty())
            setErrorState(currentErrorMessage);
        else setUiState(currentState);

        setModelName(modelName);

        setUpTheme();
    }

    private int currentForeground = Integer.MAX_VALUE;
    private int currentBackground = Integer.MAX_VALUE;

    private void setUpTheme() {
        int foreground = ThemePreferences.getForegroundColor();
        int background = ThemePreferences.getBackgroundColor();

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

    public void refresh() {
        setUpTheme();
    }

    public void setUiState(int state) {
        boolean enabled;
        int text;
        int icon;
        switch (state) {
            case STATE_PREPARE:
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
        if (initialized) {
            resultView.setText(text);
            micButton.setImageDrawable(AppCompatResources.getDrawable(ime, icon));
            micButton.setEnabled(enabled);
        }
        currentState = state;
    }

    public void setErrorState(String message) {
        setUiState(STATE_ERROR);
        if (initialized) resultView.setText(message);
        currentErrorMessage = message;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
        if (initialized) modelButton.setText(modelName);
    }

    public ConstraintLayout getRoot() {
        return overlayView;
    }

    public int getCurrentState() {
        return currentState;
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


    private int convertDpToPixel(float dp) {
        return (int) (dp * (ime.getResources().getDisplayMetrics().densityDpi / 160f));
    }
}
