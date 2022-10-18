package org.vosk.ime;

import java.io.Serializable;
import java.util.Locale;

public class Model implements Serializable {
    public final String path;
    public final Locale locale;
    public final String filename;

    public Model(String path, Locale locale, String filename) {
        this.path = path;
        this.locale = locale;
        this.filename = filename;
    }

    public String serialize() {
        return serialize(this);
    }

    public static String serialize(Model model) {
        return "[path:\"" + encode(model.path) +
                "\", locale:\"" + model.locale +
                "\", name:\"" + encode(model.filename) + "\"]";
    }

    public static Model deserialize(String serialized) {
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
}
