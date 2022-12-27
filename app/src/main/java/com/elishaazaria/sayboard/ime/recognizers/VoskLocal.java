package com.elishaazaria.sayboard.ime.recognizers;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.elishaazaria.sayboard.LocalModel;

import org.vosk.Model;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

public class VoskLocal implements RecognizerSource {
    private final MutableLiveData<RecognizerState> stateLD = new MutableLiveData<>(RecognizerState.NONE);
    private final LocalModel localModel;
    private MyRecognizer recognizer;

    private Model model;
    private WeakReference<Model> modelWeakReference;

    public VoskLocal(LocalModel localModel) {
        this.localModel = localModel;
    }

    @Override
    public void initialize(Executor executor) {
        stateLD.postValue(RecognizerState.LOADING);

        if (modelWeakReference != null) {
            Model oldModel = modelWeakReference.get();
            if (oldModel != null) {
                modelLoaded(oldModel);
            }
        }

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Model model = new Model(localModel.path);
            handler.post(() -> {
                modelLoaded(model);
            });
        });
    }

    private void modelLoaded(Model model) {
        this.model = model;
        modelWeakReference = null;
        stateLD.postValue(RecognizerState.READY);
        recognizer = new MyRecognizer(model, 16000.0f);
    }

    private static class MyRecognizer extends org.vosk.Recognizer implements Recognizer {
        private final float sampleRate;

        public MyRecognizer(Model model, float sampleRate) {
            super(model, sampleRate);
            this.sampleRate = sampleRate;
        }

        @Override
        public float getSampleRate() {
            return sampleRate;
        }
    }

    @Override
    public Recognizer getRecognizer() {
        return recognizer;
    }

    @Override
    public void close(boolean freeRAM) {
        recognizer.close();
        recognizer = null;

        if (freeRAM) {
            modelWeakReference = new WeakReference<>(this.model);
            this.model = null;
            stateLD.postValue(RecognizerState.CLOSED);
        } else {
            stateLD.postValue(RecognizerState.IN_RAM);
        }
    }

    @Override
    public LiveData<RecognizerState> getStateLD() {
        return stateLD;
    }

    @Override
    public String getName() {
        return localModel.locale.getDisplayName();
    }
}
