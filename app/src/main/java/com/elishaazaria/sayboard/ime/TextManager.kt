package com.elishaazaria.sayboard.ime

import android.util.Log

class TextManager(private val ime: IME, private val modelManager: ModelManager) {
    private var isFirstCall = true
    private var addSpace = false

    fun onUpdateSelection(
        newSelStart: Int,
        newSelEnd: Int,
    ) {
        if (newSelStart == newSelEnd) { // cursor moved
            checkAddSpace()
        }
    }

    fun onText(text: String, mode: Mode) {
        if (text.isEmpty())  // no need to commit empty text
            return
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
                ic.commitText(spacedText, 1)
            }

            Mode.PARTIAL -> ic.setComposingText(spacedText, 1)
            Mode.INSERT ->                 // Manual insert. Don't add a space.
                ic.commitText(text, 1)
        }
    }

    private fun checkAddSpace() {
        if (!modelManager.currentRecognizerSourceAddSpaces) {
            addSpace = false
            return
        }
        val cs = ime.currentInputConnection.getTextBeforeCursor(1, 0)
        Log.d("TextManager", "Standard, Text: $cs")
        if (cs != null) {
            addSpace = cs.isNotEmpty() && addSpaceAfter(cs[0])
        }
    }

    private fun addSpaceAfter(char: Char): Boolean = when (char) {
        '"' -> false
        '*' -> false
        else -> true
    }

    enum class Mode {
        STANDARD, PARTIAL, FINAL, INSERT
    }
}