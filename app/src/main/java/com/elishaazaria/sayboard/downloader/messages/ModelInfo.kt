package com.elishaazaria.sayboard.downloader.messages

import java.util.*

class ModelInfo(val url: String, val filename: String, val locale: Locale) {
    override fun toString(): String {
        return "ModelInfo{" +
                "url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                ", locale=" + locale +
                '}'
    }
}