package com.elishaazaria.sayboard.downloader.messages;

import java.util.Locale;

public class ModelInfo {
    public final String url;
    public final String filename;
    public final Locale locale;

    public ModelInfo(String url, String filename, Locale locale) {
        this.url = url;
        this.filename = filename;
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "ModelInfo{" +
                "url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                ", locale=" + locale +
                '}';
    }
}
