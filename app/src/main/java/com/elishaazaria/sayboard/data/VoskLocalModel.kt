package com.elishaazaria.sayboard.data

import java.io.Serializable
import java.util.*

data class VoskLocalModel(val path: String, val locale: Locale, val filename: String) : Serializable {

    companion object {
        fun serialize(model: VoskLocalModel): String {
            return "[path:\"" + encode(model.path) +
                    "\", locale:\"" + model.locale +
                    "\", name:\"" + encode(model.filename) + "\"]"
        }

        fun deserialize(serialized: String?): VoskLocalModel {

            throw RuntimeException() // TODO: implement
        }

        private fun encode(s: String?): String {
            val sb = StringBuilder()
            var c: Char
            for (i in 0 until s!!.length) {
                c = s[i]
                when (c) {
                    ',', '"', '\\', ':' -> {
                        sb.append("\\")
                        sb.append(String.format("%02x", c.code))
                    }

                    else -> sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun decode(s: String): String {
            val sb = StringBuilder()
            var c: Char
            var i = 0
            while (i < s.length) {
                c = s[i]
                if (c == '\\') {
                    i++
                    sb.append(s.substring(i, i + 2).toInt().toChar())
                    i += 2
                } else {
                    sb.append(c)
                }
                i++
            }
            return sb.toString()
        }
    }
}