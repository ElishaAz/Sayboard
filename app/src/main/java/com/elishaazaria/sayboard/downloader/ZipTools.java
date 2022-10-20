package com.elishaazaria.sayboard.downloader;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.elishaazaria.sayboard.Tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipTools {
    public static void unzip(File archive, File tempUnzipLocation, File unzipFinalDestination, Observer<Double> progressObserver) throws IOException {
        if (tempUnzipLocation.exists()) {
            Tools.deleteRecursive(tempUnzipLocation);
        }

        ZipFile zipfile = new ZipFile(archive);

        Enumeration<? extends ZipEntry> e = zipfile.entries();
        double size = zipfile.size();

        for (int i = 0; e.hasMoreElements(); i++) {
            progressObserver.onChanged(i / size);
            ZipEntry entry = (ZipEntry) e.nextElement();

            unzipEntry(zipfile, entry, tempUnzipLocation.getAbsolutePath());
        }
        boolean moveSuccess;
        if (unzipFinalDestination.exists()) {
            moveSuccess = true;
            for (File f : tempUnzipLocation.listFiles()) {
                moveSuccess = f.renameTo(new File(unzipFinalDestination, f.getName()));
                if (!moveSuccess) break;
            }
            tempUnzipLocation.delete();
        } else {
            moveSuccess = tempUnzipLocation.renameTo(unzipFinalDestination);
        }

        if (!moveSuccess) {
            throw new IOException("Renaming temporary unzip directory failed");
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        Log.v("ZIP E", "Extracting: " + entry);

        try (InputStream zin = zipfile.getInputStream(entry)) {
            try (BufferedInputStream inputStream = new BufferedInputStream(zin)) {
                try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                    byte[] b = new byte[1024];
                    int n;
                    while ((n = inputStream.read(b, 0, 1024)) >= 0) {
                        outputStream.write(b, 0, n);
                    }
                }
            }
        }
    }

    private static void createDir(File dir) {

        if (dir.exists()) {
            return;
        }

        Log.v("ZIP E", "Creating dir " + dir.getName());

        if (!dir.mkdirs()) {

            throw new RuntimeException("Can not create dir " + dir);
        }
    }
}
