package com.elishaazaria.sayboard.ime

import android.util.Log
import android.view.inputmethod.InputConnection

class TextManager(private val ime: IME) {
    private var isFirstCall = true
    private var addSpace = false
    fun onText(text: String, mode: Mode) {
        var text = text
        val ic = ime.currentInputConnection ?: return

        // If it's the first call after a commit
        if (isFirstCall) {
            isFirstCall = false
            addSpace = shouldAddSpace(ic)
        }
        if (addSpace) {
            text = " $text"
        }
        when (mode) {
            Mode.FINAL, Mode.STANDARD -> {
                ic.commitText(text, 1)
                isFirstCall = true
            }
            Mode.PARTIAL -> ic.setComposingText(text, 1)
            Mode.INSERT ->                 // Manual insert. Don't add a space.
                ic.commitText(text, 1)
        }
    }

    private fun shouldAddSpace(ic: InputConnection): Boolean {
        val cs = ic.getTextBeforeCursor(1, 0)
        Log.d("TextManager", "Standard, Text: $cs")
        return if (cs != null) {
            !(cs.length == 0 // if we're at the start of the text
                    || cs == " ") // or there's a space already, then don't add.
        } else true
        // Unknown. Best to add a space.
    }

    enum class Mode {
        STANDARD, PARTIAL, FINAL, INSERT
    }
}