package com.elishaazaria.sayboard.ime.recognizers

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.data.VoskLocalModel
import org.json.JSONException
import org.json.JSONObject
import org.vosk.Model
import java.util.concurrent.Executor
import java.util.Locale

class VoskLocal(private val localModel: VoskLocalModel) : RecognizerSource {
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

    override val closed: Boolean
        get() = myRecognizer == null
    override val addSpaces: Boolean
        get() = !listOf("ja", "zh").contains(localModel.locale.language)

    private fun modelLoaded(model: Model) {
        this.model = model
        stateMLD.postValue(RecognizerState.READY)
        myRecognizer = MyRecognizer(model, 16000.0f, localModel.locale)
    }

    private class MyRecognizer     //            setMaxAlternatives(3); // TODO: implement
        (model: Model, override val sampleRate: Float, override val locale: Locale?) :
        org.vosk.Recognizer(model, sampleRate),
        Recognizer {

        override fun getResult(): String {
            try {
                val result = JSONObject(super.getResult())
                return removeSpaceForLocale(result.getString("text").trim { it <= ' ' })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ""
        }

        override fun getPartialResult(): String {
            try {
                val result = JSONObject(super.getPartialResult())
                return removeSpaceForLocale(result.getString("partial").trim { it <= ' ' })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ""
        }

        override fun getFinalResult(): String {
            try {
                val result = JSONObject(super.getFinalResult())
                return removeSpaceForLocale(result.getString("text").trim { it <= ' ' })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ""
        }
    }

    override fun close(freeRAM: Boolean) {
        if (freeRAM) {
            myRecognizer?.close()
            myRecognizer = null
            model?.close()
            model = null
        }
    }

    override val errorMessage: Int
        get() = 0
    override val name: String
        get() = localModel.locale.displayName ?: ""
}