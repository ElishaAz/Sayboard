package com.elishaazaria.sayboard.downloader.messages;

public class DownloadError {
    public final ModelInfo info;

    public final String message;

    public DownloadError(ModelInfo info, String message) {
        this.info = info;
        this.message = message;
    }
}
