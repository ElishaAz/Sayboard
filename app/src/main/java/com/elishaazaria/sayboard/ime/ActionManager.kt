package com.elishaazaria.sayboard.ime

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputMethodManager
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.SettingsActivity

class ActionManager(private val ime: IME, private val viewManager: ViewManager) {
    private val mInputMethodManager: InputMethodManager = ime.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private var enterAction = EditorInfo.IME_ACTION_UNSPECIFIED
    private var selectionStart = 0
    private var selectionEnd = 0

    fun setEnterAction(enterAction: Int) {
        this.enterAction = enterAction
    }

    fun onCreateInputView() {
        val ic = ime.currentInputConnection
        if (ic != null) {
            val et = ic.getExtractedText(ExtractedTextRequest(), 0)
            if (et != null) {
                selectionStart = et.selectionStart
                selectionEnd = et.selectionEnd
            } else {
                selectionStart = 0
                selectionEnd = 0
            }
        }
    }

    fun updateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        selectionStart = newSelStart
        selectionEnd = newSelEnd
        Log.d("VoskIME", "selection update: $selectionStart, $selectionEnd")
    }

    fun selectCharsBack(chars: Int) {
        val ic = ime.currentInputConnection ?: return
        var start = selectionEnd - chars
        if (start < 0) start = 0
        ic.setSelection(start, selectionEnd)
    }

    fun deleteSelection() {
        val ic = ime.currentInputConnection ?: return
        ic.commitText("", 1)
    }

    fun deleteLastChar() {
        // delete last char
        val ic = ime.currentInputConnection ?: return
        val selectedChars = ic.getSelectedText(0)
        if (selectedChars == null) {
            ic.deleteSurroundingText(1, 0)
        } else if (selectedChars.toString().isEmpty()) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.performContextMenuAction(android.R.id.cut)
        }
    }

    fun sendEnter() {
        val ic = ime.currentInputConnection ?: return
        if (enterAction == EditorInfo.IME_ACTION_UNSPECIFIED) {
            ime.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        } else {
            ic.performEditorAction(enterAction)
        }
    }

    fun appendSpecial(text: String?) {
        val ic = ime.currentInputConnection ?: return
        ic.commitText(text, 1)
    }

    /**
     * Switch to the previous IME, either when the user tries to edit an unsupported field (e.g. password),
     * or when they explicitly want to be taken back to the previous IME e.g. in case of a one-shot
     * speech input.
     */
    fun switchToLastIme(showError: Boolean) {
        val result: Boolean
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ime.switchToPreviousInputMethod()
        } else {
            mInputMethodManager.switchToLastInputMethod(ime.token)
        }
        if (!result && showError) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_no_previous_ime)
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR)
        }
    }

    fun openSettings() {
        val myIntent = Intent(ime, SettingsActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ime.startActivity(myIntent)
    }
}