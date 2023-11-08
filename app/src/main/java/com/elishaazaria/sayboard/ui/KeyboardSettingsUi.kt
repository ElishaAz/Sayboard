package com.elishaazaria.sayboard.ui

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elishaazaria.sayboard.AppPrefs
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.sayboardPreferenceModel
import com.elishaazaria.sayboard.utils.Key
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.DialogSliderPreference
import dev.patrickgold.jetpref.datastore.ui.ExperimentalJetPrefDatastoreUi
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.datastore.ui.PreferenceGroup
import dev.patrickgold.jetpref.datastore.ui.PreferenceUiScope
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalJetPrefDatastoreUi::class)
@Composable
fun KeyboardSettingsUi() = ScrollablePreferenceLayout(sayboardPreferenceModel()) {
    PreferenceGroup(title = stringResource(id = R.string.keyboard_height_header)) {
        DialogSliderPreference(
            pref = prefs.keyboardHeightPortrait,
            title = stringResource(id = R.string.keyboard_height_portrait_title),
            min = 0.01f,
            max = 1f,
            stepIncrement = 0.01f
        )
        DialogSliderPreference(
            pref = prefs.keyboardHeightLandscape,
            title = stringResource(id = R.string.keyboard_height_landscape_title),
            min = 0.01f,
            max = 1f,
            stepIncrement = 0.01f
        )
    }

    PreferenceGroup(title = stringResource(id = R.string.keyboard_keys_header)) {

        KeysPreference(
            prefs.keyboardKeysTop,
            stringResource(id = R.string.keyboard_keys_top_title)
        )

        KeysPreference(
            prefs.keyboardKeysLeft,
            stringResource(id = R.string.keyboard_keys_left_title)
        )

        KeysPreference(
            prefs.keyboardKeysRight,
            stringResource(id = R.string.keyboard_keys_right_title)
        )
    }
}


@Composable
private fun PreferenceUiScope<AppPrefs>.KeysPreference(
    keysPref: PreferenceData<List<Key>>, title: String
) {
    val keys = keysPref.observeAsState(keysPref.default)

    var keysDialog by remember {
        mutableStateOf(false)
    }

    var keysData by remember {
        mutableStateOf(keys.value)
    }

    keysData = keys.value

    val summary = keys.value.map { p -> p.label }.joinToString(" ")

    Preference(title = title, summary = summary) {
        keysDialog = true
    }

    if (keysDialog) {
        JetPrefAlertDialog(
            title = title,
            onDismiss = { keysDialog = false },
            onConfirm = {
                keysPref.set(keysData)
                keysDialog = false
            },
            onNeutral = {
                keysPref.reset()
                keysDialog = false
            },
            confirmLabel = stringResource(id = R.string.button_confirm),
            dismissLabel = stringResource(id = R.string.button_cancel),
            neutralLabel = stringResource(id = R.string.button_default)
        ) {
            Column {

                val state = rememberReorderableLazyListState(onMove = { from, to ->
                    keysData = keysData.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                })
                Log.d("UISettingsUi", keysData.joinToString())
                LazyColumn(
                    state = state.listState,
                    modifier = Modifier.run {
                        reorderable(state)
                            .detectReorderAfterLongPress(state)
                            .heightIn(0.dp, (LocalConfiguration.current.screenHeightDp * 0.9).dp)
                    }
                ) {
                    items(keysData, { it.label }) { item ->
                        ReorderableItem(
                            state,
                            key = item.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) { isDragging ->
                            val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                            Card(
                                modifier = Modifier
                                    .shadow(elevation.value)
                                    .background(MaterialTheme.colors.onSurface.copy(0.2f))
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(MaterialTheme.colors.onSurface.copy(0.2f))
                                        .padding(10.dp)
                                ) {

                                    Text(
                                        stringResource(id = R.string.keyboard_keys_dialog_key_entry).format(
                                            item.label,
                                            item.text
                                        )
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        keysData =
                                            keysData.toMutableList().apply {
                                                remove(item)
                                            }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                var labelValue by remember {
                    mutableStateOf("")
                }
                var textValue by remember {
                    mutableStateOf("")
                }
                Card(
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary.copy(0.2f))
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colors.primary.copy(0.2f))
                            .padding(10.dp)
                    ) {
                        TextField(
                            value = labelValue,
                            onValueChange = {
                                labelValue = it
                            },
                            label = {
                                Text(text = stringResource(id = R.string.keyboard_keys_dialog_key_label_label))
                            },
                            modifier = Modifier
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = textValue,
                            onValueChange = {
                                textValue = it
                            },
                            label = {
                                Text(text = stringResource(id = R.string.keyboard_keys_dialog_key_text_label))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val newKey = Key(labelValue, textValue)
                            if (keysData.none { k -> k.label == newKey.label }) {
                                keysData = keysData.toMutableList().apply {
                                    add(newKey)
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        }

    }
}