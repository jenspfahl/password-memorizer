package de.jepfa.obfusser.ui;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.ui.settings.SettingsActivity;

public abstract class BaseActivity extends AppCompatActivity {

    public Representation getPatternRepresentation() {
        String value =  PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(SettingsActivity.PREF_PATTERN_STYLE, null);

        if (value != null) {
            try {
                return Representation.valueOf(value);
            } catch (Exception e) {
                //nothing
            }
        }
        return Representation.DEFAULT_BLOCKS;

    }
}
