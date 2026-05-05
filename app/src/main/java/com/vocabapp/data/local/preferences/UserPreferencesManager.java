package com.vocabapp.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.vocabapp.domain.enums.PronunciationAccent;
import com.vocabapp.domain.enums.VisibilityMode;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class UserPreferencesManager {

    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_VISIBILITY_MODE = "visibility_mode";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_AUTO_ADVANCE_SECONDS = "auto_advance_seconds";

    private final SharedPreferences prefs;

    public UserPreferencesManager(@ApplicationContext Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public VisibilityMode getVisibilityMode() {
        String value = prefs.getString(KEY_VISIBILITY_MODE, VisibilityMode.HIDE_ENGLISH.name());
        return VisibilityMode.valueOf(value);
    }

    public void setVisibilityMode(VisibilityMode mode) {
        prefs.edit().putString(KEY_VISIBILITY_MODE, mode.name()).apply();
    }

    public PronunciationAccent getAccent() {
        String value = prefs.getString(KEY_ACCENT, PronunciationAccent.AMERICAN.name());
        return PronunciationAccent.valueOf(value);
    }

    public void setAccent(PronunciationAccent accent) {
        prefs.edit().putString(KEY_ACCENT, accent.name()).apply();
    }

    public int getAutoAdvanceSeconds() {
        return prefs.getInt(KEY_AUTO_ADVANCE_SECONDS, 5);
    }

    public void setAutoAdvanceSeconds(int seconds) {
        prefs.edit().putInt(KEY_AUTO_ADVANCE_SECONDS, seconds).apply();
    }
}
