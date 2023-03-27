package com.elishaazaria.sayboard.ime;

import android.os.Build;
import android.util.Log;
import android.view.inputmethod.InputConnection;

public class TextManager {

    private final IME ime;

    public TextManager(IME ime) {

        this.ime = ime;
    }

    private boolean isFirstCall = true;
    private boolean addSpace;

    public void onText(String text, Mode mode) {
        InputConnection ic = ime.getCurrentInputConnection();
        if (ic == null) return;

        // If it's the first call after a commit
        if (isFirstCall) {
            isFirstCall = false;
            addSpace = shouldAddSpace(ic);
        }

        if (addSpace) {
            text = " " + text;
        }

        switch (mode) {
            case FINAL:
            case STANDARD:
                ic.commitText(text, 1);
                isFirstCall = true;
                break;
            case PARTIAL:
                ic.setComposingText(text, 1);
                break;
            case INSERT:
                // Manual insert. Don't add a space.
                ic.commitText(text, 1);
                break;
        }
    }

    private boolean shouldAddSpace(InputConnection ic) {
        CharSequence cs = ic.getTextBeforeCursor(1, 0);
        Log.d("TextManager", "Standard, Text: " + cs);
        if (cs != null) {
            return !(cs.length() == 0  // if we're at the start of the text
                    || cs.equals(" ")); // or there's a space already, then don't add.
        }
        return true; // Unknown. Best to add a space.
    }


    public enum Mode {
        STANDARD, PARTIAL, FINAL, INSERT
    }
}
