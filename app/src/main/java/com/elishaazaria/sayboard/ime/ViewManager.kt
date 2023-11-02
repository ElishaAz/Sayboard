package com.elishaazaria.sayboard.ime

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
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
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MicOff
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
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.ime.recognizers.RecognizerState
import com.elishaazaria.sayboard.sayboardPreferenceModel
import com.elishaazaria.sayboard.theme.Shapes
import com.elishaazaria.sayboard.ui.utils.MyIconButton
import dev.patrickgold.jetpref.datastore.model.observeAsState

class ViewManager(private val ime: Context) : AbstractComposeView(ime),
    Observer<RecognizerState> {
    private val prefs by sayboardPreferenceModel()
    val stateLD = MutableLiveData(STATE_INITIAL)
    val errorMessageLD = MutableLiveData(R.string.mic_info_error)
    private var listener: Listener? = null
    val recognizerNameLD = MutableLiveData("")

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
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(MaterialTheme.colors.background)
                ) {
                    Column {
                        Row {
                            IconButton(onClick = { listener?.backClicked() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectDragGestures(onDragStart = {
                                            listener?.backspaceTouchStart(it)
                                        }, onDragCancel = {
                                            listener?.backspaceTouchEnd()
                                        }, onDragEnd = {
                                            listener?.backspaceTouchEnd()
                                        }, onDrag = { change, amount ->
                                            listener?.backspaceTouched(change, amount)
                                        })
                                        detectTapGestures {
                                            setOnClickListener { listener?.backspaceClicked() }
                                        }
                                    }
                                    .minimumInteractiveComponentSize()

                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val contentAlpha = LocalContentAlpha.current
                                CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardBackspace,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            val leftKeys by prefs.keyboardKeysLeft.observeAsState()
                            FlowColumn() {
                                for (key in leftKeys) {
                                    IconButton(onClick = { listener?.buttonClicked(key.text) }) {
                                        Text(text = key.label)
                                    }
                                }
                            }
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
                            val rightKeys by prefs.keyboardKeysRight.observeAsState()
                            FlowColumn {
                                for (key in rightKeys) {
                                    IconButton(onClick = { listener?.buttonClicked(key.text) }) {
                                        Text(text = key.label)
                                    }
                                }
                            }
                        }
                        Text(
                            text = when (stateS.value) {
                                STATE_INITIAL, STATE_LOADING -> stringResource(id = R.string.mic_info_preparing)
                                STATE_READY, STATE_PAUSED -> stringResource(id = R.string.mic_info_ready)
                                STATE_LISTENING -> stringResource(id = R.string.mic_info_recording)
                                else -> stringResource(id = errorMessageS.value)
                            }, modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Row {
                            IconButton(
                                onClick = { listener?.modelClicked() },
                                modifier = Modifier.padding(5.dp)
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
                                Icon(
                                    imageVector = Icons.Default.KeyboardReturn,
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
        fun backspaceTouched(change: PointerInputChange, dragAmount: Offset)
        fun backspaceTouchEnd()
        fun returnClicked()
        fun modelClicked()

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