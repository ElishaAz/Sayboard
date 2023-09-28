package com.elishaazaria.sayboard.downloader.messages

enum class State {
    NONE, QUEUED, DOWNLOAD_STARTED, DOWNLOAD_FINISHED, UNZIP_STARTED, UNZIP_FINISHED, FINISHED, ERROR, CANCELED
}