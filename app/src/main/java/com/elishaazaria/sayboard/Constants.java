package com.elishaazaria.sayboard;

import android.content.Context;
import android.os.Environment;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Locale;

public class Constants {
    public static String DOWNLOADER_CHANNEL_ID = "downloader";

    private static File getCacheDir(Context context) {
        if (Environment.isExternalStorageEmulated() || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir();
        } else {
            return context.getCacheDir();
        }
    }

    private static File getFilesDir(Context context) {
        if (Environment.isExternalStorageEmulated() || !Environment.isExternalStorageRemovable()) {
            return context.getExternalFilesDir(null);
        } else {
            return context.getFilesDir();
        }
    }

    public static File getTemporaryDownloadLocation(Context context, String filename) {
        File dir = new File(getCacheDir(context).getAbsolutePath(), "ModelZips");
        return new File(dir, filename);
    }

    public static File getTemporaryUnzipLocation(Context context) {
        return new File(getCacheDir(context), "TempUnzip");
    }

    public static File getModelsDirectory(Context context) {
        return new File(getFilesDir(context).getAbsolutePath(), "Models");
    }

    public static File getDirectoryForModel(Context context, Locale locale) {
        File dataFolder = Constants.getModelsDirectory(context);
        String folderName = locale.toLanguageTag();
        return new File(dataFolder, folderName);
    }
}
