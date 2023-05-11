package com.elishaazaria.sayboard.settingsfragments.modelsfragment

import android.content.Context
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.VoskServerData
import com.elishaazaria.sayboard.preferences.ModelPreferences

class ModelsAdapterServerData(private val data: VoskServerData) : ModelsAdapterData {
    override val title: String
        get() = data.uri.toString()
    override val subtitle: String
        get() = "vosk server"
    override val imageRes: Int
        get() = R.drawable.ic_delete

    override fun buttonClicked(adapter: ModelsAdapter, context: Context) {
        ModelPreferences.removeFromVoskServers(data)
        adapter.removed(this)
    }
}