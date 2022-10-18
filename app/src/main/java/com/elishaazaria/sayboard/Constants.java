package com.elishaazaria.sayboard;

import android.content.Context;

import java.io.File;
import java.util.Locale;

public class Constants {
    public static File getTemporaryDownloadLocation(Context context){
        return new File(context.getCacheDir().getAbsolutePath(), "ModelZips");
    }

    public static File getTemporaryUnzipLocation(Context context) {
        return new File(context.getCacheDir(), "TempUnzip");
    }

    public static File getModelsDirectory(Context context){
        return new File(context.getFilesDir().getAbsolutePath(), "Models");
    }

    public static File getDirectoryForModel(Context context, Locale locale){
        File dataFolder = Constants.getModelsDirectory(context);
        String folderName = locale.toLanguageTag();
        return new File(dataFolder, folderName);
    }
}
