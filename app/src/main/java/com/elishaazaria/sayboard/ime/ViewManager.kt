package com.elishaazaria.sayboard.ime

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.lightColors
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.AppPrefs
import com.elishaazaria.sayboard.Constants
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.recognition.recognizers.RecognizerState
import com.elishaazaria.sayboard.sayboardPreferenceModel
import com.elishaazaria.sayboard.theme.Shapes
import com.elishaazaria.sayboard.ui.utils.MyIconButton
import com.elishaazaria.sayboard.ui.utils.MyTextButton
import dev.patrickgold.jetpref.datastore.model.observeAsState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class ViewManager(private val ime: Context) : AbstractComposeView(ime),
    Observer<RecognizerState> {
    private val prefs by sayboardPreferenceModel()
    val stateLD = MutableLiveData(STATE_INITIAL)
    val errorMessageLD = MutableLiveData(R.string.mic_info_error)
    private var listener: Listener? = null
    val recognizerNameLD = MutableLiveData("")
    val enterActionLD = MutableLiveData(EditorInfo.IME_ACTION_UNSPECIFIED)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val stateS = stateLD.observeAsState()
        val errorMessageS = errorMessageLD.observeAsState(R.string.mic_info_error)
        val recognizerNameS = recognizerNameLD.observeAsState(initial = "")
        val height =
            (LocalConfiguration.current.screenHeightDp * when (LocalConfiguration.current.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> prefs.keyboardHeightLandscape.get()
                else -> prefs.keyboardHeightPortrait.get()
            }).toInt().dp
        IMETheme(prefs) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colors.primary
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(MaterialTheme.colors.background)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Top) {
                            IconButton(onClick = { listener?.backClicked() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null
                                )
                            }
                            val topKeys by prefs.keyboardKeysTop.observeAsState()
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                for (key in topKeys) {
                                    MyTextButton(onClick = { listener?.buttonClicked(key.text) }) {
                                        Text(text = key.label)
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(onPress = {
                                            var down = true;
                                            coroutineScope {
                                                val repeatJob = launch {
                                                    delay(Constants.BackspaceRepeatStartDelay)
                                                    while (down) {
                                                        listener?.backspaceClicked()
                                                        delay(Constants.BackspaceRepeatDelay)
                                                    }
                                                }
                                                launch {
                                                    val released = tryAwaitRelease();
                                                    down = false;
                                                    Log.d("ViewManager", "$released")
                                                    repeatJob.cancel()
                                                }
                                            }
                                        }, onTap = {
                                            listener?.backspaceClicked()
                                        })
                                    }
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(onDragStart = {
                                            listener?.backspaceTouchStart(it)
                                        }, onDragCancel = {
                                            listener?.backspaceTouchEnd()
                                        }, onDragEnd = {
                                            listener?.backspaceTouchEnd()
                                        }, onHorizontalDrag = { change, amount ->
                                            listener?.backspaceTouched(change, amount)
                                        })
                                    }
                                    .minimumInteractiveComponentSize()

                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val contentAlpha = LocalContentAlpha.current
                                CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            val leftKeys by prefs.keyboardKeysLeft.observeAsState()
                            FlowColumn() {
                                for (key in leftKeys) {
                                    MyTextButton(onClick = { listener?.buttonClicked(key.text) }) {
                                        Text(text = key.label)
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            ) {
                                MyIconButton(onClick = {
                                    listener?.micClick()
                                }, onLongClick = {
                                    listener?.micLongClick()
                                },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = when (stateS.value) {
                                            STATE_INITIAL, STATE_LOADING -> Icons.Default.SettingsVoice
                                            STATE_READY, STATE_PAUSED -> Icons.Default.MicNone
                                            STATE_LISTENING -> Icons.Default.Mic
                                            else -> Icons.Default.MicOff
                                        }, contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = when (stateS.value) {
                                        STATE_INITIAL, STATE_LOADING -> stringResource(id = R.string.mic_info_preparing)
                                        STATE_READY, STATE_PAUSED -> stringResource(id = R.string.mic_info_ready)
                                        STATE_LISTENING -> stringResource(id = R.string.mic_info_recording)
                                        else -> stringResource(id = errorMessageS.value)
                                    }, fontSize = MaterialTheme.typography.h6.fontSize,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(5.dp)
                                )
                            }
                            val rightKeys by prefs.keyboardKeysRight.observeAsState()
                            FlowColumn {
                                for (key in rightKeys) {
                                    MyTextButton(onClick = { listener?.buttonClicked(key.text) }) {
                                        Text(text = key.label)
                                    }
                                }
                            }
                        }
                        Row {
                            IconButton(onClick = { listener?.settingsClicked() }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null
                                )
                            }
                            IconButton(
                                onClick = { listener?.modelClicked() }
                            ) {
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null
                                    )
                                    Text(text = recognizerNameS.value)
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { listener?.returnClicked() }) {
                                val enterAction by enterActionLD.observeAsState()
                                Icon(
                                    imageVector = when (enterAction) {
                                        EditorInfo.IME_ACTION_GO -> Icons.Default.ArrowRightAlt
                                        EditorInfo.IME_ACTION_SEARCH -> Icons.Default.Search
                                        EditorInfo.IME_ACTION_SEND -> Icons.Default.Send
                                        EditorInfo.IME_ACTION_NEXT -> Icons.Default.NavigateNext
                                        EditorInfo.IME_ACTION_PREVIOUS -> Icons.Default.NavigateBefore
                                        else -> Icons.Default.KeyboardReturn
                                    },
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onChanged(value: RecognizerState) {
        when (value) {
            RecognizerState.CLOSED, RecognizerState.NONE -> stateLD.setValue(STATE_INITIAL)

            RecognizerState.LOADING -> stateLD.setValue(STATE_LOADING)
            RecognizerState.READY -> stateLD.setValue(STATE_READY)
            RecognizerState.IN_RAM -> stateLD.setValue(STATE_PAUSED)
            RecognizerState.ERROR -> stateLD.setValue(STATE_ERROR)
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun micClick()
        fun micLongClick(): Boolean
        fun backClicked()
        fun backspaceClicked()
        fun backspaceTouchStart(offset: Offset)
        fun backspaceTouched(change: PointerInputChange, dragAmount: Float)
        fun backspaceTouchEnd()
        fun returnClicked()
        fun modelClicked()
        fun settingsClicked()
        fun buttonClicked(text: String)
    }

    companion object {
        const val STATE_INITIAL = 0
        const val STATE_LOADING = 1
        const val STATE_READY = 2 // model loaded, ready to start
        const val STATE_LISTENING = 3
        const val STATE_PAUSED = 4
        const val STATE_ERROR = 5

//        const val BUTTON_TOP_LEFT = 1
//        const val BUTTON_MIDDLE_LEFT = 2
//        const val BUTTON_BOTTOM_LEFT = 3
//        const val BUTTON_TOP_RIGHT = 11
//        const val BUTTON_MIDDLE_RIGHT = 12
//        const val BUTTON_BOTTOM_RIGHT = 13

    }
}

@Composable
fun IMETheme(
    prefs: AppPrefs,
    content: @Composable () -> Unit
) {
    val colors = if (isSystemInDarkTheme()) {
        darkColors(
            background = Color(prefs.uiNightBackground.get()),
            primary = if (prefs.uiNightForegroundMaterialYou.get()) {
                colorResource(id = R.color.materialYouForeground)
            } else {
                Color(prefs.uiNightForeground.get())
            },
        )
    } else {
        lightColors(
            background = Color(prefs.uiDayBackground.get()),
            primary = if (prefs.uiDayForegroundMaterialYou.get()) {
                colorResource(id = R.color.materialYouForeground)
            } else {
                Color(prefs.uiDayForeground.get())
            },
        )
    }

    MaterialTheme(
        colors = colors,
        shapes = Shapes,
        content = content,
    )
}