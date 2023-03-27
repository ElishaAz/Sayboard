package com.elishaazaria.sayboard.ime.recognizers.providers;

import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.data.LocalModel;
import com.elishaazaria.sayboard.ime.IME;
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource;
import com.elishaazaria.sayboard.ime.recognizers.VoskLocal;

import java.util.List;

public class VoskLocalProvider implements RecognizerSourceProvider {
    private final IME ime;

    public VoskLocalProvider(IME ime) {
        this.ime = ime;
    }

    @Override
    public void loadSources(List<RecognizerSource> recognizerSources) {
        for (LocalModel localModel : Tools.getInstalledModelsList(ime)) {
            recognizerSources.add(new VoskLocal(localModel));
        }
    }
}
