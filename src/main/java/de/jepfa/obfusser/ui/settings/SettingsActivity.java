package de.jepfa.obfusser.ui.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.service.SecurityService;
import de.jepfa.obfusser.util.EncryptUtil;

import java.util.List;

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
    public static final String PREF_SECURITY_PASSWORD = "pref_enable_password";
    public static final String PREF_REFERENCE_PASSWORD = "pref_reference_password";

    private static class EnablePasswordPreferenceListener implements Preference.OnPreferenceChangeListener {


        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            boolean enabled = Boolean.parseBoolean(value.toString());

            inputPasswordAndCrypt(preference, enabled);

            return true;
        }
    }

    private static void inputPasswordAndCrypt(final Preference preference, final boolean encrypt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());

        final EditText input = new EditText(preference.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        String message = encrypt ? "For encryption" : "For decryption";
        builder.setTitle("Enter a password")
                .setMessage(message)
                .setView(input)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean(PREF_ENABLE_PASSWORD, !encrypt);
                        editor.commit();
                        SwitchPreference switchPreference = (SwitchPreference) preference;
                        switchPreference.setChecked(!encrypt);

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String pwd = input.getText().toString();
                        if (TextUtils.isEmpty(pwd)) {
                            input.setError("Password required");
                        }
                        else {

                            byte[] key = EncryptUtil.generateKey(pwd);

                            if (encrypt) {
                                SecurityService.startEncryptAll(preference.getContext(), key);

                                Secret secret = Secret.getOrCreate();
                                secret.setDigest(key);
                            }
                            else {
                                SecurityService.startDecryptAll(preference.getContext(), key);
                                Secret secret = Secret.getOrCreate();
                                secret.setDigest(null);
                            }
                        }


                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private static class ReferencePasswordPreferenceListener implements Preference.OnPreferenceChangeListener {


        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue); //TODO really needed to show this in settings overview?

            return true;
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
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
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
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SecurityPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SecurityPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_security);
            setHasOptionsMenu(true);

            Preference passwordEnablePref = findPreference(PREF_ENABLE_PASSWORD);
            passwordEnablePref.setOnPreferenceChangeListener(new EnablePasswordPreferenceListener());

            Preference referencePasswordPref = findPreference(PREF_REFERENCE_PASSWORD);
            referencePasswordPref.setOnPreferenceChangeListener(new ReferencePasswordPreferenceListener());
            Object referencePasswordValue = PreferenceManager
                    .getDefaultSharedPreferences(referencePasswordPref.getContext())
                    .getString(referencePasswordPref.getKey(), "");
            referencePasswordPref.setSummary(referencePasswordValue.toString());


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }



}
