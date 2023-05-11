package com.elishaazaria.sayboard.data

import androidx.annotation.StringRes
import com.elishaazaria.sayboard.R
import java.util.*

// Locale list available at: https://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android
/**
 *
 */
enum class ModelLink(
    val link: String,
    val locale: Locale,
    @param:StringRes val nameRes: Int
) {
    ENGLISH_US(
        "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
        Locale.US,
        R.string.model_en_us
    ),
    ENGLISH_IN(
        "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip",
        Locale("en", "IN"),
        R.string.model_en_in
    ),
    CHINESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
        Locale.CHINESE,
        R.string.model_cn
    ),
    RUSSIAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip",
        Locale("ru"),
        R.string.model_ru
    ),
    FRENCH(
        "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip",
        Locale.FRENCH,
        R.string.model_fr
    ),
    GERMAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip",
        Locale.GERMAN,
        R.string.model_de
    ),
    SPANISH(
        "https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip",
        Locale("es"),
        R.string.model_es
    ),
    PORTUGUESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip",
        Locale("pt"),
        R.string.model_pt
    ),
    TURKISH(
        "https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip",
        Locale("tr"),
        R.string.model_tr
    ),
    VIETNAMESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-vn-0.3.zip",
        Locale("vi"),
        R.string.model_vi
    ),
    ITALIAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-it-0.22.zip",
        Locale.ITALIAN,
        R.string.model_it
    ),
    DUTCH(
        "https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip",
        Locale("nl"),
        R.string.model_nl
    ),
    CATALAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip",
        Locale("ca"),
        R.string.model_nl
    ),
    PERSIAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.4.zip",
        Locale("fa"),
        R.string.model_fa
    ),
    KAZAKH(
        "https://alphacephei.com/vosk/models/vosk-model-small-kz-0.15.zip",
        Locale("kk"),
        R.string.model_kk
    ),
    JAPANESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip",
        Locale.JAPANESE,
        R.string.model_nl
    ),
    ESPERANTO(
        "https://alphacephei.com/vosk/models/vosk-model-small-eo-0.42.zip",
        Locale("eo"),
        R.string.model_nl
    ),
    HINDI(
        "https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip",
        Locale("hi"),
        R.string.model_hi
    ),
    CZECH(
        "https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip",
        Locale("cs"),
        R.string.model_cs
    ),
    POLISH(
        "https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip",
        Locale("pl"),
        R.string.model_pl
    );

    val filename: String
        get() = link.substring(link.lastIndexOf('/') + 1, link.lastIndexOf('.'))
}