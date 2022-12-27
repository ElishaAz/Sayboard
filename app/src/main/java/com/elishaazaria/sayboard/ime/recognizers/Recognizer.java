package com.elishaazaria.sayboard.ime.recognizers;

public interface Recognizer {
    void reset();

    boolean acceptWaveForm(short[] buffer, int nread);

    String getResult();

    String getPartialResult();

    String getFinalResult();

    float getSampleRate();
}
