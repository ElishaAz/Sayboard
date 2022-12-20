package com.elishaazaria.sayboard.ime;

import android.os.Handler;
import android.os.Looper;

import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.preferences.LogicPreferences;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ModelManager {
    private final IME ime;
    private final ViewManager viewManager;

    private WeakReference<Model> modelWeakReference;
    private Model model;
    private SpeechService speechService;
    private Recognizer recognizer;

    private boolean running = false;

    private List<com.elishaazaria.sayboard.Model> models;
    private int currentModelIndex = 0;

    public ModelManager(IME ime, ViewManager viewManager) {
        this.ime = ime;
        this.viewManager = viewManager;

        models = Tools.getInstalledModelsList(ime);

        if (models.size() == 0) {
            viewManager.setErrorState("No Models installed!");
        } else {
            currentModelIndex = 0;
            loadModel(models.get(0));
        }
    }

    private final Executor executor = Executors.newSingleThreadExecutor();

    public void loadModel() {
        if (modelWeakReference != null) {
            Model oldModel = modelWeakReference.get();
            if (oldModel != null) {
                // equivalent to loading the model
                this.model = oldModel;
                if (LogicPreferences.isListenImmediately()) {
                    start();
                }
                return;
            }
        }
        modelWeakReference = null;
        com.elishaazaria.sayboard.Model currentModel = models.get(currentModelIndex);
        loadModel(currentModel);
    }

    private void loadModel(com.elishaazaria.sayboard.Model myModel) {
        stop();
        viewManager.setModelName(myModel.locale.getDisplayName());
        viewManager.setUiState(ViewManager.STATE_LOADING);

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Model model = new Model(myModel.path);
            handler.post(() -> {
                this.model = model;
                modelWeakReference = null;
                stop();
                viewManager.setModelName(myModel.locale.getDisplayName());
                viewManager.setUiState(ViewManager.STATE_READY);

                if (LogicPreferences.isListenImmediately()) {
                    start();
                }
            });
        });
    }

    public void unloadModel() {
        modelWeakReference = new WeakReference<>(this.model);
        this.model = null;
    }

    public void loadNextModel() {
        if (models.size() <= 1) return; // Don't reload if there is only one

        currentModelIndex++;
        if (currentModelIndex >= models.size()) currentModelIndex = 0;
        loadModel(models.get(currentModelIndex));
    }

    public boolean modelLoaded() {
        return model != null;
    }

    public void start() {
        if (model == null) return;

        if (running || speechService != null) {
            speechService.stop();
        }

        viewManager.setUiState(ViewManager.STATE_LISTENING);
        try {
            recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(ime);
        } catch (IOException e) {
            viewManager.setErrorState(e.getMessage());
        }
        running = true;
    }

    private boolean pausedState = false;

    public void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
            pausedState = checked;
            if (checked) {
                viewManager.setUiState(ViewManager.STATE_PAUSED);
            } else {
                viewManager.setUiState(ViewManager.STATE_LISTENING);
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
        if (recognizer != null) {
            recognizer.close();
        }
        recognizer = null;
        running = false;
        viewManager.setUiState(ViewManager.STATE_INITIAL);
    }

    public void onDestroy() {
        stop();
        unloadModel();
    }

    public boolean isRunning() {
        return running;
    }

    public void reloadModels() {
        List<com.elishaazaria.sayboard.Model> newModels = Tools.getInstalledModelsList(ime);
        if (newModels.size() == 0) return; // Or crash loudly

        com.elishaazaria.sayboard.Model currentModel = models.get(currentModelIndex);
        models = newModels;
        currentModelIndex = newModels.indexOf(currentModel);
        if (currentModelIndex == -1) {
            currentModelIndex = 0;
        }
    }
}
