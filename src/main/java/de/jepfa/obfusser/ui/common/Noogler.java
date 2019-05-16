package de.jepfa.obfusser.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.ui.settings.fragments.SecurityPreferenceFragment;

public class Noogler {

    public static final String PREF_DONT_NOOGLE_ENC_DATA = "de.jepfa.obfusser.dont_noogle_enc_data";
    public static final String PREF_NOOGLE_ENC_DATA_COUNTER = "de.jepfa.obfusser.noogle_enc_data_counter";
    private static final int NOOGLE_DURATION = 10_000;

    public static void noogleEncryptData(final Activity activity, View view) {
        final SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);
        boolean dontNoogle = defaultSharedPreferences
                .getBoolean(PREF_DONT_NOOGLE_ENC_DATA, false);
        int noogleCounter = defaultSharedPreferences
                .getInt(PREF_NOOGLE_ENC_DATA_COUNTER, 0);
        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        edit.putInt(PREF_NOOGLE_ENC_DATA_COUNTER, ++noogleCounter);
        edit.commit();

        if (!dontNoogle && ! SecureActivity.SecretChecker.isPasswordCheckEnabled(activity)
            && itIsTime(noogleCounter)) {

            final boolean isOpenSettings = noogleCounter % 2 == 1;
            String text = isOpenSettings ? activity.getString(R.string.noogler_open_setting) : activity.getString(R.string.noogler_dismiss_forever);
            Snackbar.make(view, R.string.noogler_noogle_text, NOOGLE_DURATION)
                    .setAction(text, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isOpenSettings) {
                                Intent intent = new Intent(activity, SettingsActivity.class);
                                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SecurityPreferenceFragment.class.getName());
                                activity.startActivity(intent);
                            }
                            else {
                                SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                                edit.putBoolean(PREF_DONT_NOOGLE_ENC_DATA, true);
                                edit.commit();
                            }
                        }
                    })
                    .show();
        }
    }

    private static boolean itIsTime(int noogleCounter) {
        return noogleCounter % 19 == 3;
    }


    public static void resetPrefs(Activity activity) {
        final SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        edit.remove(PREF_DONT_NOOGLE_ENC_DATA);
        edit.remove(PREF_NOOGLE_ENC_DATA_COUNTER);
        edit.commit();
    }
}
