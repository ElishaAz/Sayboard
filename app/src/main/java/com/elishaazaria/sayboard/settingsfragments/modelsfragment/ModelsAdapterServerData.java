package com.elishaazaria.sayboard.settingsfragments.modelsfragment;

import android.content.Context;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.data.VoskServerData;
import com.elishaazaria.sayboard.preferences.ModelPreferences;

public class ModelsAdapterServerData implements ModelsAdapterData {
    private final VoskServerData data;

    public ModelsAdapterServerData(VoskServerData data) {

        this.data = data;
    }

    @Override
    public String getTitle() {
        return data.uri.toString();
    }

    @Override
    public String getSubtitle() {
        return "vosk server";
    }

    @Override
    public int getImageRes() {
        return R.drawable.ic_delete;
    }

    @Override
    public void buttonClicked(ModelsAdapter adapter, Context context) {
        ModelPreferences.removeFromVoskServers(data);
        adapter.removed(this);
    }
}
