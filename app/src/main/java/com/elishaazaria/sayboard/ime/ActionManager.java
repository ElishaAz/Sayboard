package com.elishaazaria.sayboard.ime;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.SettingsActivity;

public class ActionManager {

    private final IME ime;
    private final ViewManager viewManager;

    private final InputMethodManager mInputMethodManager;
    private int enterAction = EditorInfo.IME_ACTION_UNSPECIFIED;

    private int selectionStart = 0;
    private int selectionEnd = 0;

    public ActionManager(IME ime, ViewManager viewManager) {
        this.ime = ime;
        this.viewManager = viewManager;

        mInputMethodManager = (InputMethodManager) ime.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void setEnterAction(int enterAction) {
        this.enterAction = enterAction;
    }

    public void onCreateInputView() {
        InputConnection ic = ime.getCurrentInputConnection();
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
    }

    public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        selectionStart = newSelStart;
        selectionEnd = newSelEnd;
        Log.d("VoskIME", "selection update: " + selectionStart + ", " + selectionEnd);
    }

    public void selectCharsBack(int chars) {
        InputConnection ic = ime.getCurrentInputConnection();
        if (ic == null) return;
        int start = selectionEnd - chars;
        if (start < 0) start = 0;
        ic.setSelection(start, selectionEnd);
    }

    public void deleteSelection() {
        InputConnection ic = ime.getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText("", 1);
    }

    public void deleteLastChar() {
        // delete last char
        InputConnection ic = ime.getCurrentInputConnection();
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

    public void sendEnter() {
        InputConnection ic = ime.getCurrentInputConnection();
        if (ic == null) return;

        if (enterAction == EditorInfo.IME_ACTION_UNSPECIFIED) {
            ime.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
        } else {
            ic.performEditorAction(enterAction);
        }
    }

    public void appendSpecial(String text) {
        InputConnection ic = ime.getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText(text, 1);
    }

    /**
     * Switch to the previous IME, either when the user tries to edit an unsupported field (e.g. password),
     * or when they explicitly want to be taken back to the previous IME e.g. in case of a one-shot
     * speech input.
     */
    public void switchToLastIme(boolean showError) {
        boolean result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            result = ime.switchToPreviousInputMethod();
        } else {
            result = mInputMethodManager.switchToLastInputMethod(ime.getToken());
        }
        if (!result && showError) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_no_previous_ime);
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR);
        }
    }

    public void openSettings() {
        Intent myIntent = new Intent(ime, SettingsActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ime.startActivity(myIntent);
    }
}
