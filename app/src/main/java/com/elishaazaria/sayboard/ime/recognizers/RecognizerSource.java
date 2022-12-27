package com.elishaazaria.sayboard.ime.recognizers;

import androidx.lifecycle.LiveData;

import java.util.concurrent.Executor;

public interface RecognizerSource {
    void initialize(Executor executor);

    Recognizer getRecognizer();

    void close(boolean freeRAM);

    LiveData<RecognizerState> getStateLD();

    String getName();
}
