package com.elishaazaria.sayboard

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.elishaazaria.sayboard.Constants.getModelsDirectory
import com.elishaazaria.sayboard.data.LocalModel
import com.elishaazaria.sayboard.data.ModelLink
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapterLocalData
import java.io.File
import java.util.*

object Tools {
    @JvmStatic
    fun isMicrophonePermissionGranted(activity: Activity): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            activity.applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun isIMEEnabled(activity: Activity): Boolean {
        val imeManager =
            activity.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        for (i in imeManager.enabledInputMethodList) {
            if (i.packageName == activity.packageName) {
                return true
            }
        }
        return false
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
    @JvmStatic
    fun deleteModel(model: LocalModel, context: Context?) {
        val modelFile = File(model.path)
        if (modelFile.exists()) deleteRecursive(modelFile)
    }

    @JvmStatic
    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    fun getInstalledModelsMap(context: Context?): Map<Locale, MutableList<LocalModel>> {
        val localeMap: MutableMap<Locale, MutableList<LocalModel>> = HashMap()
        val modelsDir = getModelsDirectory(context!!)
        if (!modelsDir.exists()) return localeMap
        for (localeFolder in modelsDir.listFiles()) {
            if (!localeFolder.isDirectory) continue
            val locale = Locale.forLanguageTag(localeFolder.name)
            val models: MutableList<LocalModel> = ArrayList()
            for (modelFolder in localeFolder.listFiles()) {
                if (!modelFolder.isDirectory) continue
                val name = modelFolder.name
                val model = LocalModel(modelFolder.absolutePath, locale, name)
                models.add(model)
            }
            localeMap[locale] = models
        }
        return localeMap
    }

    @JvmStatic
    fun getInstalledModelsList(context: Context?): List<LocalModel> {
        val models: MutableList<LocalModel> = ArrayList()
        val modelsDir = getModelsDirectory(context!!)
        if (!modelsDir.exists()) return models
        for (localeFolder in modelsDir.listFiles()) {
            if (!localeFolder.isDirectory) continue
            val locale = Locale.forLanguageTag(localeFolder.name)
            for (modelFolder in localeFolder.listFiles()) {
                if (!modelFolder.isDirectory) continue
                val name = modelFolder.name
                val model = LocalModel(modelFolder.absolutePath, locale, name)
                models.add(model)
            }
        }
        return models
    }

    @JvmStatic
    fun getModelsData(context: Context?): List<ModelsAdapterLocalData> {
        val data: MutableList<ModelsAdapterLocalData> = ArrayList()
        val installedModels = getInstalledModelsMap(context)
        for (link in ModelLink.values()) {
            var found = false
            if (installedModels.containsKey(link.locale)) {
                val localeModels = installedModels[link.locale]!!
                for (i in localeModels.indices) {
                    val model = localeModels[i]
                    if (model.filename == link.filename) {
                        data.add(ModelsAdapterLocalData(link, model))
                        localeModels.removeAt(i)
                        found = true
                        break
                    }
                }
            }
            if (!found) data.add(ModelsAdapterLocalData(link))
        }
        for (models in installedModels.values) {
            for (model in models) {
                data.add(ModelsAdapterLocalData(model))
            }
        }
        return data
    }

    @JvmStatic
    fun getModelForLink(modelLink: ModelLink, context: Context?): LocalModel? {
        val modelsDir = getModelsDirectory(context!!)
        val localeDir = File(modelsDir, modelLink.locale.toLanguageTag())
        val modelDir = File(localeDir, modelLink.filename)
        return if (!localeDir.exists() || !modelDir.exists() || !modelDir.isDirectory) {
            null
        } else LocalModel(modelDir.absolutePath, modelLink.locale, modelLink.filename)
    }

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.notification_download_channel_name)
            val description = context.getString(R.string.notification_download_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(Constants.DOWNLOADER_CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}