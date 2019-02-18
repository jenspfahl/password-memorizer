package de.jepfa.obfusser.ui.settings.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.ListPreference;

import java.util.ArrayList;
import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.settings.listener.PatternStylePreferenceListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragmentBase {

    @Override
    protected int getPrefResourceId() {
        return R.xml.pref_general;
    }

    @Override
    protected void initPreferences() {
        ListPreference patternStylePref = (ListPreference) findPreference(SettingsActivity.PREF_PATTERN_STYLE);
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();
        for (Representation representation : Representation.values()) {
            if (representation.isAvailable()) {
                entries.add(representation.getTitle());
                entryValues.add(representation.name());
            }
        }

        String currRepresentationValue = patternStylePref.getPreferenceManager().getDefaultSharedPreferences(this.getActivity()).getString(
                SettingsActivity.PREF_PATTERN_STYLE, Representation.DEFAULT_BLOCKS.name());

        Representation currRepresentation = Representation.valueOfWithDefault(currRepresentationValue);

        patternStylePref.setEntries(entries.toArray(new String[entries.size()]));
        patternStylePref.setEntryValues(entryValues.toArray(new String[entryValues.size()]));
        patternStylePref.setDefaultValue(currRepresentation.name());

        PatternStylePreferenceListener onPreferenceChangeListener = new PatternStylePreferenceListener(getActivity());
        patternStylePref.setOnPreferenceChangeListener(onPreferenceChangeListener);
        onPreferenceChangeListener.onPreferenceChange(patternStylePref, currRepresentation);
    }
}