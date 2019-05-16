package de.jepfa.obfusser.ui.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.ui.settings.fragments.PreferenceFragmentBase;
import de.jepfa.obfusser.ui.settings.listener.BackupPreferenceListener;
import de.jepfa.obfusser.ui.settings.listener.RestorePreferenceListener;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String PREF_ENABLE_PASSWORD = "pref_enable_password";
    public static final String PREF_EXPANDABLE_CREDENTIAL_LIST = "pref_expandable_credential_list";
    public static final String PREF_PATTERN_STYLE = "pref_pattern_style";
    public static final String PREF_HIDE_PATTERN_IN_OVERVIEW = "pref_hide_patterns_in_overview";
    public static final String PREF_EXPANDED_GROUPS = "pref_expanded_groups";
    public static final String PREF_BACKUP = "pref_backup";
    public static final String PREF_RESTORE = "pref_restore";



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case BackupPreferenceListener.REQUEST_CODE_BACKUP_FILE:
                    BackupPreferenceListener.doBackupProcess(this, data);
                    break;
                case RestorePreferenceListener.REQUEST_CODE_RESTORE_FILE:
                    RestorePreferenceListener.doRestoreProcess(this, data);
                    break;
            }
        }
    }



    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.isAssignableFrom(PreferenceFragmentBase.class);
    }

}
