package com.elishaazaria.sayboard.utils

import com.elishaazaria.sayboard.data.InstalledModelReference
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ModelListSerializer : PreferenceSerializer<List<InstalledModelReference>> {
    private val serializer = ListSerializer(InstalledModelReference.serializer())
    override fun deserialize(value: String): List<InstalledModelReference> {
        return Json.decodeFromString(serializer, value)
    }

    override fun serialize(value: List<InstalledModelReference>): String {
        return Json.encodeToString(serializer, value)
    }

}