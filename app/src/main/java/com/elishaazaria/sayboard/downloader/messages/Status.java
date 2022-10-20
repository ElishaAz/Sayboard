package com.elishaazaria.sayboard.downloader.messages;

import java.util.Queue;

public class Status {
    public final ModelInfo current;
    public final Queue<ModelInfo> queued;
    public final int downloadProgress;
    public final int unzipProgress;
    public final State state;

    public Status(ModelInfo current, Queue<ModelInfo> queued, int downloadProgress, int unzipProgress, State state) {
        this.current = current;
        this.queued = queued;
        this.downloadProgress = downloadProgress;
        this.unzipProgress = unzipProgress;
        this.state = state;
    }
}
