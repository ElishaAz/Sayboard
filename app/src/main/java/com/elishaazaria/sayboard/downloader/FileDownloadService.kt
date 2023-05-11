package com.elishaazaria.sayboard.downloader

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elishaazaria.sayboard.Constants
import com.elishaazaria.sayboard.Constants.getDirectoryForModel
import com.elishaazaria.sayboard.Constants.getTemporaryDownloadLocation
import com.elishaazaria.sayboard.Constants.getTemporaryUnzipLocation
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.downloader.messages.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.*
import java.net.URL
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FileDownloadService : Service() {
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var currentModel: ModelInfo? = null
    private val queuedModels: Queue<ModelInfo> = LinkedList()
    private var currentState = State.NONE
    private var downloadProgress = 0
    private var unzipProgress = 0
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        notificationManager = NotificationManagerCompat.from(this)
        notificationBuilder = NotificationCompat.Builder(this, Constants.DOWNLOADER_CHANNEL_ID)
        notificationBuilder.setContentTitle(getString(R.string.notification_download_title))
            .setContentText(getString(R.string.notification_download_content_unknown))
            .setSmallIcon(R.drawable.ic_notification).priority = NotificationCompat.PRIORITY_LOW
        notificationBuilder.setProgress(0, 0, true)
        notificationBuilder.foregroundServiceBehavior =
            NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val modelInfo = FileDownloader.getInfoForIntent(intent)
            ?: return START_NOT_STICKY
        Log.d(TAG, "Got message $modelInfo")
        queuedModels.add(modelInfo)
        executor.execute { main() }
        sendEnqueued(modelInfo)
        startForeground(notificationId, notificationBuilder.build())
        return START_NOT_STICKY
    }

    private fun main() {
        currentModel = queuedModels.poll()
        val currentModelS = currentModel ?: return
        Log.d(TAG, "Started processing $currentModelS")
        downloadProgress = 0
        unzipProgress = 0
        setState(State.NONE)
        val downloadLocation = getTemporaryDownloadLocation(
            applicationContext, currentModelS.filename
        )
        if (!downloadLocation.parentFile!!.exists()) {
            downloadLocation.parentFile!!.mkdirs()
        }
        try {
            downloadFile(downloadLocation)
        } catch (e: IOException) {
            e.printStackTrace()
            setError(e.message)
            mainEnd()
            return
        }
        Log.d(TAG, "Finished downloading")
        try {
            unzipFile(downloadLocation)
        } catch (e: IOException) {
            e.printStackTrace()
            setError(e.message)
            mainEnd()
            return
        }
        Log.d(TAG, "Finished unzipping")
        downloadLocation.delete()
        setState(State.FINISHED)
        Log.d(TAG, "Finished processing $currentModel")
        mainEnd()
    }

    private fun mainEnd() {
        downloadProgress = 0
        unzipProgress = 0
        currentState = State.NONE
        currentModel = null
        if (queuedModels.isEmpty()) stopForeground(false)
    }

    @Throws(IOException::class)
    private fun downloadFile(downloadLocation: File) {
        setState(State.DOWNLOAD_STARTED)
        val url = URL(currentModel!!.url)
        val urlConnection = url.openConnection()
        urlConnection.connect()
        val lengthOfFile = urlConnection.contentLength
        Log.d("TAG", "Length of file: $lengthOfFile")
        setDownloadProgress(0)
        val input: InputStream = BufferedInputStream(url.openStream())
        val output: OutputStream = FileOutputStream(downloadLocation)
        val data = ByteArray(1024) // 1mb
        var total: Long = 0
        var count: Int
        while (input.read(data).also { count = it } != -1) {
            total += count.toLong()
            setDownloadProgress((total * PROGRESS_MAX / lengthOfFile).toInt())
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        input.close()
        setDownloadProgress(PROGRESS_MAX)
        setState(State.DOWNLOAD_FINISHED)
    }

    @Throws(IOException::class)
    private fun unzipFile(downloadLocation: File) {
        setState(State.UNZIP_STARTED)
        val unzipDestination = getDirectoryForModel(
            applicationContext, currentModel!!.locale
        )
        if (!unzipDestination.exists()) {
            unzipDestination.mkdirs()
        }
        val unzipFolder = getTemporaryUnzipLocation(this)
        val currentUnzipFolder = File(unzipFolder, unzipDestination.name)
        ZipTools.unzip(
            downloadLocation,
            currentUnzipFolder,
            unzipDestination
        ) { d: Double -> setUnzipProgress((d * PROGRESS_MAX).toInt()) }
        setUnzipProgress(PROGRESS_MAX)
        setState(State.UNZIP_FINISHED)
    }

    private var lastDownloadProgress = 0
    private var lastUnzipProgress = 0
    private var lastState: State = State.NONE
    private var lastUpdateTime: Long = 0
    private fun updateNotification() {
        if (lastDownloadProgress == downloadProgress && lastUnzipProgress == unzipProgress && lastState == currentState) // nothing changed
            return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < minUpdateTime) {
            if (lastState == currentState) { // it's a progress update
                if (!(downloadProgress == PROGRESS_MAX && unzipProgress == 0) &&
                    unzipProgress != PROGRESS_MAX
                ) { // it's not the last progress update
                    lastUpdateTime = currentTime
                    return
                }
            }
        }
        lastUpdateTime = currentTime
        when (currentState) {
            State.NONE -> notificationBuilder.setContentText(getString(R.string.notification_download_content_unknown))
                .setProgress(0, 0, true)
            State.DOWNLOAD_STARTED, State.DOWNLOAD_FINISHED -> notificationBuilder.setContentText(
                getString(R.string.notification_download_content_downloading)
            )
                .setProgress(PROGRESS_MAX, downloadProgress, false)
            State.UNZIP_STARTED, State.UNZIP_FINISHED -> notificationBuilder.setContentText(
                getString(R.string.notification_download_content_unzipping)
            )
                .setProgress(PROGRESS_MAX, unzipProgress, false)
            State.FINISHED -> notificationBuilder.setContentText(getString(R.string.notification_download_content_finished))
                .setProgress(0, 0, false)
            State.ERROR -> notificationBuilder.setContentText(getString(R.string.notification_download_content_error))
                .setProgress(0, 0, false)
            State.QUEUED -> TODO()
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
        lastDownloadProgress = downloadProgress
        lastUnzipProgress = unzipProgress
        lastState = currentState
    }

    private fun setState(state: State) {
        currentState = state
        EventBus.getDefault().post(DownloadState(currentModel!!, state))
        updateNotification()
    }

    private fun setDownloadProgress(progress: Int) {
        downloadProgress = progress
        EventBus.getDefault().post(DownloadProgress(currentModel!!, downloadProgress))
        updateNotification()
    }

    private fun setUnzipProgress(progress: Int) {
        unzipProgress = progress
        EventBus.getDefault().post(UnzipProgress(currentModel!!, unzipProgress))
        updateNotification()
    }

    private fun setError(message: String?) {
        setState(State.ERROR)
        EventBus.getDefault().post(DownloadError(currentModel!!, message ?: ""))
        updateNotification()
    }

    private fun sendEnqueued(modelInfo: ModelInfo) {
        EventBus.getDefault().post(DownloadState(modelInfo, State.QUEUED))
    }

    @Subscribe
    fun handleStatusQuery(event: StatusQuery?) {
        EventBus.getDefault()
            .post(Status(currentModel!!, queuedModels, downloadProgress, unzipProgress, currentState))
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val TAG = "FileDownloadService"
        private const val notificationId = 1
        private const val PROGRESS_MAX = 100
        private const val minUpdateTime = (1000 / 5 // 5 updates a second;
                ).toLong()
    }
}