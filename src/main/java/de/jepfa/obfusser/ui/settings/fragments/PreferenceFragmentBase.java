package de.jepfa.obfusser.ui.settings.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import de.jepfa.obfusser.ui.settings.SettingsActivity;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class PreferenceFragmentBase extends PreferenceFragment {


    protected abstract int getPrefResourceId();

    protected abstract void initPreferences();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(getPrefResourceId());
        setHasOptionsMenu(true);

        initPreferences();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class)); //TODO
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}