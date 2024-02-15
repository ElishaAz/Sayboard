package com.elishaazaria.sayboard.downloader.messages

import com.elishaazaria.sayboard.Constants
import java.util.*

data class ModelInfo(val url: String, val filename: String, val locale: Locale = Constants.UndefinedLocale) {
//    override fun toString(): String {
//        return "ModelInfo{" +
//                "url='" + url + '\'' +
//                ", filename='" + filename + '\'' +
//                ", locale=" + locale +
//                '}'
//    }
}