package com.elishaazaria.sayboard.settingsfragments.modelsfragment;

import android.content.Context;

public interface ModelsAdapterData {
    String getTitle();

    String getSubtitle();

    int getImageRes();

    void buttonClicked(ModelsAdapter adapter, Context context);
}
