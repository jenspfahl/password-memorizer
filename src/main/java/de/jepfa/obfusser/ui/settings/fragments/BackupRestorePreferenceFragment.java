package de.jepfa.obfusser.ui.settings.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.Preference;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.settings.listener.BackupPreferenceListener;
import de.jepfa.obfusser.ui.settings.listener.RestorePreferenceListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BackupRestorePreferenceFragment extends PreferenceFragmentBase {

    @Override
    protected int getPrefResourceId() {
        return R.xml.pref_backuprestore;
    }

    @Override
    protected void initPreferences() {
        Preference backupPref = findPreference(SettingsActivity.PREF_BACKUP);
        backupPref.setOnPreferenceClickListener(
                new BackupPreferenceListener(getActivity()));

        Preference restorePref = findPreference(SettingsActivity.PREF_RESTORE);
        restorePref.setOnPreferenceClickListener(
                new RestorePreferenceListener(getActivity()));
    }

}

