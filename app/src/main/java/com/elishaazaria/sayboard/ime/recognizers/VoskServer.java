package com.elishaazaria.sayboard.ime.recognizers;

import android.os.Debug;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class VoskServer implements RecognizerSource {
    private final MutableLiveData<RecognizerState> stateLD = new MutableLiveData<>(RecognizerState.NONE);

    private final URI uri;
    private MyRecognizer recognizer;

    public VoskServer(URI uri) {

        this.uri = uri;
    }

    @Override
    public void initialize(Executor executor, Observer<RecognizerSource> onLoaded) {
        stateLD.postValue(RecognizerState.LOADING);
        recognizer = new MyRecognizer(uri);
        stateLD.postValue(RecognizerState.READY);
        onLoaded.onChanged(this);
    }

    @Override
    public Recognizer getRecognizer() {
        return recognizer;
    }

    @Override
    public void close(boolean freeRAM) {
        recognizer.close();
    }

    @Override
    public LiveData<RecognizerState> getStateLD() {
        return null;
    }

    @Override
    public int getErrorMessage() {
        return 0;
    }

    @Override
    public String getName() {
        return String.format("%s:%s", uri.getHost(), uri.getPort());
    }

    private class MyRecognizer extends WebSocketClient implements Recognizer {
        private CountDownLatch receiveLatch;
        private boolean isCompleteResult = false;
        private String lastResult;
        private String lastPartialResult;
        private String lastFinalResult;
        private boolean closing = false;

        /**
         * Initialize all the variables
         *
         * @param uri URI of the WebSocket server
         */
        public MyRecognizer(URI uri) {
            super(uri);
        }

        /************************** Recognizer **********************************/

        @Override
        public void reset() {

        }

        @Override
        public boolean acceptWaveForm(short[] buffer, int nread) {
            java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(nread * 2);
            bb.asShortBuffer().put(buffer, 0, nread);
            send(bb.array());
            receiveLatch = new CountDownLatch(1);
            try {
                receiveLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return isCompleteResult;
        }

        @Override
        public String getResult() {
            return lastResult;
        }

        @Override
        public String getPartialResult() {
            return lastPartialResult;
        }

        @Override
        public String getFinalResult() {
            return lastFinalResult;
        }

        @Override
        public float getSampleRate() {
            return 16000.0f;
        }

        @Override
        public void close() {
//            closing = true;
//            send("{\"eof\" : 1}");
            super.close();
        }

        /************************** WebSocket **********************************/

        @Override
        public void onOpen() {

        }

        @Override
        public void onTextReceived(String message) {
            Log.d("VoskServer", message);
//            lastResult = message;
            receiveLatch.countDown();

//            if (closing) {
//                super.close();
//            }
        }

        @Override
        public void onBinaryReceived(byte[] data) {

        }

        @Override
        public void onPingReceived(byte[] data) {

        }

        @Override
        public void onPongReceived(byte[] data) {

        }

        @Override
        public void onException(Exception e) {

        }

        @Override
        public void onCloseReceived() {

        }
    }
}
