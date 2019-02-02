package de.jepfa.obfusser.ui.settings.listener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;

import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.ui.settings.SettingsActivity;

public class PatternStylePreferenceListener implements Preference.OnPreferenceChangeListener {

    private final Activity activity;

    public PatternStylePreferenceListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String representationValue = value == null ? null : value.toString();
        try {
            Representation.valueOf(representationValue);
        } catch (Exception e) {
            representationValue = Representation.DEFAULT_BLOCKS.name();
        }

        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(representationValue);

        // Set the summary to reflect the new value.
        preference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

        SharedPreferences.Editor editor = preference.getPreferenceManager().getDefaultSharedPreferences(preference.getContext()).edit();
        editor.putString(
                SettingsActivity.PREF_PATTERN_STYLE, representationValue);
        editor.commit();

        return true;
    }
}
