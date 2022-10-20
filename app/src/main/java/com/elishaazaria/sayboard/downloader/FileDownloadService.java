package com.elishaazaria.sayboard.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.elishaazaria.sayboard.Constants;
import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.downloader.messages.DownloadError;
import com.elishaazaria.sayboard.downloader.messages.DownloadProgress;
import com.elishaazaria.sayboard.downloader.messages.DownloadState;
import com.elishaazaria.sayboard.downloader.messages.ModelInfo;
import com.elishaazaria.sayboard.downloader.messages.State;
import com.elishaazaria.sayboard.downloader.messages.Status;
import com.elishaazaria.sayboard.downloader.messages.StatusQuery;
import com.elishaazaria.sayboard.downloader.messages.UnzipProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileDownloadService extends Service {
    private static final String TAG = "FileDownloadService";
    private static final int notificationId = 1;
    private static final int PROGRESS_MAX = 100;
    private static final long minUpdateTime = 1000 / 5; // 5 updates a second;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private ModelInfo currentModel;
    private final Queue<ModelInfo> queuedModels = new LinkedList<>();
    private State currentState = State.NONE;
    private int downloadProgress = 0;
    private int unzipProgress;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        notificationManager = NotificationManagerCompat.from(this);
        notificationBuilder = new NotificationCompat.Builder(this, Constants.DOWNLOADER_CHANNEL_ID);
        notificationBuilder.setContentTitle(getString(R.string.notification_download_title))
                .setContentText(getString(R.string.notification_download_content_unknown))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationBuilder.setProgress(0, 0, true);
        notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ModelInfo modelInfo = FileDownloader.getInfoForIntent(intent);
        if (modelInfo == null) return START_NOT_STICKY;

        Log.d(TAG, "Got message " + modelInfo);

        queuedModels.add(modelInfo);
        executor.execute(this::main);

        sendEnqueued(modelInfo);

        startForeground(notificationId, notificationBuilder.build());

        return START_NOT_STICKY;
    }

    private void main() {
        currentModel = queuedModels.poll();

        Log.d(TAG, "Started processing " + currentModel);

        downloadProgress = 0;
        unzipProgress = 0;
        setState(State.NONE);

        File downloadLocation = Constants.getTemporaryDownloadLocation(getApplicationContext(), currentModel.filename);

        if (!downloadLocation.getParentFile().exists()) {
            downloadLocation.getParentFile().mkdirs();
        }

        try {
            downloadFile(downloadLocation);
        } catch (IOException e) {
            e.printStackTrace();
            setError(e.getMessage());
            mainEnd();
            return;
        }

        Log.d(TAG, "Finished downloading");

        try {
            unzipFile(downloadLocation);
        } catch (IOException e) {
            e.printStackTrace();
            setError(e.getMessage());
            mainEnd();
            return;
        }

        Log.d(TAG, "Finished unzipping");

        downloadLocation.delete();

        setState(State.FINISHED);

        Log.d(TAG, "Finished processing " + currentModel);
        mainEnd();
    }

    private void mainEnd() {
        downloadProgress = 0;
        unzipProgress = 0;
        currentState = State.NONE;
        currentModel = null;

        if (queuedModels.isEmpty())
            stopForeground(false);
    }

    private void downloadFile(File downloadLocation) throws IOException {
        setState(State.DOWNLOAD_STARTED);
        URL url = new URL(currentModel.url);
        URLConnection urlConnection = url.openConnection();

        urlConnection.connect();

        int lengthOfFile = urlConnection.getContentLength();

        Log.d("TAG", "Length of file: " + lengthOfFile);

        setDownloadProgress(0);

        InputStream input = new BufferedInputStream(url.openStream());

        OutputStream output = new FileOutputStream(downloadLocation);

        byte[] data = new byte[1024]; // 1mb

        long total = 0;

        int count;

        while ((count = input.read(data)) != -1) {

            total += count;
            setDownloadProgress((int) ((total * PROGRESS_MAX) / lengthOfFile));

            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();

        setDownloadProgress(PROGRESS_MAX);
        setState(State.DOWNLOAD_FINISHED);
    }

    private void unzipFile(File downloadLocation) throws IOException {
        setState(State.UNZIP_STARTED);
        File unzipDestination = Constants.getDirectoryForModel(getApplicationContext(), currentModel.locale);

        if (!unzipDestination.exists()) {
            unzipDestination.mkdirs();
        }

        File unzipFolder = Constants.getTemporaryUnzipLocation(this);
        File currentUnzipFolder = new File(unzipFolder, unzipDestination.getName());

        ZipTools.unzip(downloadLocation, currentUnzipFolder, unzipDestination, (d) -> setUnzipProgress((int) (d * PROGRESS_MAX)));
        setUnzipProgress(PROGRESS_MAX);
        setState(State.UNZIP_FINISHED);
    }

    private int lastDownloadProgress;
    private int lastUnzipProgress;
    private State lastState;

    private long lastUpdateTime;

    private void updateNotification() {
        if (lastDownloadProgress == downloadProgress &&
                lastUnzipProgress == unzipProgress &&
                lastState == currentState) // nothing changed
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < minUpdateTime) {
            if (lastState == currentState) { // it's a progress update
                if (!(downloadProgress == PROGRESS_MAX && unzipProgress == 0) &&
                        !(unzipProgress == PROGRESS_MAX)) { // it's not the last progress update
                    lastUpdateTime = currentTime;
                    return;
                }
            }
        }
        lastUpdateTime = currentTime;

        switch (currentState) {
            case NONE:
                notificationBuilder.setContentText(getString(R.string.notification_download_content_unknown))
                        .setProgress(0, 0, true);
                break;
            case DOWNLOAD_STARTED:
            case DOWNLOAD_FINISHED:
                notificationBuilder.setContentText(getString(R.string.notification_download_content_downloading))
                        .setProgress(PROGRESS_MAX, downloadProgress, false);
                break;
            case UNZIP_STARTED:
            case UNZIP_FINISHED:
                notificationBuilder.setContentText(getString(R.string.notification_download_content_unzipping))
                        .setProgress(PROGRESS_MAX, unzipProgress, false);
                break;
            case FINISHED:
                notificationBuilder.setContentText(getString(R.string.notification_download_content_finished))
                        .setProgress(0, 0, false);
                break;
            case ERROR:
                notificationBuilder.setContentText(getString(R.string.notification_download_content_error))
                        .setProgress(0, 0, false);
                break;
        }

        notificationManager.notify(notificationId, notificationBuilder.build());

        lastDownloadProgress = downloadProgress;
        lastUnzipProgress = unzipProgress;
        lastState = currentState;
    }

    private void setState(State state) {
        currentState = state;
        EventBus.getDefault().post(new DownloadState(currentModel, state));
        updateNotification();
    }

    private void setDownloadProgress(int progress) {
        downloadProgress = progress;
        EventBus.getDefault().post(new DownloadProgress(currentModel, downloadProgress));
        updateNotification();
    }

    private void setUnzipProgress(int progress) {
        unzipProgress = progress;
        EventBus.getDefault().post(new UnzipProgress(currentModel, unzipProgress));
        updateNotification();
    }

    private void setError(String message) {
        setState(State.ERROR);
        EventBus.getDefault().post(new DownloadError(currentModel, message));
        updateNotification();
    }

    private void sendEnqueued(ModelInfo modelInfo) {
        EventBus.getDefault().post(new DownloadState(modelInfo, State.QUEUED));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleStatusQuery(StatusQuery event) {
        EventBus.getDefault().post(new Status(currentModel, queuedModels, downloadProgress, unzipProgress, currentState));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        EventBus.getDefault().unregister(this);
    }
}
