package com.elishaazaria.sayboard.ime.recognizers;

import android.os.Debug;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.elishaazaria.sayboard.data.VoskServerData;
import com.google.protobuf.ByteString;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import vosk.stt.v1.SttServiceGrpc;

import dev.gustavoavila.websocketclient.WebSocketClient;
import vosk.stt.v1.SttServiceOuterClass;

public class VoskServer implements RecognizerSource {
    private final MutableLiveData<RecognizerState> stateLD = new MutableLiveData<>(RecognizerState.NONE);

    private final VoskServerData data;

    private MyRecognizerGRPC recognizer;

    public VoskServer(VoskServerData data) {
        this.data = data;
    }

    @Override
    public void initialize(Executor executor, Observer<RecognizerSource> onLoaded) {
        stateLD.postValue(RecognizerState.LOADING);
        recognizer = new MyRecognizerGRPC(data.uri, 16000);
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
        return stateLD;
    }

    @Override
    public int getErrorMessage() {
        return 0;
    }

    @Override
    public String getName() {
        return String.format("%s:%s", data.uri.getHost(), data.uri.getPort());
    }

    private static class MyRecognizerGRPC implements Recognizer, StreamObserver<SttServiceOuterClass.StreamingRecognitionResponse> {
        private final SttServiceGrpc.SttServiceBlockingStub blockingStub;
        private final SttServiceGrpc.SttServiceStub asyncStub;
        private final StreamObserver<SttServiceOuterClass.StreamingRecognitionRequest> requestStream;

        private final SttServiceOuterClass.RecognitionConfig config;
        private final int sampleRate;

        private CountDownLatch latch;

        private String result;
        private String finalResult;
        private String partialResult;
        private boolean isPartialResult;

        private boolean closed = false;

        public MyRecognizerGRPC(URI uri, int sampleRate) {
            this.sampleRate = sampleRate;
            ManagedChannel channel = ManagedChannelBuilder.forAddress(uri.getHost(), uri.getPort()).build();
            blockingStub = SttServiceGrpc.newBlockingStub(channel);
            asyncStub = SttServiceGrpc.newStub(channel);

            requestStream = asyncStub.streamingRecognize(this);

            config = SttServiceOuterClass.RecognitionConfig.newBuilder()
                    .setSpecification(SttServiceOuterClass.RecognitionSpec.newBuilder()
                            .setAudioEncoding(SttServiceOuterClass.RecognitionSpec.AudioEncoding.LINEAR16_PCM)
                            .setSampleRateHertz(sampleRate)
                            .setMaxAlternatives(1)
                            .setPartialResults(true)
                            .build())
                    .build();

            latch = new CountDownLatch(1);
        }


        @Override
        public void reset() {
            throw new UnsupportedOperationException("Reset was not yet implemented");
        }

        @Override
        public boolean acceptWaveForm(short[] buffer, int nread) {
            if (closed) return false;

            java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(nread * 2);
            bb.asShortBuffer().put(buffer, 0, nread);
            requestStream.onNext(SttServiceOuterClass.StreamingRecognitionRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(bb))
                    .setConfig(config)
                    .build());
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return isPartialResult;
        }

        @Override
        public String getResult() {
            return result;
        }

        @Override
        public String getPartialResult() {
            return partialResult;
        }

        @Override
        public String getFinalResult() {
            return finalResult;
        }

        @Override
        public float getSampleRate() {
            return sampleRate;
        }

        public void close() {
            requestStream.onCompleted();
            closed = true;
            latch.countDown();
            result = partialResult = "";
        }

        /*************************** gRPC *********************************/

        @Override
        public void onNext(SttServiceOuterClass.StreamingRecognitionResponse value) {
            Log.d("VoskServer", "Message received: " + value);
            for (SttServiceOuterClass.SpeechRecognitionChunk chunk : value.getChunksList()) {
                if (chunk.getEndOfUtterance()) {
                    finalResult = chunk.getAlternatives(0).getText();
                    isPartialResult = false;
                } else if (chunk.getFinal()) {
                    result = chunk.getAlternatives(0).getText();
                    isPartialResult = false;
                } else {
                    partialResult = chunk.getAlternatives(0).getText();
                    isPartialResult = true;
                }

                CountDownLatch oldLatch = latch;
                latch = new CountDownLatch(1);
                oldLatch.countDown();
            }
        }

        @Override
        public void onError(Throwable t) {

        }

        @Override
        public void onCompleted() {

        }
    }
}
