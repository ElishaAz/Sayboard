//package com.elishaazaria.sayboard.downloader;
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.ResultReceiver;
//import android.util.Log;
//
//import com.elishaazaria.sayboard.Constants;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
//import java.net.URLConnection;
//
///**
// * Code from: https://stackoverflow.com/questions/9324103/download-and-extract-zip-file-in-android
// * <p>
// * Created by Vaibhav.Jani on 6/4/15.
// */
//public class FileDownloadIntentService extends IntentService {
//
//    public FileDownloadIntentService() {
//
//        super("FileDownloadService");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//        Bundle bundle = intent.getExtras();
//
//        if (bundle == null
//                || !bundle.containsKey(Communication.DOWNLOADER_RECEIVER)
//                || !bundle.containsKey(Communication.DOWNLOAD_DETAILS)) {
//
//            return;
//        }
//
//        ResultReceiver resultReceiver = bundle.getParcelable(Communication.DOWNLOADER_RECEIVER);
//
//        DownloadRequest downloadDetails = bundle.getParcelable(Communication.DOWNLOAD_DETAILS);
//
//        try {
//
//            assert downloadDetails != null;
//            URL url = new URL(downloadDetails.getServerFilePath());
//
//            URLConnection urlConnection = url.openConnection();
//
//            urlConnection.connect();
//
//            int lengthOfFile = urlConnection.getContentLength();
//
//            Log.d("FileDownloaderService", "Length of file: " + lengthOfFile);
//            downloadStarted(resultReceiver);
//
//            InputStream input = new BufferedInputStream(url.openStream());
//
//            String localPath = downloadDetails.getLocalFilePath();
//
//            OutputStream output = new FileOutputStream(localPath);
//
//            byte[] data = new byte[1024];
//
//            long total = 0;
//
//            int count;
//
//            while ((count = input.read(data)) != -1) {
//
//                total += count;
//
//                int progress = (int) ((total * 100) / lengthOfFile);
//
//                sendProgress(progress, resultReceiver);
//
//                output.write(data, 0, count);
//            }
//
//            output.flush();
//            output.close();
//            input.close();
//
//            if (downloadDetails.isRequiresUnzip()) {
//
//                File unzipDestination;
//                if (downloadDetails.getUnzipAtFilePath() == null) {
//
//                    File file = new File(localPath);
//
//                    unzipDestination = file.getParentFile();
//                } else {
//
//                    unzipDestination = new File(downloadDetails.getUnzipAtFilePath());
//                }
//
//
//                File unzipFolder = Constants.getTemporaryUnzipLocation(this);
//                File currentUnzipFolder = new File(unzipFolder, unzipDestination.getName());
//
//                ZipTools.unzip(localPath, currentUnzipFolder, unzipDestination);
//            }
//
//            downloadCompleted(resultReceiver);
//
//            if (downloadDetails.isDeleteZipAfterExtract()) {
//
//                File file = new File(localPath);
//                file.delete();
//            }
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            downloadFailed(resultReceiver);
//        }
//    }
//
//    public void sendProgress(int progress, ResultReceiver receiver) {
//
//        Bundle progressBundle = new Bundle();
//        progressBundle.putInt(Communication.DOWNLOAD_PROGRESS, progress);
//        receiver.send(Communication.STATUS_OK, progressBundle);
//    }
//
//    public void downloadStarted(ResultReceiver resultReceiver) {
//
//        Bundle progressBundle = new Bundle();
//        progressBundle.putBoolean(Communication.DOWNLOAD_STARTED, true);
//        resultReceiver.send(Communication.STATUS_OK, progressBundle);
//    }
//
//    public void downloadCompleted(ResultReceiver resultReceiver) {
//
//        Bundle progressBundle = new Bundle();
//        progressBundle.putBoolean(Communication.DOWNLOAD_COMPLETED, true);
//        resultReceiver.send(Communication.STATUS_OK, progressBundle);
//    }
//
//    public void downloadFailed(ResultReceiver resultReceiver) {
//
//        Bundle progressBundle = new Bundle();
//        progressBundle.putBoolean(Communication.DOWNLOAD_FAILED, true);
//        resultReceiver.send(Communication.STATUS_FAILED, progressBundle);
//    }
//}
