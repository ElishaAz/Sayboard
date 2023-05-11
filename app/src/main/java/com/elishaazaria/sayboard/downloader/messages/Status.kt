package com.elishaazaria.sayboard.downloader.messages

import java.util.*

class Status(
    val current: ModelInfo,
    val queued: Queue<ModelInfo>,
    val downloadProgress: Int,
    val unzipProgress: Int,
    val state: State
)