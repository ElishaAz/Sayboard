package com.elishaazaria.sayboard.ime;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.ime.recognizers.Recognizer;
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource;
import com.elishaazaria.sayboard.ime.recognizers.providers.RecognizerSourceProvider;
import com.elishaazaria.sayboard.ime.recognizers.providers.VoskLocalProvider;
import com.elishaazaria.sayboard.ime.recognizers.providers.VoskServerProvider;
import com.elishaazaria.sayboard.preferences.LogicPreferences;
import com.elishaazaria.sayboard.preferences.ModelPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ModelManager {

    private final IME ime;
    private final ViewManager viewManager;

    private MySpeechService speechService;

    private boolean running = false;

    private final List<RecognizerSourceProvider> sourceProviders = new ArrayList<>();
    private List<RecognizerSource> recognizerSources = new ArrayList<>();
    private int currentRecognizerSourceIndex = 0;
    private RecognizerSource currentRecognizerSource;

    public ModelManager(IME ime, ViewManager viewManager) {
        this.ime = ime;
        this.viewManager = viewManager;

        sourceProviders.add(new VoskLocalProvider(ime));
        if (ModelPreferences.VOSK_SERVER_ENABLED) {
            sourceProviders.add(new VoskServerProvider());
        }

        for (RecognizerSourceProvider provider : sourceProviders) {
            provider.loadSources(recognizerSources);
        }

        if (recognizerSources.size() == 0) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_no_recognizers);
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR);
        } else {
            currentRecognizerSourceIndex = 0;
            initializeRecognizer();
        }
    }

    private final Executor executor = Executors.newSingleThreadExecutor();

    public void initializeRecognizer() {
        if (recognizerSources.size() == 0) return;

        Observer<RecognizerSource> onLoaded = (r) -> {
            if (LogicPreferences.isListenImmediately()) {
                this.start(); // execute after initialize
            }
        };

        currentRecognizerSource = recognizerSources.get(currentRecognizerSourceIndex);
        viewManager.recognizerNameLD.postValue(currentRecognizerSource.getName());
        currentRecognizerSource.getStateLD().observe(ime, viewManager);
        currentRecognizerSource.initialize(executor, onLoaded);
    }

    private void stopRecognizerSource() {
        currentRecognizerSource.close(LogicPreferences.isWeakRefModel());
        currentRecognizerSource.getStateLD().removeObserver(viewManager);
    }

    public void switchToNextRecognizer() {
        if (recognizerSources.size() == 0) return;

        stopRecognizerSource();
        currentRecognizerSourceIndex++;
        if (currentRecognizerSourceIndex >= recognizerSources.size()) {
            currentRecognizerSourceIndex = 0;
        }
        initializeRecognizer();
    }

    public void start() {
        if (running || speechService != null) {
            speechService.stop();
        }

        viewManager.stateLD.postValue(ViewManager.STATE_LISTENING);
        try {
            Recognizer recognizer = currentRecognizerSource.getRecognizer();
            if (ActivityCompat.checkSelfPermission(ime, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            speechService = new MySpeechService(recognizer, recognizer.getSampleRate());
            speechService.startListening(ime);
        } catch (IOException e) {
            viewManager.errorMessageLD.postValue(R.string.mic_error_mic_in_use);
            viewManager.stateLD.postValue(ViewManager.STATE_ERROR);
        }
        running = true;
    }

    private boolean pausedState = false;

    public void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
            pausedState = checked;
            if (checked) {
                viewManager.stateLD.postValue(ViewManager.STATE_PAUSED);
            } else {
                viewManager.stateLD.postValue(ViewManager.STATE_LISTENING);
            }
        } else {
            pausedState = false;
        }
    }

    public boolean isPaused() {
        return pausedState && (speechService != null);
    }

    public void stop() {
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
        speechService = null;
        running = false;
        stopRecognizerSource();
    }

    public void onDestroy() {
        stop();
    }

    public boolean isRunning() {
        return running;
    }

    public void reloadModels() {
        List<RecognizerSource> newModels = new ArrayList<>();

        for (RecognizerSourceProvider provider : sourceProviders) {
            provider.loadSources(newModels);
        }

        RecognizerSource currentModel = recognizerSources.get(currentRecognizerSourceIndex);
        recognizerSources = newModels;
        currentRecognizerSourceIndex = newModels.indexOf(currentModel);
        if (currentRecognizerSourceIndex == -1) {
            currentRecognizerSourceIndex = 0;
        }
    }
}
