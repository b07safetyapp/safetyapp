package com.safetyapp.mainapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
public class PINManager {
    private static final String PIN_KEY = "user_pin";

    private SharedPreferences getPrefs (Context context) throws GeneralSecurityException, IOException{
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
    public void savePin(Context context, String pin) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putString(PIN_KEY, pin).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSavedPin(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(PIN_KEY, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearPin(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().remove(PIN_KEY).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
