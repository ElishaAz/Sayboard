package com.elishaazaria.sayboard.downloader;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.elishaazaria.sayboard.Model;
import com.elishaazaria.sayboard.ModelLink;
import com.elishaazaria.sayboard.downloader.messages.ModelInfo;

import java.util.Locale;

public class Communication {
    public static final String DOWNLOADER_RECEIVER = "downloader_receiver";
    public static final String DOWNLOAD_DETAILS = "download_details";
    public static final String DOWNLOAD_STARTED = "download_started";
    public static final String DOWNLOAD_FAILED = "download_failed";
    public static final String DOWNLOAD_COMPLETED = "download_completed";
    public static final String DOWNLOAD_PROGRESS = "download_progress";

    public static final int STATUS_OK = 100;
    public static final int STATUS_FAILED = 200;

    public static final String DOWNLOAD_URL = "download_url";
    public static final String DOWNLOAD_FILENAME = "download_filename";
    public static final String DOWNLOAD_LOCALE = "download_locale";

    static ModelInfo getInfoForIntent(Intent intent) {
        String url = intent.getStringExtra(DOWNLOAD_URL);
        String filename = intent.getStringExtra(DOWNLOAD_FILENAME);
        Locale locale = (Locale) intent.getSerializableExtra(DOWNLOAD_LOCALE);
        if (url == null || filename == null || locale == null) return null;
        return new ModelInfo(url, filename, locale);
    }

    public static void downloadModel(ModelLink model, Context context) {
        Intent serviceIntent = new Intent(context, FileDownloadService.class);

        serviceIntent.putExtra(DOWNLOAD_URL, model.link);
        serviceIntent.putExtra(DOWNLOAD_FILENAME, model.getFilename());
        serviceIntent.putExtra(DOWNLOAD_LOCALE, model.locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
