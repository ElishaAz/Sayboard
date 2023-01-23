package com.elishaazaria.sayboard.data;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

public class VoskServerData implements Serializable, Comparable<VoskServerData> {
    public final URI uri;
    public final Locale locale; // placeholder for now

    public VoskServerData(URI uri, Locale locale) {
        this.uri = uri;
        this.locale = locale;
    }

    @Override
    public int compareTo(VoskServerData o) {
        return uri.compareTo(o.uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoskServerData that = (VoskServerData) o;
        return uri.equals(that.uri) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, locale);
    }

    public static String serialize(VoskServerData data) {
        return data.uri.toString();
    }

    public static VoskServerData deserialize(String data) {
        try {
            return new VoskServerData(new URI(data), null);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
