package de.jepfa.obfusser.ui.settings.listener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.Preference;

import de.jepfa.obfusser.ui.settings.SettingsActivity;

public class ShowPatternsInOverviewPreferenceListener implements Preference.OnPreferenceChangeListener {

    private final Activity activity;

    public ShowPatternsInOverviewPreferenceListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean show = Boolean.parseBoolean(value.toString());

        SharedPreferences.Editor editor = preference.getEditor();
        editor.putBoolean(SettingsActivity.PREF_SHOW_PATTERN_IN_OVERVIEW, !show);
        editor.commit();

        return true;
    }
}