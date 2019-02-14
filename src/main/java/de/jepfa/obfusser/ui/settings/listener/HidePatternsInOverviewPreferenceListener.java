package de.jepfa.obfusser.ui.settings.listener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.Preference;

import de.jepfa.obfusser.ui.settings.SettingsActivity;

public class HidePatternsInOverviewPreferenceListener implements Preference.OnPreferenceChangeListener {

    private final Activity activity;

    public HidePatternsInOverviewPreferenceListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean hidePatterns = Boolean.parseBoolean(value.toString());

        SharedPreferences.Editor editor = preference.getEditor();
        editor.putBoolean(SettingsActivity.PREF_HIDE_PATTERN_IN_OVERVIEW, !hidePatterns);
        editor.commit();

        return true;
    }
}