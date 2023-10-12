package com.elishaazaria.sayboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.elishaazaria.sayboard.SettingsActivity
import com.elishaazaria.sayboard.Tools
import com.elishaazaria.sayboard.data.LocalModel
import com.elishaazaria.sayboard.data.ModelLink
import com.elishaazaria.sayboard.downloader.FileDownloader
import com.elishaazaria.sayboard.downloader.messages.CancelCurrent
import com.elishaazaria.sayboard.downloader.messages.CancelFinished
import com.elishaazaria.sayboard.downloader.messages.CancelPending
import com.elishaazaria.sayboard.downloader.messages.DownloadError
import com.elishaazaria.sayboard.downloader.messages.DownloadProgress
import com.elishaazaria.sayboard.downloader.messages.DownloadState
import com.elishaazaria.sayboard.downloader.messages.ModelInfo
import com.elishaazaria.sayboard.downloader.messages.State
import com.elishaazaria.sayboard.downloader.messages.Status
import com.elishaazaria.sayboard.downloader.messages.StatusQuery
import com.elishaazaria.sayboard.downloader.messages.UnzipProgress
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class DownloadModelProgress(
    val info: ModelInfo,
    var state: State, var progress: Float
) {
    fun withProgress(newProgress: Float): DownloadModelProgress {
        return DownloadModelProgress(info, state, newProgress)
    }

    fun withState(newState: State): DownloadModelProgress {
        return DownloadModelProgress(info, newState, progress)
    }
}

class ModelsSettingsUi(private val activity: SettingsActivity) {

    val models = MutableLiveData<List<LocalModel>>(mutableListOf())

    private val modelsPendingDownloadLD = MutableLiveData<List<ModelInfo>>(mutableListOf())
    private val modelsPendingDownload = mutableListOf<ModelInfo>()

    private val currentDownloadingModel = MutableLiveData<DownloadModelProgress?>()

    fun onCreate() {
        reloadModels()
    }

    private fun reloadModels() {
        Log.d(TAG, "Reloading Models")
        models.postValue(Tools.getInstalledModelsList(activity))
    }


    @Composable
    fun Content() {
        val modelsState = models.observeAsState(listOf())
        val modelsPendingDownload = modelsPendingDownloadLD.observeAsState(mutableListOf())
        val currentDownloadingModel = currentDownloadingModel.observeAsState()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                currentDownloadingModel.value?.let { current ->
                    Card {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = current.info.locale.displayName, fontSize = 20.sp)
                                Text(text = current.info.url, fontSize = 12.sp)
                                val stateText = when (current.state) {
                                    State.NONE -> "Unknown"
                                    State.QUEUED -> "Pending"
                                    State.DOWNLOAD_STARTED -> "Downloading"
                                    State.DOWNLOAD_FINISHED -> "Download Finished"
                                    State.UNZIP_STARTED -> "Unzipping"
                                    State.UNZIP_FINISHED -> "Unzipping Finished"
                                    State.FINISHED -> "Finished"
                                    State.ERROR -> "Error"
                                    State.CANCELED -> "Canceled"
                                }

                                Text(
                                    text = "State: $stateText", fontSize = 14.sp
                                )
                            }
                            FilledIconButton(onClick = {
                                EventBus.getDefault()
                                    .post(
                                        CancelCurrent(current.info)
                                    )
                            }, shape = ShapeDefaults.Medium) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null
                                )
                            }
                        }
                        LinearProgressIndicator(current.progress)
                    }
                }
            }
            items(items = modelsPendingDownload.value) {
                Card {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = it.locale.displayName, fontSize = 20.sp)
                            Text(text = it.url, fontSize = 12.sp)
                            Text(text = "State: Pending Download", fontSize = 14.sp)
                        }
                        FilledIconButton(onClick = {
                            EventBus.getDefault()
                                .post(
                                    CancelPending(it)
                                )
                        }, shape = ShapeDefaults.Medium) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                        }
                    }
                }
            }

            items(items = modelsState.value) { lm ->
                Card {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = lm.locale.displayName ?: "null", fontSize = 20.sp)
                            Text(text = lm.path, fontSize = 12.sp)
                        }
                        FilledIconButton(onClick = {
                            Tools.deleteModel(lm, activity)
                            reloadModels()
                        }, shape = ShapeDefaults.Medium) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Fab() {
        var showDownloadDialog by remember {
            mutableStateOf(false)
        }

        var showImportDialog by remember {
            mutableStateOf(false)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(onClick = {
                showDownloadDialog = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        activity.applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            activity, arrayOf(
                                Manifest.permission.POST_NOTIFICATIONS
                            ), SettingsActivity.PERMISSION_REQUEST_POST_NOTIFICATIONS
                        )
//                        Toast.makeText(
//                            activity,
//                            "Notifications recommended for downloader to work properly",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }
            }) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
            }

            FloatingActionButton(onClick = {
                showImportDialog = true
            }) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = null)
            }
        }

        if (showDownloadDialog) {
            AlertDialog(onDismissRequest = {
                showDownloadDialog = false
            }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(
                    Modifier
                        .clip(RectangleShape)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Download Model", fontSize = 30.sp)

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(items = ModelLink.values()) { ml ->
                                Card(onClick = {
                                    showDownloadDialog = false
                                    FileDownloader.downloadModel(ml, activity)
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = ml.locale.displayName)
                                        Text(text = ml.link, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                showDownloadDialog = false
                            }, modifier = Modifier.align(Alignment.CenterEnd)) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
            }
        }

        if (showImportDialog) {
            AlertDialog(onDismissRequest = {

            }, confirmButton = {
                Button(onClick = {
                    showImportDialog = false
                    activity.importModel()
                }) {
                    Text(text = "Import")
                }
            }, dismissButton = {
                Button(onClick = { showImportDialog = false }) {
                    Text(text = "Cancel")
                }
            }, title = {
                Text(text = "Import Model")
            }, text = {
                val annotatedString = buildAnnotatedString {
                    append("Manually download a Vosk model from ")

                    pushStringAnnotation(tag = "vosk-website", annotation = "https://alphacephei.com/vosk/models")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("the Vosk website")
                    }
                    pop()

                    append(" - make sure to pick a \"small\" model - and then choose it in the file picker after pressing import")
                }

                ClickableText(text = annotatedString, style = MaterialTheme.typography.bodyMedium, onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "vosk-website", start = offset, end = offset).firstOrNull()?.let {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                        activity.startActivity(browserIntent)
                    }
                })
            })
        }
    }

    private fun updateCurrentDownloading(info: ModelInfo): DownloadModelProgress {
        var currentModel = currentDownloadingModel.value

        if (currentModel == null) {
            currentModel = DownloadModelProgress(
                info,
                State.NONE,
                0f
            )
        } else if (currentModel.info != info) {
            val newCurrent = modelsPendingDownload.find { it == currentModel!!.info }
            if (newCurrent == null) {
                currentModel = DownloadModelProgress(
                    info,
                    State.NONE,
                    0f
                )
            } else {
                modelsPendingDownload.remove(newCurrent)
                modelsPendingDownloadLD.postValue(modelsPendingDownload.toList())
                currentModel = DownloadModelProgress(
                    newCurrent,
                    State.NONE,
                    0f
                )
            }
        }
        currentDownloadingModel.postValue(currentModel)
        return currentModel
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAddToDownload(info: ModelInfo) {
        Log.d(TAG, "onAddToDownload($info)")

        if (modelsPendingDownload.contains(info)) return
        modelsPendingDownload.add(info)
        modelsPendingDownloadLD.postValue(modelsPendingDownload.toList())
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onState(state: DownloadState) {
        Log.d(TAG, "onState($state)")
        when (state.state) {
            State.QUEUED -> onAddToDownload(state.info)
            State.DOWNLOAD_STARTED, State.NONE -> {
                modelsPendingDownload.remove(state.info)
                modelsPendingDownloadLD.postValue(modelsPendingDownload.toList())
                currentDownloadingModel.postValue(
                    updateCurrentDownloading(state.info).withState(
                        state.state
                    )
                )
            }

            State.FINISHED -> {
                currentDownloadingModel.postValue(null)
                reloadModels()
            }

            else -> {
                currentDownloadingModel.postValue(
                    updateCurrentDownloading(state.info).withState(
                        state.state
                    )
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onStatus(status: Status) {
        Log.d(TAG, "onStatus($status)")
        if (status.current != null) {
            onState(DownloadState(status.current, status.state))
            when (status.state) {
                State.DOWNLOAD_STARTED -> onDownloadProgress(
                    DownloadProgress(
                        status.current,
                        status.downloadProgress
                    )
                )

                State.UNZIP_STARTED -> onUnzipProgress(
                    UnzipProgress(
                        status.current,
                        status.unzipProgress
                    )
                )

                else -> {}
            }
        } else {
            currentDownloadingModel.postValue(null)
        }
        modelsPendingDownload.clear()
        for (modelInfo in status.queued) {
            onState(DownloadState(modelInfo, State.QUEUED))
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDownloadProgress(progress: DownloadProgress) {
//        Log.d(TAG, "onDownloadProgress($progress)")
        // CG wasteful, but works
        currentDownloadingModel.postValue(
            updateCurrentDownloading(progress.info).withProgress(
                progress.progress
            )
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onUnzipProgress(progress: UnzipProgress) {
//        Log.d(TAG, "onUnzipProgress($progress)")
        currentDownloadingModel.postValue(
            updateCurrentDownloading(progress.info).withProgress(
                progress.progress
            )
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onCancelFinished(event: CancelFinished) {
        if (currentDownloadingModel.value?.info == event.info) {
            currentDownloadingModel.postValue(null)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadError(error: DownloadError) {
        Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
    }

    fun onStart() {
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(StatusQuery())
    }

    fun onStop() {
        EventBus.getDefault().unregister(this)
    }

    fun onResume() {
        reloadModels()
        EventBus.getDefault().post(StatusQuery())
    }

    companion object {
        private const val TAG = "ModelsSettingUi"
    }
}