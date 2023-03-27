package com.elishaazaria.sayboard.preferences;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.data.VoskServerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelPreferences {
    public static final boolean VOSK_SERVER_ENABLED = false;

    public static List<VoskServerData> getVoskServers() {
        Set<String> set = MyPreferences.getSharedPref().getStringSet(AppCtx.getStringRes(R.string.pref_models_vosk_servers_set),
                null);

        if (set == null) return new ArrayList<>();

        ArrayList<VoskServerData> list = new ArrayList<>();

        for (String stringData : set) {
            VoskServerData data = VoskServerData.deserialize(stringData);
            if (data != null) {
                list.add(data);
            }
        }

        Collections.sort(list);
        return list;
    }

    public static void setVoskServers(List<VoskServerData> servers) {
        Set<String> set = new HashSet<>();
        for (VoskServerData data : servers) {
            set.add(VoskServerData.serialize(data));
        }

        MyPreferences.getSharedPref().edit().putStringSet(AppCtx.getStringRes(R.string.pref_models_vosk_servers_set), set).apply();
    }

    public static void addToVoskServers(VoskServerData data) {
        Set<String> set = new HashSet<>(MyPreferences.getSharedPref().getStringSet(AppCtx.getStringRes(R.string.pref_models_vosk_servers_set),
                new HashSet<>()));
        set.add(VoskServerData.serialize(data));
        MyPreferences.getSharedPref().edit().putStringSet(AppCtx.getStringRes(R.string.pref_models_vosk_servers_set), set).apply();
    }

    public static void removeFromVoskServers(VoskServerData data) {
        Set<String> set = new HashSet<>(MyPreferences.getSharedPref().getStringSet(AppCtx.getStringRes(R.string.pref_models_vosk_servers_set),
                new HashSet<>()));
        set.remove(VoskServerData.serialize(data));
        MyPreferences.getSharedPref().edit().putStringSet(AppCtx.getStringRes(R.string.pref_models_vosk_servers_set), set).apply();
    }
}
