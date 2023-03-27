package com.elishaazaria.sayboard.ime.recognizers.providers;

import com.elishaazaria.sayboard.data.VoskServerData;
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource;
import com.elishaazaria.sayboard.ime.recognizers.VoskServer;
import com.elishaazaria.sayboard.preferences.ModelPreferences;

import java.util.List;

public class VoskServerProvider implements RecognizerSourceProvider{
    @Override
    public void loadSources(List<RecognizerSource> recognizerSources) {
        for (VoskServerData voskServer : ModelPreferences.getVoskServers()) {
            recognizerSources.add(new VoskServer(voskServer));
        }
    }
}
