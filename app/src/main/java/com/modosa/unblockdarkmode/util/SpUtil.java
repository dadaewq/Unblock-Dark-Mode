package com.modosa.unblockdarkmode.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * @author dadaewq
 */
public class SpUtil {
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SpUtil(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getWorkMode() {
        return sharedPreferences.getInt("method", -1);
    }

    public void putWorkMode(int checked) {
        editor = sharedPreferences.edit();
        editor.putInt("method", checked);
        editor.apply();
    }

    public boolean isStableMode() {
        String stablemode = "stablemode";
        return sharedPreferences.getBoolean(stablemode, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public boolean getFalseBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public String getString(String key, String defaultVale) {
        return sharedPreferences.getString(key, defaultVale);
    }

    public void putString(String key, String value) {
        editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
