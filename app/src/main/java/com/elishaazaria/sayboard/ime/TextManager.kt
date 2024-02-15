package com.elishaazaria.sayboard.ime

import android.util.Log
import com.elishaazaria.sayboard.recognition.ModelManager
import com.elishaazaria.sayboard.sayboardPreferenceModel

class TextManager(private val ime: IME, private val modelManager: ModelManager) {
    private val prefs by sayboardPreferenceModel()

    private var addSpace = false
    private var capitalize = true
    private var firstSinceResume = true

    private var composing = false

    fun onUpdateSelection(
        newSelStart: Int,
        newSelEnd: Int,
    ) {
        if (!composing) {
            if (newSelStart == newSelEnd) { // cursor moved
                checkAddSpaceAndCapitalize()
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
            checkAddSpaceAndCapitalize()
        }

        val ic = ime.currentInputConnection ?: return

        var spacedText = text
        if (prefs.logicAutoCapitalize.get() && capitalize) {
            spacedText = spacedText[0].uppercase() + spacedText.substring(1)
        }

        if (modelManager.currentRecognizerSourceAddSpaces && addSpace) {
            spacedText = " $spacedText"
        }
        when (mode) {
            Mode.FINAL, Mode.STANDARD -> {
                // add a space next time. Usually overridden by onUpdateSelection
                addSpace = addSpaceAfter(
                    spacedText[spacedText.length - 1] // last char
                )
                capitalizeAfter(
                    spacedText
                )?.let {
                    capitalize = it
                }
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

    private fun checkAddSpaceAndCapitalize() {
        if (!modelManager.currentRecognizerSourceAddSpaces) {
            addSpace = false
            return
        }
        val cs = ime.currentInputConnection.getTextBeforeCursor(3, 0)
        if (cs != null) {
            addSpace = cs.isNotEmpty() && addSpaceAfter(cs[cs.length - 1])

            val value = capitalizeAfter(cs)
            value?.let {
                capitalize = it
            }
        }
    }

    private fun capitalizeAfter(string: CharSequence): Boolean? {
        for (char in string.reversed()) {
            if (char.isLetterOrDigit()) {
                return false
            }
            if (char in sentenceTerminator) {
                return true
            }
        }
        return null
    }

    private fun addSpaceAfter(char: Char): Boolean = when (char) {
        '"' -> false
        '*' -> false
        ' ' -> false
        '\n' -> false
        '\t' -> false
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
        private val sentenceTerminator = charArrayOf('.', '\n', '!', '?')
    }
}