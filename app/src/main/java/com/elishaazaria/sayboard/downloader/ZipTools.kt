package com.elishaazaria.sayboard.downloader

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.Constants
import com.elishaazaria.sayboard.Tools.deleteRecursive
import java.io.*
import java.util.Locale
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

object ZipTools {
    private const val TAG = "ZipTools"
    private val localePattern: Pattern =
        Pattern.compile("vosk-model-(small-)?(\\w\\w(-\\w\\w)?)-(\\w+-)?v?\\d\\.?\\d*.*")

    @Throws(IOException::class)
    fun unzip(
        archive: File,
//        tempUnzipLocation: File,
//        unzipFinalDestination: File,
        definedLocale: Locale = Constants.UndefinedLocale,
        context: Context,
        errorObserver: Observer<String>? = null,
        progressObserver: Observer<Double>
    ) {
        var locale = definedLocale
        val tempUnzipLocation = Constants.getTemporaryUnzipLocation(context)

        if (!tempUnzipLocation.parentFile!!.exists()) {
            tempUnzipLocation.parentFile!!.mkdirs()
        }

        if (tempUnzipLocation.exists()) {
            deleteRecursive(tempUnzipLocation)
        }
        val zipfile = try {
            ZipFile(archive)
        } catch (e: ZipException) {
            return
        }
        val e = zipfile.entries()
        val size = zipfile.size().toDouble()

        var foundAmFinalMDL = false
        var i = 0
        while (e.hasMoreElements()) {
            progressObserver.onChanged(i / size)
            val entry = e.nextElement() as ZipEntry
            if (locale == Constants.UndefinedLocale) {
                Log.d(TAG, "Trying to detect locale: ${entry.name}")
                val matcher = localePattern.matcher(entry.name)
                if (matcher.matches()) {
                    locale = Locale.forLanguageTag(matcher.group(2)!!)
                    Log.d(TAG, "Locale detected: ${locale.toLanguageTag()}")
                }
            }

            // Some tests to make sure it actually is a Vosk model
            if (!foundAmFinalMDL && entry.name.endsWith("/am/final.mdl")) {
                foundAmFinalMDL = true
            }

            // outdated, but final.mdl might be elsewhere
            if (!foundAmFinalMDL && entry.name.endsWith("/final.mdl")) {
                foundAmFinalMDL = true
            }

            unzipEntry(zipfile, entry, tempUnzipLocation.absolutePath)
            i++
        }

        if (!foundAmFinalMDL) {
            // Not a Vosk model!
            Log.e(TAG, "Not a Vosk model: ${archive.absolutePath}")
            errorObserver?.onChanged("Zip is not a Vosk model!")
            tempUnzipLocation.delete()
            return
        }

        val unzipFinalDestination = Constants.getDirectoryForModel(
            context, locale
        )
        if (!unzipFinalDestination.exists()) {
            unzipFinalDestination.mkdirs()
        }

        Log.d(TAG, "Unzipping finished. Moving to ${unzipFinalDestination.absolutePath}")

//        var moveSuccess: Boolean
//        if (unzipFinalDestination.exists()) {
//            moveSuccess = true
//            for (f in tempUnzipLocation.listFiles()!!) {
//                moveSuccess = f.renameTo(File(unzipFinalDestination, f.name))
//                if (!moveSuccess) break
//            }
//            tempUnzipLocation.delete()
//        } else {
//            moveSuccess = tempUnzipLocation.renameTo(unzipFinalDestination)
//        }

        val moveSuccess = tempUnzipLocation.renameTo(unzipFinalDestination)
        if (!moveSuccess) {
            Log.e(TAG, "Model exists at ${unzipFinalDestination.absolutePath}")
            errorObserver?.onChanged("Model exists")
            tempUnzipLocation.delete()
        }
    }

    @Throws(IOException::class)
    private fun unzipEntry(zipfile: ZipFile, entry: ZipEntry, outputDir: String) {
        if (entry.isDirectory) {
            createDir(File(outputDir, entry.name))
            return
        }
        val outputFile = File(outputDir, entry.name)
        if (!outputFile.parentFile!!.exists()) {
            createDir(outputFile.parentFile!!)
        }
        Log.v("ZIP E", "Extracting: $entry")
        zipfile.getInputStream(entry).use { zin ->
            BufferedInputStream(zin).use { inputStream ->
                BufferedOutputStream(
                    FileOutputStream(outputFile)
                ).use { outputStream ->
                    val b = ByteArray(1024)
                    var n: Int
                    while (inputStream.read(b, 0, 1024).also { n = it } >= 0) {
                        outputStream.write(b, 0, n)
                    }
                }
            }
        }
    }

    private fun createDir(dir: File) {
        if (dir.exists()) {
            return
        }
        Log.v("ZIP E", "Creating dir " + dir.name)
        if (!dir.mkdirs()) {
            throw RuntimeException("Can not create dir $dir")
        }
    }
}