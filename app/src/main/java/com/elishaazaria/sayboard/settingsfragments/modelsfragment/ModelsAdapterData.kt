package com.elishaazaria.sayboard.settingsfragments.modelsfragment

import android.content.Context

interface ModelsAdapterData {
    val title: String
    val subtitle: String
    val imageRes: Int
    fun buttonClicked(adapter: ModelsAdapter, context: Context)
}