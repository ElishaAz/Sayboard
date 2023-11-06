package com.elishaazaria.sayboard.ime

import android.util.Log

class TextManager(private val ime: IME, private val modelManager: ModelManager) {
    private var addSpace = false
    private var firstSinceResume = true

    private var composing = false

    fun onUpdateSelection(
        newSelStart: Int,
        newSelEnd: Int,
    ) {
        if (!composing) {
            if (newSelStart == newSelEnd) { // cursor moved
                checkAddSpace()
            }
        }
    }

    fun onText(text: String, mode: Mode) {
        if (text.isEmpty())  // no need to commit empty text
            return
        Log.d(
            TAG,
            "onText. text: $text, mode: $mode, addSpace: $addSpace, firstSinceResume: $firstSinceResume"
        )

        if (text.startsWith(" ")) {
            Log.d(TAG, "Starts with space!")
        }

        if (firstSinceResume) {
            firstSinceResume = false
            checkAddSpace()
        }

        val ic = ime.currentInputConnection ?: return

        var spacedText = text
        if (modelManager.currentRecognizerSourceAddSpaces && addSpace) {
            spacedText = " $spacedText"
        }
        when (mode) {
            Mode.FINAL, Mode.STANDARD -> {
                // add a space next time. Usually overridden by onUpdateSelection
                addSpace = addSpaceAfter(
                    spacedText[spacedText.length - 1] // last char
                )
                composing = false
                ic.commitText(spacedText, 1)
            }

            Mode.PARTIAL -> {
                composing = true
                ic.setComposingText(spacedText, 1)
            }

            Mode.INSERT -> {                // Manual insert. Don't add a space.
                composing = false
                ic.commitText(text, 1)
            }
        }
    }

    private fun checkAddSpace() {
        if (!modelManager.currentRecognizerSourceAddSpaces) {
            addSpace = false
            return
        }
        val cs = ime.currentInputConnection.getTextBeforeCursor(1, 0)
        Log.d(TAG, "Standard, Text: $cs")
        if (cs != null) {
            addSpace = cs.isNotEmpty() && addSpaceAfter(cs[0])
        }
    }

    private fun addSpaceAfter(char: Char): Boolean = when (char) {
        '"' -> false
        '*' -> false
        ' ' -> false
        else -> true
    }

    fun onResume() {
        firstSinceResume = true;
    }

    enum class Mode {
        STANDARD, PARTIAL, FINAL, INSERT
    }

    companion object {
        private const val TAG = "TextManager"
    }
}