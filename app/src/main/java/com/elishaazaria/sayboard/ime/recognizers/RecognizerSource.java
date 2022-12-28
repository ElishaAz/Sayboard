package com.elishaazaria.sayboard.ime.recognizers;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.Executor;

public interface RecognizerSource {
    void initialize(Executor executor, Observer<RecognizerSource> onLoaded);

    Recognizer getRecognizer();

    void close(boolean freeRAM);

    LiveData<RecognizerState> getStateLD();

    @StringRes
    int getErrorMessage();

    String getName();
}
