package com.elishaazaria.sayboard.downloader

import android.util.Log
import androidx.lifecycle.Observer
import com.elishaazaria.sayboard.Tools.deleteRecursive
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object ZipTools {
    @Throws(IOException::class)
    fun unzip(
        archive: File?,
        tempUnzipLocation: File,
        unzipFinalDestination: File,
        progressObserver: Observer<Double>
    ) {
        if (tempUnzipLocation.exists()) {
            deleteRecursive(tempUnzipLocation)
        }
        val zipfile = ZipFile(archive)
        val e = zipfile.entries()
        val size = zipfile.size().toDouble()
        var i = 0
        while (e.hasMoreElements()) {
            progressObserver.onChanged(i / size)
            val entry = e.nextElement() as ZipEntry
            unzipEntry(zipfile, entry, tempUnzipLocation.absolutePath)
            i++
        }
        var moveSuccess: Boolean
        if (unzipFinalDestination.exists()) {
            moveSuccess = true
            for (f in tempUnzipLocation.listFiles()!!) {
                moveSuccess = f.renameTo(File(unzipFinalDestination, f.name))
                if (!moveSuccess) break
            }
            tempUnzipLocation.delete()
        } else {
            moveSuccess = tempUnzipLocation.renameTo(unzipFinalDestination)
        }
        if (!moveSuccess) {
            throw IOException("Renaming temporary unzip directory failed")
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