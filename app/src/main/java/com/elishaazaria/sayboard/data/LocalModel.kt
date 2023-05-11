package com.elishaazaria.sayboard.data

import java.io.Serializable
import java.util.*

class LocalModel(val path: String?, val locale: Locale?, val filename: String?) : Serializable {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val model = o as LocalModel
        if (path != model.path) return false
        return if (locale != model.locale) false else filename == model.filename
    }

    override fun hashCode(): Int {
        var result = path?.hashCode() ?: 0
        result = 31 * result + (locale?.hashCode() ?: 0)
        result = 31 * result + (filename?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun serialize(model: LocalModel): String {
            return "[path:\"" + encode(model.path) +
                    "\", locale:\"" + model.locale +
                    "\", name:\"" + encode(model.filename) + "\"]"
        }

        fun deserialize(serialized: String?): LocalModel {
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