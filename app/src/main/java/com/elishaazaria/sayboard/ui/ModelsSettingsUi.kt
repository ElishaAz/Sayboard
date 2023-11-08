package com.elishaazaria.sayboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.SettingsActivity
import com.elishaazaria.sayboard.Tools
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
import com.elishaazaria.sayboard.ime.recognizers.providers.Providers
import com.elishaazaria.sayboard.sayboardPreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class DownloadModelProgress(
    val info: ModelInfo, var state: State, var progress: Float
) {
    fun withProgress(newProgress: Float): DownloadModelProgress {
        return DownloadModelProgress(info, state, newProgress)
    }

    fun withState(newState: State): DownloadModelProgress {
        return DownloadModelProgress(info, newState, progress)
    }
}

class ModelsSettingsUi(private val activity: SettingsActivity) {
    private val prefs by sayboardPreferenceModel()

    private val modelsPendingDownloadLD = MutableLiveData<List<ModelInfo>>(mutableListOf())
    private val modelsPendingDownload = mutableListOf<ModelInfo>()

    private val currentDownloadingModel = MutableLiveData<DownloadModelProgress?>()

    private val recognizerSourceProviders = Providers(activity)

    fun onCreate() {
        reloadModels()
    }

    private fun reloadModels() {
        Log.d(TAG, "Reloading Models")
        val currentModels = prefs.modelsOrder.get().toMutableList()
        val installedModels = recognizerSourceProviders.installedModels()
        currentModels.removeAll { it !in installedModels }
        for (model in installedModels) {
            if (model !in currentModels) {
                currentModels.add(model)
            }
        }
        prefs.modelsOrder.set(currentModels)
    }


    @Composable
    fun Content() {
        val modelsPendingDownload by modelsPendingDownloadLD.observeAsState(mutableListOf())
        val currentDownloadingModel by currentDownloadingModel.observeAsState()
        val modelOrder by prefs.modelsOrder.observeAsState()

        var modelOrderData by remember {
            mutableStateOf(modelOrder)
        }
        modelOrderData = modelOrder

        val state = rememberReorderableLazyListState(onMove = { from, to ->
            // one for currently downloading, one for the separator, and all the pending downloads
            val fromIndex = from.index - modelsPendingDownload.size - 2
            val toIndex = to.index - modelsPendingDownload.size - 2
            if (fromIndex < 0 || toIndex < 0) return@rememberReorderableLazyListState

            prefs.modelsOrder.set(modelOrderData.toMutableList().apply {
                add(
                    toIndex, removeAt(fromIndex)
                )
            })
        })
        Log.d("ModelsSettingsUi", modelOrderData.joinToString())
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            item {
                currentDownloadingModel?.let { current ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Card {
                            Column {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = current.info.locale.displayName,
                                            fontSize = 20.sp
                                        )
                                        Text(text = current.info.url, fontSize = 12.sp)
                                        val stateText = stringResource(
                                            id = when (current.state) {
                                                State.NONE -> R.string.models_download_state_unknown
                                                State.QUEUED -> R.string.models_download_state_pending
                                                State.DOWNLOAD_STARTED -> R.string.models_download_state_download_started
                                                State.DOWNLOAD_FINISHED -> R.string.models_download_state_download_finished
                                                State.UNZIP_STARTED -> R.string.models_download_state_unzip_started
                                                State.UNZIP_FINISHED -> R.string.models_download_state_unzip_finished
                                                State.FINISHED -> R.string.models_download_state_finished
                                                State.ERROR -> R.string.models_download_state_error
                                                State.CANCELED -> R.string.models_download_state_canceled
                                            }
                                        )

                                        Text(
                                            text = stringResource(id = R.string.models_download_state).format(
                                                stateText
                                            ), fontSize = 14.sp
                                        )
                                    }
                                    IconButton(onClick = {
                                        EventBus.getDefault().post(
                                            CancelCurrent(current.info)
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = null
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    current.progress, modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            items(items = modelsPendingDownload) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {

                    Card {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = it.locale.displayName, fontSize = 20.sp)
                                Text(text = it.url, fontSize = 12.sp)
                                Text(
                                    text = stringResource(id = R.string.models_pending_download_state),
                                    fontSize = 14.sp
                                )
                            }
                            IconButton(onClick = {
                                EventBus.getDefault().post(
                                    CancelPending(it)
                                )
                            }) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                            }
                        }
                    }
                }
            }

            item {
                if (currentDownloadingModel != null || modelsPendingDownload.isNotEmpty()) {
                    Spacer(
                        modifier = Modifier
                            .padding(10.dp, 5.dp)
                            .background(MaterialTheme.colors.primary)
                            .fillMaxWidth()
                            .height(2.dp)
                    )
                }
            }

            items(modelOrderData, { it.path }) { item ->
                ReorderableItem(
                    state, key = item.path, modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                    Card(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .padding(10.dp)
                        ) {

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name
                                        ?: stringResource(id = R.string.models_model_display_name_null),
                                    fontSize = 20.sp
                                )
                                Text(text = item.path, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                Tools.deleteModel(item, activity)
                                reloadModels()
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun Fab() {
        val modelOrder by prefs.modelsOrder.observeAsState()
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
                        activity.applicationContext, Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            activity, arrayOf(
                                Manifest.permission.POST_NOTIFICATIONS
                            ), SettingsActivity.PERMISSION_REQUEST_POST_NOTIFICATIONS
                        )
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
            JetPrefAlertDialog(
                title = stringResource(id = R.string.models_download_dialog_title),
                onDismiss = { showDownloadDialog = false },
                dismissLabel = stringResource(id = R.string.button_cancel),
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, (LocalConfiguration.current.screenHeightDp * 0.9).dp)
                ) {
                    items(items = ModelLink.values().filter { ml ->
                        modelOrder.none {
                            ml.link.substring(
                                ml.link.lastIndexOf('/'),
                                ml.link.lastIndexOf('.')
                            ) == it.path.substring(
                                it.path.lastIndexOf('/')
                            )
                        }
                    }) { ml ->
                        Card(
                            onClick = {
                                showDownloadDialog = false
                                FileDownloader.downloadModel(ml, activity)
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colors.onSurface.copy(0.2f))
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(MaterialTheme.colors.onSurface.copy(0.2f))
                                    .padding(10.dp)
                            ) {
                                Text(text = ml.locale.displayName)
                                Text(text = ml.link, fontSize = 10.sp)
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
                    Text(text = stringResource(id = R.string.models_import_dialog_import))
                }
            }, dismissButton = {
                Button(onClick = { showImportDialog = false }) {
                    Text(text = stringResource(id = R.string.button_cancel))
                }
            }, title = {
                Text(text = stringResource(id = R.string.models_import_dialog_title))
            }, text = {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.onSurface)) {
                        append(stringResource(id = R.string.models_import_dialog_text_before_link))
                    }

                    pushStringAnnotation(
                        tag = "vosk-website", annotation = "https://alphacephei.com/vosk/models"
                    )
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                        append(stringResource(id = R.string.models_import_dialog_text_link))
                    }
                    pop()

                    withStyle(style = SpanStyle(color = MaterialTheme.colors.onSurface)) {
                        append(stringResource(id = R.string.models_import_dialog_text_after_link))
                    }
                }

                ClickableText(text = annotatedString,
                    style = MaterialTheme.typography.body1,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "vosk-website", start = offset, end = offset
                        ).firstOrNull()?.let {
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
                info, State.NONE, 0f
            )
        } else if (currentModel.info != info) {
            val newCurrent = modelsPendingDownload.find { it == currentModel!!.info }
            if (newCurrent == null) {
                currentModel = DownloadModelProgress(
                    info, State.NONE, 0f
                )
            } else {
                modelsPendingDownload.remove(newCurrent)
                modelsPendingDownloadLD.postValue(modelsPendingDownload.toList())
                currentModel = DownloadModelProgress(
                    newCurrent, State.NONE, 0f
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
                        status.current, status.downloadProgress
                    )
                )

                State.UNZIP_STARTED -> onUnzipProgress(
                    UnzipProgress(
                        status.current, status.unzipProgress
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