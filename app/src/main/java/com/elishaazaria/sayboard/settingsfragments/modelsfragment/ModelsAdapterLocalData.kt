package com.elishaazaria.sayboard.settingsfragments.modelsfragment

import android.content.Context
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.Tools.deleteModel
import com.elishaazaria.sayboard.data.LocalModel
import com.elishaazaria.sayboard.data.ModelLink
import com.elishaazaria.sayboard.downloader.FileDownloader
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapter.DataState
import java.util.*

class ModelsAdapterLocalData : ModelsAdapterData {
    var modelLink: ModelLink?
        private set
    var model: LocalModel?
        private set
    var state: DataState
        private set

    constructor(modelLink: ModelLink?) {
        this.modelLink = modelLink
        model = null
        state = DataState.CLOUD
    }

    constructor(model: LocalModel?) {
        modelLink = null
        this.model = model
        state = DataState.INSTALLED
    }

    constructor(modelLink: ModelLink?, model: LocalModel?) {
        this.modelLink = modelLink
        this.model = model
        state = DataState.INSTALLED
    }

    val filename: String
        get() = if (modelLink != null) {
            modelLink!!.filename
        } else if (model != null) {
            model!!.filename!!
        } else {
            "Undefined"
        }
    val locale: Locale
        get() = if (model != null) {
            model!!.locale!!
        } else if (modelLink != null) {
            modelLink!!.locale
        } else Locale.forLanguageTag("und")

    fun wasInstalled(model: LocalModel?) {
        this.model = model
        state = DataState.INSTALLED
    }

    fun wasDeleted(): Boolean {
        model = null
        state = DataState.CLOUD
        return modelLink == null
    }

    fun wasQueued() {
        state = DataState.QUEUED
    }

    fun downloading() {
        state = DataState.DOWNLOADING
    }

    fun downloadCanceled() {
        if (state == DataState.DOWNLOADING) state = DataState.CLOUD
    }

    override val title: String
        get() = locale.displayName
    override val subtitle: String
        get() = filename
    override val imageRes: Int
        get() {
            return when (state) {
                DataState.CLOUD -> R.drawable.ic_download
                DataState.INSTALLED -> R.drawable.ic_delete
                DataState.DOWNLOADING -> R.drawable.ic_downloading
                DataState.QUEUED -> R.drawable.ic_add_circle_outline
            }
            return 0
        }

    override fun buttonClicked(adapter: ModelsAdapter, context: Context) {
        when (state) {
            DataState.CLOUD -> FileDownloader.downloadModel(modelLink!!, context)
            DataState.INSTALLED -> {
                deleteModel(model!!, context)
                val removed = wasDeleted()
                if (removed) {
                    adapter.removed(this)
                } else {
                    adapter.changed(this)
                }
            }
            DataState.DOWNLOADING -> {}
            DataState.QUEUED -> {}
        }
    }
}