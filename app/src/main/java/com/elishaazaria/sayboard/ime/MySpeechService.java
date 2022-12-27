/*
 * org.vosk.SpeechService, extended to support other recognizers.
 */

package com.elishaazaria.sayboard.ime;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresPermission;

import com.elishaazaria.sayboard.ime.recognizers.Recognizer;

import java.io.IOException;

import org.vosk.android.RecognitionListener;

public class MySpeechService {
    private final Recognizer recognizer;
    private final int sampleRate;
    private static final float BUFFER_SIZE_SECONDS = 0.2F;
    private final int bufferSize;
    private final AudioRecord recorder;
    private MySpeechService.RecognizerThread recognizerThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public MySpeechService(Recognizer recognizer, float sampleRate) throws IOException {
        this.recognizer = recognizer;
        this.sampleRate = (int) sampleRate;
        this.bufferSize = Math.round((float) this.sampleRate * BUFFER_SIZE_SECONDS);
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, this.sampleRate, 16, 2, this.bufferSize * 2);
        if (this.recorder.getState() == 0) {
            this.recorder.release();
            throw new IOException("Failed to initialize recorder. Microphone might be already in use.");
        }
    }

    public boolean startListening(RecognitionListener listener) {
        if (null != this.recognizerThread) {
            return false;
        } else {
            this.recognizerThread = new MySpeechService.RecognizerThread(listener);
            this.recognizerThread.start();
            return true;
        }
    }

    public boolean startListening(RecognitionListener listener, int timeout) {
        if (null != this.recognizerThread) {
            return false;
        } else {
            this.recognizerThread = new MySpeechService.RecognizerThread(listener, timeout);
            this.recognizerThread.start();
            return true;
        }
    }

    private boolean stopRecognizerThread() {
        if (null == this.recognizerThread) {
            return false;
        } else {
            try {
                this.recognizerThread.interrupt();
                this.recognizerThread.join();
            } catch (InterruptedException var2) {
                Thread.currentThread().interrupt();
            }

            this.recognizerThread = null;
            return true;
        }
    }

    public boolean stop() {
        return this.stopRecognizerThread();
    }

    public boolean cancel() {
        if (this.recognizerThread != null) {
            this.recognizerThread.setPause(true);
        }

        return this.stopRecognizerThread();
    }

    public void shutdown() {
        this.recorder.release();
    }

    public void setPause(boolean paused) {
        if (this.recognizerThread != null) {
            this.recognizerThread.setPause(paused);
        }

    }

    public void reset() {
        if (this.recognizerThread != null) {
            this.recognizerThread.reset();
        }

    }

    private final class RecognizerThread extends Thread {
        private int remainingSamples;
        private final int timeoutSamples;
        private static final int NO_TIMEOUT = -1;
        private volatile boolean paused;
        private volatile boolean reset;
        RecognitionListener listener;

        public RecognizerThread(RecognitionListener listener, int timeout) {
            this.paused = false;
            this.reset = false;
            this.listener = listener;
            if (timeout != -1) {
                this.timeoutSamples = timeout * MySpeechService.this.sampleRate / 1000;
            } else {
                this.timeoutSamples = -1;
            }

            this.remainingSamples = this.timeoutSamples;
        }

        public RecognizerThread(RecognitionListener listener) {
            this(listener, -1);
        }

        public void setPause(boolean paused) {
            this.paused = paused;
        }

        public void reset() {
            this.reset = true;
        }

        public void run() {
            MySpeechService.this.recorder.startRecording();
            if (MySpeechService.this.recorder.getRecordingState() == 1) {
                MySpeechService.this.recorder.stop();
                IOException ioe = new IOException("Failed to start recording. Microphone might be already in use.");
                MySpeechService.this.mainHandler.post(() -> {
                    this.listener.onError(ioe);
                });
            }

            short[] buffer = new short[MySpeechService.this.bufferSize];

            while (!interrupted() && (this.timeoutSamples == -1 || this.remainingSamples > 0)) {
                int nread = MySpeechService.this.recorder.read(buffer, 0, buffer.length);
                if (!this.paused) {
                    if (this.reset) {
                        MySpeechService.this.recognizer.reset();
                        this.reset = false;
                    }

                    if (nread < 0) {
                        throw new RuntimeException("error reading audio buffer");
                    }

                    String result;
                    if (MySpeechService.this.recognizer.acceptWaveForm(buffer, nread)) {
                        result = MySpeechService.this.recognizer.getResult();
                        MySpeechService.this.mainHandler.post(() -> {
                            this.listener.onResult(result);
                        });
                    } else {
                        result = MySpeechService.this.recognizer.getPartialResult();
                        MySpeechService.this.mainHandler.post(() -> {
                            this.listener.onPartialResult(result);
                        });
                    }

                    if (this.timeoutSamples != -1) {
                        this.remainingSamples -= nread;
                    }
                }
            }

            MySpeechService.this.recorder.stop();
            if (!this.paused) {
                if (this.timeoutSamples != -1 && this.remainingSamples <= 0) {
                    MySpeechService.this.mainHandler.post(() -> {
                        this.listener.onTimeout();
                    });
                } else {
                    String finalResult = MySpeechService.this.recognizer.getFinalResult();
                    MySpeechService.this.mainHandler.post(() -> {
                        this.listener.onFinalResult(finalResult);
                    });
                }
            }

        }
    }
}

