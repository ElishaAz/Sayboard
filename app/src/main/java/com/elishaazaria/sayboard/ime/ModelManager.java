package com.elishaazaria.sayboard.ime;

import android.os.Handler;
import android.os.Looper;

import com.elishaazaria.sayboard.Tools;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ModelManager {
    private final IME ime;
    private final ViewManager viewManager;

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
            viewManager.setErrorState("No Model installed!");
        } else {
            currentModelIndex = 0;
            loadModel(models.get(0));
        }
    }

    private final Executor executor = Executors.newSingleThreadExecutor();

    private void loadModel(com.elishaazaria.sayboard.Model myModel) {
        viewManager.setModelName(myModel.locale.getDisplayName());
        viewManager.setUiState(ViewManager.STATE_PREPARE);
        stop();

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Model model = new Model(myModel.path);
            handler.post(() -> {
                this.model = model;
                stop();
                viewManager.setUiState(ViewManager.STATE_READY);
            });
        });
    }

    public void loadNextModel() {
        if (models.size() <= 1) return; // Don't reload if there is only one

        currentModelIndex++;
        if (currentModelIndex >= models.size()) currentModelIndex = 0;
        loadModel(models.get(currentModelIndex));
    }

    public void start() {
        if (model == null) return;

        if (running || speechService != null) {

            speechService.stop();
        }

        viewManager.setUiState(ViewManager.STATE_MIC);
        try {
            recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(ime);
        } catch (IOException e) {
            viewManager.setErrorState(e.getMessage());
        }
        running = true;
    }

    public void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
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
    }

    public void onDestroy() {
        stop();
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
            loadModel(models.get(0));
        }
    }
}
