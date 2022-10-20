package com.elishaazaria.sayboard.downloader.messages;

public class UnzipProgress {
    public final ModelInfo info;

    public final int progress;

    public UnzipProgress(ModelInfo info, int progress) {
        this.info = info;
        this.progress = progress;
    }
}
