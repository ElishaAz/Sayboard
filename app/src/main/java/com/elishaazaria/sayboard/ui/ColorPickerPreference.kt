package com.elishaazaria.sayboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.ui.PreferenceUiScope
import dev.patrickgold.jetpref.material.ui.ExperimentalJetPrefMaterialUi
import dev.patrickgold.jetpref.material.ui.JetPrefColorPicker
import dev.patrickgold.jetpref.material.ui.JetPrefListItem
import dev.patrickgold.jetpref.material.ui.rememberJetPrefColorPickerState

@OptIn(ExperimentalJetPrefMaterialUi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.ColorPickerPreference(
    pref: PreferenceData<Int>,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true
) {
    var setColor by remember {
        mutableStateOf(Color(pref.get()))
    }
    var selectedColor by remember {
        mutableStateOf(Color(pref.get()))
    }
    val colorPickerState = rememberJetPrefColorPickerState(initColor = selectedColor)
    var showDialog by remember {
        mutableStateOf(false)
    }
    JetPrefListItem(
        modifier =
        modifier.clickable(
            enabled = enabled,
            role = Role.Button,
            onClick = {
                showDialog = true
            },
        ),
        icon = {
            Spacer(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(1.dp)
                    .background(setColor)
                    .size(40.dp)
            )
        },
        text = title,
        secondaryText = summary,
        enabled = enabled
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            }) {
            Card {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = title, fontSize = 20.sp)
                    JetPrefColorPicker(state = colorPickerState,
                        onColorChange = {
                            selectedColor = it
                        })
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            showDialog = false
                        }) {
                            Text(text = "Cancel")
                        }
                        Button(onClick = {
                            showDialog = false
                            setColor = selectedColor
                            pref.set(selectedColor.toArgb())
                        }) {
                            Text(text = "Select")
                        }
                    }
                }
            }
        }
    }
}