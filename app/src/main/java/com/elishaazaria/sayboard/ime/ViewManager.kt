package com.elishaazaria.sayboard.ime

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.ime.recognizers.RecognizerState
import com.elishaazaria.sayboard.preferences.UIPreferences.getBackgroundColor
import com.elishaazaria.sayboard.preferences.UIPreferences.getForegroundColor
import com.elishaazaria.sayboard.preferences.UIPreferences.screenHeightLandscape
import com.elishaazaria.sayboard.preferences.UIPreferences.screenHeightPortrait

class ViewManager(private val ime: IME) : Observer<RecognizerState> {
    lateinit var root: ConstraintLayout
        private set
    private lateinit var micButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var backspaceButton: ImageButton
    private lateinit var returnButton: ImageButton
    private lateinit var modelButton: Button
    private lateinit var resultView: TextView
    val stateLD = MutableLiveData(STATE_INITIAL)
    val errorMessageLD = MutableLiveData(0)
    val recognizerNameLD = MutableLiveData("")
    private var listener: Listener? = null
    fun init() {
        initializeVariables()
        reloadOrientation()
        setUpListeners()
        currentForeground = Int.MAX_VALUE
        currentBackground = Int.MAX_VALUE
        setUpTheme()
    }

    @SuppressLint("InflateParams")
    private fun initializeVariables() {
        this.root = ime.layoutInflater.inflate(R.layout.ime, null) as ConstraintLayout
        resultView = root.findViewById(R.id.result_text)
        micButton = root.findViewById(R.id.mic_button)
        backButton = root.findViewById(R.id.back_button)
        backspaceButton = root.findViewById(R.id.backspace_button)
        modelButton = root.findViewById(R.id.model_button)
        returnButton = root.findViewById(R.id.return_button)
        resultView.setMovementMethod(ScrollingMovementMethod())
    }

    private fun setUpListeners() {
        micButton.setOnClickListener { v: View? -> if (listener != null) listener!!.micClick() }
        micButton.setOnLongClickListener { v: View? -> listener != null && listener!!.micLongClick() }
        backButton.setOnClickListener { v: View? -> if (listener != null) listener!!.backClicked() }
        backspaceButton.setOnClickListener { v: View? -> if (listener != null) listener!!.backspaceClicked() }
        backspaceButton.setOnTouchListener { v: View, event: MotionEvent ->
            if (listener == null) {
                v.performClick()
                return@setOnTouchListener false
            }
            listener?.backspaceTouched(v, event) == true
        }
        returnButton.setOnClickListener { v: View? -> if (listener != null) listener!!.returnClicked() }
        modelButton.setOnClickListener { v: View? -> if (listener != null) listener!!.modelClicked() }
    }

    private var currentForeground = Int.MAX_VALUE
    private var currentBackground = Int.MAX_VALUE

    init {
        stateLD.observe(ime) { state: Int -> observeState(state) }
        errorMessageLD.observe(ime) { messageId: Int -> observeError(messageId) }
        recognizerNameLD.observe(ime) { name: String? -> observeRecognizerName(name) }
    }

    private fun setUpTheme() {
        val dark =
            ime.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val foreground = getForegroundColor(dark, ime)
        val background = getBackgroundColor(dark)
        if (currentForeground == foreground && currentBackground == background) return
        currentForeground = foreground
        currentBackground = background
        root.setBackgroundColor(background)
        val foregroundTint = ColorStateList.valueOf(foreground)
        micButton.imageTintList = foregroundTint
        backButton.imageTintList = foregroundTint
        backspaceButton.imageTintList = foregroundTint
        returnButton.imageTintList = foregroundTint
        TextViewCompat.setCompoundDrawableTintList(modelButton, foregroundTint)
        modelButton.setTextColor(foreground)
        resultView.setTextColor(foreground)
    }

    private fun reloadOrientation() {
        val landscape =
            ime.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val window = ime.myWindow ?: return
        val screenHeight = ime.resources.displayMetrics.heightPixels
        val percent: Float
        percent = if (landscape) {
            screenHeightLandscape
        } else {
            screenHeightPortrait
        }
        val height = (percent * screenHeight).toInt()
        Log.d("ViewManager", "Screen height: $screenHeight, height: $height")

//        WindowManager.LayoutParams params = window.getAttributes();
//        params.height = height;
//        window.setAttributes(params);
//
        root.minHeight = height
        root.maxHeight = height
        //        overlayView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
    }

    fun refresh() {
        setUpTheme()
        reloadOrientation()
    }

    private fun observeState(state: Int) {
        val enabled: Boolean
        val text: Int
        val icon: Int
        when (state) {
            STATE_INITIAL, STATE_LOADING -> {
                text = R.string.mic_info_preparing
                icon = R.drawable.ic_settings_voice
                enabled = false
            }
            STATE_READY, STATE_PAUSED -> {
                text = R.string.mic_info_ready
                icon = R.drawable.ic_mic_none
                enabled = true
            }
            STATE_LISTENING -> {
                text = R.string.mic_info_recording
                icon = R.drawable.ic_mic
                enabled = true
            }
            STATE_ERROR -> {
                text = R.string.mic_info_error
                icon = R.drawable.ic_mic_off
                enabled = false
            }
            else -> return
        }
        resultView.setText(text)
        micButton.setImageDrawable(AppCompatResources.getDrawable(ime, icon))
        micButton.isEnabled = enabled
    }

    private fun observeError(messageId: Int) {
        if (messageId == 0) return
        resultView.text = ime.getText(messageId)
    }

    fun observeRecognizerName(name: String?) {
        modelButton.text = name
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun micClick()
        fun micLongClick(): Boolean
        fun backClicked()
        fun backspaceClicked()
        fun backspaceTouched(v: View, event: MotionEvent): Boolean
        fun returnClicked()
        fun modelClicked()
    }

    override fun onChanged(recognizerState: RecognizerState) {
        when (recognizerState) {
            RecognizerState.CLOSED, RecognizerState.NONE -> stateLD.setValue(
                STATE_INITIAL
            )
            RecognizerState.LOADING -> stateLD.setValue(STATE_LOADING)
            RecognizerState.READY -> stateLD.setValue(STATE_READY)
            RecognizerState.IN_RAM -> stateLD.setValue(STATE_PAUSED)
            RecognizerState.ERROR -> stateLD.setValue(STATE_ERROR)
        }
    }

    private fun convertDpToPixel(dp: Float): Int {
        return (dp * (ime.resources.displayMetrics.densityDpi / 160f)).toInt()
    }

    companion object {
        const val STATE_INITIAL = 0
        const val STATE_LOADING = 1
        const val STATE_READY = 2 // model loaded, ready to start
        const val STATE_LISTENING = 3
        const val STATE_PAUSED = 4
        const val STATE_ERROR = 5
    }
}