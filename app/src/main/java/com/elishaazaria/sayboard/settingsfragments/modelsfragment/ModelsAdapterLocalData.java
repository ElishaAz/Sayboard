package com.elishaazaria.sayboard.settingsfragments.modelsfragment;

import android.content.Context;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.data.LocalModel;
import com.elishaazaria.sayboard.data.ModelLink;
import com.elishaazaria.sayboard.downloader.FileDownloader;

import java.util.Locale;

public class ModelsAdapterLocalData implements ModelsAdapterData {
    private ModelLink modelLink;
    private LocalModel model;
    private ModelsAdapter.DataState state;

    public ModelsAdapterLocalData(ModelLink modelLink) {
        this.modelLink = modelLink;
        this.model = null;
        state = ModelsAdapter.DataState.CLOUD;
    }

    public ModelsAdapterLocalData(LocalModel model) {
        this.modelLink = null;
        this.model = model;
        state = ModelsAdapter.DataState.INSTALLED;
    }

    public ModelsAdapterLocalData(ModelLink modelLink, LocalModel model) {
        this.modelLink = modelLink;
        this.model = model;
        state = ModelsAdapter.DataState.INSTALLED;
    }

    public String getFilename() {
        if (modelLink != null) {
            return modelLink.getFilename();
        } else if (model != null) {
            return model.filename;
        } else {
            return "Undefined";
        }
    }

    public Locale getLocale() {
        if (model != null) {
            return model.locale;
        } else if (modelLink != null) {
            return modelLink.locale;
        } else return Locale.forLanguageTag("und");
    }

    public void wasInstalled(LocalModel model) {
        this.model = model;
        state = ModelsAdapter.DataState.INSTALLED;
    }

    public boolean wasDeleted() {
        this.model = null;
        state = ModelsAdapter.DataState.CLOUD;
        return this.modelLink == null;
    }

    public void wasQueued() {
        state = ModelsAdapter.DataState.QUEUED;
    }

    public void downloading() {
        state = ModelsAdapter.DataState.DOWNLOADING;
    }

    public void downloadCanceled() {
        if (state == ModelsAdapter.DataState.DOWNLOADING)
            state = ModelsAdapter.DataState.CLOUD;
    }

    public ModelsAdapter.DataState getState() {
        return state;
    }

    public ModelLink getModelLink() {
        return modelLink;
    }

    public LocalModel getModel() {
        return model;
    }

    @Override
    public String getTitle() {
        return getLocale().getDisplayName();
    }

    @Override
    public String getSubtitle() {
        return getFilename();
    }

    @Override
    public int getImageRes() {
        switch (getState()) {
            case CLOUD:
                return R.drawable.ic_download;
            case INSTALLED:
                return R.drawable.ic_delete;
            case DOWNLOADING:
                return R.drawable.ic_downloading;
            case QUEUED:
                return R.drawable.ic_add_circle_outline;
        }
        return 0;
    }

    @Override
    public void buttonClicked(ModelsAdapter adapter, Context context) {
        switch (getState()) {
            case CLOUD: // Not installed, download
                FileDownloader.downloadModel(getModelLink(), context);
                break;
            case INSTALLED: // Installed, delete
                Tools.deleteModel(getModel(), context);
                boolean removed = wasDeleted();
                if (removed) {
                    adapter.removed(this);
                } else {
                    adapter.changed(this);
                }
                break;
            case DOWNLOADING: // Downloading, cancel download
                // TODO: cancel download
                break;
            case QUEUED: // Queued for download, remove from queue
                // TODO: remove from downloading queue
                break;
        }
    }
}
