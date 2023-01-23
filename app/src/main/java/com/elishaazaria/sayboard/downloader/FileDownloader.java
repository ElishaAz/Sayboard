package com.elishaazaria.sayboard.downloader;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.elishaazaria.sayboard.data.ModelLink;
import com.elishaazaria.sayboard.downloader.messages.ModelInfo;

import java.util.Locale;

public class FileDownloader {
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
        context = context.getApplicationContext();

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
