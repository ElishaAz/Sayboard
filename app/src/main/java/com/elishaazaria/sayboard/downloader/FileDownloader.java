//package com.elishaazaria.sayboard.downloader;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.ResultReceiver;
//
//public class FileDownloader extends ResultReceiver {
//
//    private OnDownloadStatusListener onDownloadStatusListener;
//
//    public static FileDownloader getInstance(OnDownloadStatusListener downloadStatusListener) {
//
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        FileDownloader fileDownloader = new FileDownloader(handler);
//
//        fileDownloader.onDownloadStatusListener = downloadStatusListener;
//
//        return fileDownloader;
//    }
//
//    public void download(DownloadRequest downloadDetails, Context context) {
//        if (DownloaderTools.isOnline(context)) {
//            Intent intent = new Intent(context, FileDownloadIntentService.class);
//            intent.putExtra(Communication.DOWNLOADER_RECEIVER, this);
//            intent.putExtra(Communication.DOWNLOAD_DETAILS, downloadDetails);
//            context.startService(intent);
//        }
//    }
//
//    private FileDownloader(Handler handler) {
//
//        super(handler);
//    }
//
//    @Override
//    protected void onReceiveResult(int resultCode, Bundle resultData) {
//
//        super.onReceiveResult(resultCode, resultData);
//
//        if (onDownloadStatusListener == null) {
//
//            return;
//        }
//
//        if (resultCode == Communication.STATUS_OK) {
//
//            if (resultData.containsKey(Communication.DOWNLOAD_STARTED)
//                    && resultData.getBoolean(Communication.DOWNLOAD_STARTED)) {
//
//                onDownloadStatusListener.onDownloadStarted();
//
//            } else if (resultData.containsKey(Communication.DOWNLOAD_COMPLETED)
//                    && resultData.getBoolean(Communication.DOWNLOAD_COMPLETED)) {
//
//                onDownloadStatusListener.onDownloadCompleted();
//
//            } else if (resultData.containsKey(Communication.DOWNLOAD_PROGRESS)) {
//
//                int progress = resultData.getInt(Communication.DOWNLOAD_PROGRESS);
//                onDownloadStatusListener.onDownloadProgress(progress);
//
//            }
//
//        } else if (resultCode == Communication.STATUS_FAILED) {
//
//            onDownloadStatusListener.onDownloadFailed();
//        }
//    }
//
//    public void setOnDownloadStatusListener(OnDownloadStatusListener onDownloadStatusListener) {
//
//        this.onDownloadStatusListener = onDownloadStatusListener;
//    }
//
//}
