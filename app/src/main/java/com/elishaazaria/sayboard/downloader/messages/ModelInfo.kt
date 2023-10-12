package com.elishaazaria.sayboard.downloader.messages

import java.util.*

data class ModelInfo(val url: String, val filename: String, val locale: Locale = Locale.ROOT) {
//    override fun toString(): String {
//        return "ModelInfo{" +
//                "url='" + url + '\'' +
//                ", filename='" + filename + '\'' +
//                ", locale=" + locale +
//                '}'
//    }
}