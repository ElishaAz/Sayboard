package com.elishaazaria.sayboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.elishaazaria.sayboard.data.LocalModel
import com.elishaazaria.sayboard.downloader.FileDownloader
import com.elishaazaria.sayboard.theme.AppTheme
import com.elishaazaria.sayboard.ui.GrantPermissionUi
import com.elishaazaria.sayboard.ui.LogicSettingsUi
import com.elishaazaria.sayboard.ui.ModelsSettingsUi
import com.elishaazaria.sayboard.ui.UISettingsUi
import java.util.Locale

class SettingsActivity : ComponentActivity() {

    private val micGranted = MutableLiveData<Boolean>(true)
    private val imeGranted = MutableLiveData<Boolean>(true)

    private val modelSettingsUi = ModelsSettingsUi(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.createNotificationChannel(this)

        checkPermissions()

        modelSettingsUi.onCreate()

        setContent {
            AppTheme {
                val micGrantedState = micGranted.observeAsState(true)
                val imeGrantedState = imeGranted.observeAsState(true)
                if (micGrantedState.value && imeGrantedState.value) {

                    MainUi()
                } else {
                    GrantPermissionUi(mic = micGrantedState, ime = imeGrantedState, requestMic = {
                        ActivityCompat.requestPermissions(
                            this, arrayOf(
                                Manifest.permission.RECORD_AUDIO
                            ), PERMISSIONS_REQUEST_RECORD_AUDIO
                        )
                    }) {
                        startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
                    }
                }
            }
        }
    }

    @Composable
    private fun MainUi() {
        val tabs = listOf<String>(
            stringResource(id = R.string.title_models),
            stringResource(id = R.string.title_ui),
            stringResource(id = R.string.title_logic)
        )
        var selectedIndex by remember {
            mutableIntStateOf(0)
        }

        Scaffold(bottomBar = {
            BottomNavigation() {
                tabs.forEachIndexed { index, tab ->
                    BottomNavigationItem(
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        icon = {
                            when (index) {
                                0 -> Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null
                                )

                                1 -> Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_color_lens_24),
                                    contentDescription = null
                                )

                                2 -> Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null
                                )
                            }
                        }, label = {
                            Text(text = tab)
                        })
                }
            }
        }, floatingActionButton = {
            if (selectedIndex == 0) {
                modelSettingsUi.Fab()
            }
        }) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .padding(10.dp)
            ) {
                when (selectedIndex) {
                    0 -> modelSettingsUi.Content()
                    1 -> UISettingsUi()
                    2 -> LogicSettingsUi()
                }
            }
        }
    }

    fun importModel() {
        val intent = Intent()
        intent.type = "application/zip"
        intent.action = Intent.ACTION_GET_CONTENT
        //launch picker screen
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val intent: Intent = result.data ?: return@registerForActivityResult
                FileDownloader.importModel(intent.data!!, this)
            }
        }

    private fun checkPermissions() {
        micGranted.postValue(Tools.isMicrophonePermissionGranted(this))
        imeGranted.postValue(Tools.isIMEEnabled(this))
    }

    override fun onStart() {
        super.onStart()
        modelSettingsUi.onStart()
    }

    override fun onStop() {
        modelSettingsUi.onStop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        modelSettingsUi.onResume()
    }

    companion object {
        /* Used to handle permission request */
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
        public const val PERMISSION_REQUEST_POST_NOTIFICATIONS = 1

//        private const val FILE_PICKER_REQUEST_CODE = 1
    }

    @Preview
    @Composable
    fun DefaultPreview() {
        modelSettingsUi.models.postValue(listOf(LocalModel("abc/def", Locale.ENGLISH, "english")))
        MainUi()
    }
}