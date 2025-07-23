package com.safetyapp.mainapp;

import android.content.Context;

public class PINManager {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String PIN_KEY = "saved_pin";
    private static final String ATTEMPTS_KEY = "pin_attempts";

    public static String getSavedPin(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(PIN_KEY, null);
    }

    public static void savePin(Context context, String pin) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(PIN_KEY, pin).apply();
    }

    public static int getAttempts(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(ATTEMPTS_KEY, 0);
    }

    public static void incrementAttempts(Context context) {
        int current = getAttempts(context);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(ATTEMPTS_KEY, current + 1).apply();
    }

    public static void resetAttempts(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(ATTEMPTS_KEY, 0).apply();
    }
}
