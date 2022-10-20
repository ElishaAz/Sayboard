package com.elishaazaria.sayboard.downloader.messages;

public class DownloadProgress {
    public final ModelInfo info;

    public final int progress;

    public DownloadProgress(ModelInfo info, int progress) {
        this.info = info;
        this.progress = progress;
    }
}
