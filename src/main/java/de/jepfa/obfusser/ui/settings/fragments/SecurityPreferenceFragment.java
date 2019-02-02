package de.jepfa.obfusser.ui.settings.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.Preference;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.settings.listener.EnablePasswordPreferenceListener;
import de.jepfa.obfusser.ui.settings.listener.ShowPatternsInOverviewPreferenceListener;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SecurityPreferenceFragment extends PreferenceFragmentBase {

    @Override
    protected int getPrefResourceId() {
        return R.xml.pref_security;
    }

    @Override
    protected void initPreferences() {

        Preference passwordEnablePref = findPreference(SettingsActivity.PREF_ENABLE_PASSWORD);
        passwordEnablePref.setOnPreferenceChangeListener(
                new EnablePasswordPreferenceListener(getActivity()));

        Preference passwordShowPattern = findPreference(SettingsActivity.PREF_SHOW_PATTERN_IN_OVERVIEW);
        passwordShowPattern.setOnPreferenceChangeListener(
                new ShowPatternsInOverviewPreferenceListener(getActivity()));

    }

}