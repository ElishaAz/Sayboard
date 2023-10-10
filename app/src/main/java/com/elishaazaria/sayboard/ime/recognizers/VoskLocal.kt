package com.elishaazaria.sayboard.ime.recognizers

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.data.LocalModel
import org.json.JSONException
import org.json.JSONObject
import org.vosk.Model
import java.util.concurrent.Executor

class VoskLocal(private val localModel: LocalModel) : RecognizerSource {
    private val stateMLD = MutableLiveData(RecognizerState.NONE)
    override val stateLD: LiveData<RecognizerState>
        get() = stateMLD
    private var myRecognizer: MyRecognizer? = null
    override val recognizer: Recognizer
        get() = myRecognizer!!
    private var model: Model? = null
    override fun initialize(executor: Executor, onLoaded: Observer<RecognizerSource?>) {
        stateMLD.postValue(RecognizerState.LOADING)
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val model = Model(localModel.path)
            handler.post {
                modelLoaded(model)
                onLoaded.onChanged(this)
            }
        }
    }

    private fun modelLoaded(model: Model) {
        this.model = model
        stateMLD.postValue(RecognizerState.READY)
        myRecognizer = MyRecognizer(model, 16000.0f)
    }

    private class MyRecognizer     //            setMaxAlternatives(3); // TODO: implement
        (model: Model, override val sampleRate: Float) : org.vosk.Recognizer(model, sampleRate),
        Recognizer {

        override fun getResult(): String {
            try {
                val result = JSONObject(super.getResult())
                return result.getString("text").trim { it <= ' ' }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ""
        }

        override fun getPartialResult(): String {
            try {
                val result = JSONObject(super.getPartialResult())
                return result.getString("partial").trim { it <= ' ' }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ""
        }

        override fun getFinalResult(): String {
            try {
                val result = JSONObject(super.getFinalResult())
                return result.getString("text").trim { it <= ' ' }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ""
        }
    }

    override fun close(freeRAM: Boolean) {
        myRecognizer?.close()
        myRecognizer = null
        if (freeRAM) {
            model?.close()
            model = null
        }
    }

    override val errorMessage: Int
        get() = 0
    override val name: String
        get() = localModel.locale.displayName ?: ""
}