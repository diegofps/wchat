package br.com.wespa.wchat.persistency.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by dsouza on 26/08/17.
 */

public class Settings {

    private static final String PREF_DB_NAME = "preferences.xml";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_UUID = "uuid";

    private final Context mContext;
    private final SharedPreferences mPref;

    public Settings(Context context) {
        mPref = context.getSharedPreferences(PREF_DB_NAME, Context.MODE_PRIVATE);
        mContext = context;
    }

    public String getUsername() {
        return mPref.getString(PREF_USERNAME, null);
    }

    public String getPassword() {
        return mPref.getString(PREF_PASSWORD, null);
    }

    public void saveCredentials(String username, String password) {
        mPref.edit()
                .putString(PREF_USERNAME, username)
                .putString(PREF_PASSWORD, password)
                .apply();
    }

    public String getUUID() {
        String result = mPref.getString(PREF_UUID, null);
        
        if (result == null) {
            result = UUID.randomUUID().toString();
            mPref.edit().putString(PREF_UUID, result).commit();
        }
        
        return result;
    }

}
