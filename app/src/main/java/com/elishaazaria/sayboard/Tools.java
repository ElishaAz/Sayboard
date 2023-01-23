package com.elishaazaria.sayboard;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.elishaazaria.sayboard.data.LocalModel;
import com.elishaazaria.sayboard.data.ModelLink;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapterLocalData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Tools {

    public static boolean isMicrophonePermissionGranted(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isIMEEnabled(Activity activity) {
        InputMethodManager imeManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        for (InputMethodInfo i : imeManager.getEnabledInputMethodList()) {
            if (i.getPackageName().equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

//    public static void downloadModelFromLink(ModelLink model, OnDownloadStatusListener listener, Context context) {
//        String serverFilePath = model.link;
//
//        File tempFolder = Constants.getTemporaryDownloadLocation(context);
//        if (!tempFolder.exists()) {
//            tempFolder.mkdirs();
//        }
//
//        String fileName = model.link.substring(model.link.lastIndexOf('/') + 1); // file name
//        File tempFile = new File(tempFolder, fileName);
//
//        String localPath = tempFile.getAbsolutePath();
//
//        File modelFolder = Constants.getDirectoryForModel(context, model.locale);
//
//        if (!modelFolder.exists()) {
//            modelFolder.mkdirs();
//        }
//
//        String unzipPath = modelFolder.getAbsolutePath();
//
//        DownloadRequest downloadRequest = new DownloadRequest(serverFilePath, localPath, true);
//        downloadRequest.setRequiresUnzip(true);
//        downloadRequest.setDeleteZipAfterExtract(true);
//        downloadRequest.setUnzipAtFilePath(unzipPath);
//
//        FileDownloader downloader = FileDownloader.getInstance(listener);
//        downloader.download(downloadRequest, context);
//    }

    public static void deleteModel(LocalModel model, Context context) {
        File modelFile = new File(model.path);

        if (modelFile.exists())
            deleteRecursive(modelFile);
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static Map<Locale, List<LocalModel>> getInstalledModelsMap(Context context) {
        Map<Locale, List<LocalModel>> localeMap = new HashMap<>();

        File modelsDir = Constants.getModelsDirectory(context);

        if (!modelsDir.exists()) return localeMap;

        for (File localeFolder : modelsDir.listFiles()) {
            if (!localeFolder.isDirectory()) continue;
            Locale locale = Locale.forLanguageTag(localeFolder.getName());
            List<LocalModel> models = new ArrayList<>();
            for (File modelFolder : localeFolder.listFiles()) {
                if (!modelFolder.isDirectory()) continue;
                String name = modelFolder.getName();
                LocalModel model = new LocalModel(modelFolder.getAbsolutePath(), locale, name);
                models.add(model);
            }
            localeMap.put(locale, models);
        }
        return localeMap;
    }

    public static List<LocalModel> getInstalledModelsList(Context context) {
        List<LocalModel> models = new ArrayList<>();

        File modelsDir = Constants.getModelsDirectory(context);

        if (!modelsDir.exists()) return models;

        for (File localeFolder : modelsDir.listFiles()) {
            if (!localeFolder.isDirectory()) continue;
            Locale locale = Locale.forLanguageTag(localeFolder.getName());
            for (File modelFolder : localeFolder.listFiles()) {
                if (!modelFolder.isDirectory()) continue;
                String name = modelFolder.getName();
                LocalModel model = new LocalModel(modelFolder.getAbsolutePath(), locale, name);
                models.add(model);
            }
        }
        return models;
    }

    public static List<ModelsAdapterLocalData> getModelsData(Context context) {
        List<ModelsAdapterLocalData> data = new ArrayList<>();
        Map<Locale, List<LocalModel>> installedModels = getInstalledModelsMap(context);
        for (ModelLink link : ModelLink.values()) {
            boolean found = false;
            if (installedModels.containsKey(link.locale)) {
                List<LocalModel> localeModels = installedModels.get(link.locale);
                for (int i = 0; i < localeModels.size(); i++) {
                    LocalModel model = localeModels.get(i);
                    if (model.filename.equals(link.getFilename())) {
                        data.add(new ModelsAdapterLocalData(link, model));
                        localeModels.remove(i);
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                data.add(new ModelsAdapterLocalData(link));
        }
        for (List<LocalModel> models : installedModels.values()) {
            for (LocalModel model : models) {
                data.add(new ModelsAdapterLocalData(model));
            }
        }

        return data;
    }

    public static LocalModel getModelForLink(ModelLink modelLink, Context context) {
        File modelsDir = Constants.getModelsDirectory(context);
        File localeDir = new File(modelsDir, modelLink.locale.toLanguageTag());
        File modelDir = new File(localeDir, modelLink.getFilename());
        if (!localeDir.exists() || !modelDir.exists() || !modelDir.isDirectory()) {
            return null;
        }
        return new LocalModel(modelDir.getAbsolutePath(), modelLink.locale, modelLink.getFilename());
    }

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_download_channel_name);
            String description = context.getString(R.string.notification_download_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Constants.DOWNLOADER_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
