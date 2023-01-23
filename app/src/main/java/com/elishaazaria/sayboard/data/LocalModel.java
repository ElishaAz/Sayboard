package com.elishaazaria.sayboard.data;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class LocalModel implements Serializable {
    public final String path;
    public final Locale locale;
    public final String filename;

    public LocalModel(String path, Locale locale, String filename) {
        this.path = path;
        this.locale = locale;
        this.filename = filename;
    }

    public String serialize() {
        return serialize(this);
    }

    public static String serialize(LocalModel model) {
        return "[path:\"" + encode(model.path) +
                "\", locale:\"" + model.locale +
                "\", name:\"" + encode(model.filename) + "\"]";
    }

    public static LocalModel deserialize(String serialized) {
        throw new RuntimeException(); // TODO: implement
    }

    private static String encode(String s) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
                case ',':
                case '"':
                case '\\':
                case ':':
                    sb.append("\\");
                    sb.append(String.format("%02x", (int) c));
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String decode(String s) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '\\') {
                i++;
                sb.append((char) Integer.parseInt(s.substring(i, i + 2)));
                i += 2;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalModel model = (LocalModel) o;

        if (!Objects.equals(path, model.path)) return false;
        if (!Objects.equals(locale, model.locale)) return false;
        return Objects.equals(filename, model.filename);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        return result;
    }
}
