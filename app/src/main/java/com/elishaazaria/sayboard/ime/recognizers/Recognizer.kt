package com.elishaazaria.sayboard.ime.recognizers

interface Recognizer {
    fun reset()
    fun acceptWaveForm(buffer: ShortArray?, nread: Int): Boolean
    fun getResult(): String
    fun getPartialResult(): String
    fun getFinalResult(): String
    val sampleRate: Float
}