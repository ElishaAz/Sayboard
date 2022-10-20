package com.elishaazaria.sayboard.downloader.messages;

public class DownloadState {
    public final ModelInfo info;
    public final State state;

    public DownloadState(ModelInfo info, State state) {
        this.info = info;
        this.state = state;
    }
}
