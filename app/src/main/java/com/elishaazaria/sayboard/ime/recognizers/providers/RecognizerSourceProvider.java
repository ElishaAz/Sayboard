package com.elishaazaria.sayboard.ime.recognizers.providers;

import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource;

import java.util.List;

public interface RecognizerSourceProvider {
    void loadSources(List<RecognizerSource> recognizerSources);
}
