package com.elishaazaria.sayboard.downloader.messages;

public enum State {
    NONE, QUEUED, DOWNLOAD_STARTED, DOWNLOAD_FINISHED, UNZIP_STARTED, UNZIP_FINISHED, FINISHED,
    ERROR
}
