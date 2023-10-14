package com.elishaazaria.sayboard.ime.recognizers
import java.util.Locale

interface Recognizer {
    fun reset()
    fun acceptWaveForm(buffer: ShortArray?, nread: Int): Boolean
    fun getResult(): String
    fun getPartialResult(): String
    fun getFinalResult(): String
    val sampleRate: Float
    val locale: Locale?

    val localeNeedsRemovingSpace: Boolean
        get() = listOf("ja", "zh").contains(locale?.language?:"")

    fun removeSpaceForLocale(text: String): String {
        return if (localeNeedsRemovingSpace) text.replace("\\s".toRegex(), "")
        else text
    }
}