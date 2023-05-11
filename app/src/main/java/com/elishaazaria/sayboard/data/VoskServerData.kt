package com.elishaazaria.sayboard.data

import java.io.Serializable
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class VoskServerData(
    val uri: URI, // placeholder for now
    val locale: Locale?
) : Serializable, Comparable<VoskServerData> {
    override fun compareTo(other: VoskServerData): Int {
        return uri.compareTo(other.uri)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as VoskServerData
        return uri == that.uri && locale == that.locale
    }

    override fun hashCode(): Int {
        return Objects.hash(uri, locale)
    }

    companion object {
        fun serialize(data: VoskServerData): String {
            return data.uri.toString()
        }

        fun deserialize(data: String?): VoskServerData? {
            return try {
                VoskServerData(URI(data), null)
            } catch (e: URISyntaxException) {
                null
            }
        }
    }
}