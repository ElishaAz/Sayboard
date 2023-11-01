package com.elishaazaria.sayboard.utils

import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class Key(val label: String, val text: String)

class KeysListSerializer : PreferenceSerializer<List<Key>> {
    private val serializer = ListSerializer(Key.serializer())
    override fun deserialize(value: String): List<Key> {
        return Json.decodeFromString(serializer, value)
    }

    override fun serialize(value: List<Key>): String {
        return Json.encodeToString(serializer, value)
    }

}

val leftDefaultKeysList = listOf(Key(",", ","), Key(".", "."), Key("?", "?"))
val rightDefaultKeysList = listOf(Key("!", "!"), Key("\"", "\""), Key("'", "'"))