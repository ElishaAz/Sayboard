package com.elishaazaria.sayboard.ime.recognizers

import com.elishaazaria.sayboard.data.VoskServerData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import androidx.lifecycle.Observer
import io.grpc.stub.StreamObserver
import vosk.stt.v1.SttServiceOuterClass.StreamingRecognitionResponse
import vosk.stt.v1.SttServiceGrpc.SttServiceBlockingStub
import vosk.stt.v1.SttServiceGrpc.SttServiceStub
import vosk.stt.v1.SttServiceOuterClass.StreamingRecognitionRequest
import vosk.stt.v1.SttServiceOuterClass.RecognitionConfig
import io.grpc.ManagedChannelBuilder
import vosk.stt.v1.SttServiceGrpc
import vosk.stt.v1.SttServiceOuterClass
import com.google.protobuf.ByteString
import java.lang.UnsupportedOperationException
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.Locale

class VoskServer(private val data: VoskServerData) : RecognizerSource {
    private val stateMLD = MutableLiveData(RecognizerState.NONE)
    override val stateLD: LiveData<RecognizerState>
        get() = stateMLD
    private var myRecognizerGRPC: MyRecognizerGRPC? = null
    override val recognizer: Recognizer
        get() = myRecognizerGRPC!!
    override fun initialize(executor: Executor, onLoaded: Observer<RecognizerSource?>) {
        stateMLD.postValue(RecognizerState.LOADING)
        myRecognizerGRPC = MyRecognizerGRPC(data.uri, 16000.0f)
        stateMLD.postValue(RecognizerState.READY)
        onLoaded.onChanged(this)
    }

    override val locale: Locale?
    get() = data.locale

    override fun close(freeRAM: Boolean) {
        myRecognizerGRPC!!.close()
    }

    override val errorMessage: Int
        get() = 0
    override val name: String
        get() = String.format("%s:%s", data.uri.host, data.uri.port)

    private class MyRecognizerGRPC(uri: URI, override val sampleRate: Float) : Recognizer, StreamObserver<StreamingRecognitionResponse> {
        private val blockingStub: SttServiceBlockingStub
        private val asyncStub: SttServiceStub
        private val requestStream: StreamObserver<StreamingRecognitionRequest>
        private val config: RecognitionConfig
        private var latch: CountDownLatch

        private var myResult: String = ""
        private var myPartialResult: String = ""
        private var myFinalResult: String = ""

        private var isPartialResult = false
        private var closed = false

        init {
            val channel = ManagedChannelBuilder.forAddress(uri.host, uri.port).build()
            blockingStub = SttServiceGrpc.newBlockingStub(channel)
            asyncStub = SttServiceGrpc.newStub(channel)
            requestStream = asyncStub.streamingRecognize(this)
            config = RecognitionConfig.newBuilder()
                .setSpecification(
                    SttServiceOuterClass.RecognitionSpec.newBuilder()
                        .setAudioEncoding(SttServiceOuterClass.RecognitionSpec.AudioEncoding.LINEAR16_PCM)
                        .setSampleRateHertz(sampleRate.toLong())
                        .setMaxAlternatives(1)
                        .setPartialResults(true)
                        .build()
                )
                .build()
            latch = CountDownLatch(1)
        }

        override fun reset() {
            throw UnsupportedOperationException("Reset was not yet implemented")
        }

        override fun acceptWaveForm(buffer: ShortArray?, nread: Int): Boolean {
            if (closed) return false
            val bb = ByteBuffer.allocate(nread * 2)
            bb.asShortBuffer().put(buffer, 0, nread)
            requestStream.onNext(
                StreamingRecognitionRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(bb))
                    .setConfig(config)
                    .build()
            )
            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return isPartialResult
        }

        override fun getResult(): String {
            return myResult
        }

        override fun getPartialResult(): String {
            return myPartialResult
        }

        override fun getFinalResult(): String {
            return myFinalResult
        }

        fun close() {
            requestStream.onCompleted()
            closed = true
            latch.countDown()
            myPartialResult = ""
            myResult = ""
        }

        /*************************** gRPC  */
        override fun onNext(value: StreamingRecognitionResponse) {
            Log.d("VoskServer", "Message received: $value")
            for (chunk in value.chunksList) {
                if (chunk.endOfUtterance) {
                    myFinalResult = chunk.getAlternatives(0).text
                    isPartialResult = false
                } else if (chunk.final) {
                    myResult = chunk.getAlternatives(0).text
                    isPartialResult = false
                } else {
                    myPartialResult = chunk.getAlternatives(0).text
                    isPartialResult = true
                }
                val oldLatch = latch
                latch = CountDownLatch(1)
                oldLatch.countDown()
            }
        }

        override fun onError(t: Throwable) {}
        override fun onCompleted() {}
    }
}