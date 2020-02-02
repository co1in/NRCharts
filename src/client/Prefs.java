package client;

import java.util.prefs.Preferences;

public class Prefs {
    private static Prefs instance;
    public static Prefs getInstance() {
        if(instance == null) {
            instance = new Prefs();
        }
        return instance;
    }

    Preferences prefs = Preferences.userNodeForPackage(Prefs.class);

    public static final String SERVER_PREF = "server";
    public static final String WIDTH_PREF = "width";
    public static final String HEIGHT_PREF = "height";
    public static final String KEYS_PREF = "keys";
    public static final String SELECTED_CHARTS_PREF = "selected_charts";

    public String get(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }

    public String get(String key) {
        return prefs.get(key, "");
    }

    public void put(String key, String value){
        prefs.put(key, value);
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void putInt(String key, int value) {
        prefs.putInt(key, value);
    }
}
