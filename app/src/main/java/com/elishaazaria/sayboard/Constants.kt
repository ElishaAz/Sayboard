package com.elishaazaria.sayboard

import android.content.Context
import android.os.Environment
import java.io.File
import java.util.*

object Constants {
    public val UndefinedLocale = Locale("und")

    // TODO make configurable
    val BackspaceRepeatStartDelay: Long = 500
    val BackspaceRepeatDelay: Long = 100

    @JvmField
    var DOWNLOADER_CHANNEL_ID = "downloader"
    private fun getCacheDir(context: Context): File {
        return if (Environment.isExternalStorageEmulated() || !Environment.isExternalStorageRemovable()) {
            context.externalCacheDir!!
        } else {
            context.cacheDir
        }
    }

    private fun getFilesDir(context: Context): File {
        return if (Environment.isExternalStorageEmulated() || !Environment.isExternalStorageRemovable()) {
            context.getExternalFilesDir(null)!!
        } else {
            context.filesDir
        }
    }

    private fun getTempDir(context: Context): File {
        return File(getFilesDir(context).absolutePath, "Temp")
    }

    @JvmStatic
    fun getTemporaryDownloadLocation(context: Context, filename: String): File {
        val dir = File(
            getTempDir(context).absolutePath, "ModelZips"
        )
        return File(dir, filename)
    }

    @JvmStatic
    fun getTemporaryUnzipLocation(context: Context): File {
        return File(File(getTempDir(context), "TempUnzip"), "Folder")
    }

    @JvmStatic
    fun getModelsDirectory(context: Context): File {
        return File(getFilesDir(context).absolutePath, "Models")
    }

    @JvmStatic
    fun getDirectoryForModel(context: Context, locale: Locale): File {
        val dataFolder = getModelsDirectory(context)
        val folderName = locale.toLanguageTag()
        return File(dataFolder, folderName)
    }
}