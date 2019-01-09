package de.jepfa.obfusser.ui.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.service.SecurityService;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.util.EncryptUtil;

import java.util.ArrayList;
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
    public static final String PREF_EXPANDABLE_CREDENTIAL_LIST = "pref_expandable_credential_list";
    public static final String PREF_PATTERN_STYLE = "pref_pattern_style";
    public static final String PREF_SHOW_PATTERN_IN_OVERVIEW = "pref_show_pattern_in_overview";
    public static final String PREF_EXPANDED_GROUPS = "pref_expanded_groups";


    private static class PatternStylePreferenceListener implements Preference.OnPreferenceChangeListener {

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
                    PREF_PATTERN_STYLE, representationValue);
            editor.commit();

            return true;
        }
    }

    private static class EnablePasswordPreferenceListener implements Preference.OnPreferenceChangeListener {

        private final Activity activity;

        public EnablePasswordPreferenceListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean enabled = Boolean.parseBoolean(value.toString());

            inputPasswordAndCrypt(activity, preference, enabled);

            return true;
        }
    }

    private static class ShowPatternsInOverviewPreferenceListener implements Preference.OnPreferenceChangeListener {

        private final Activity activity;

        public ShowPatternsInOverviewPreferenceListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean show = Boolean.parseBoolean(value.toString());

            SharedPreferences.Editor editor = preference.getEditor();
            editor.putBoolean(PREF_SHOW_PATTERN_IN_OVERVIEW, !show);
            editor.commit();

            return true;
        }
    }

    private static void inputPasswordAndCrypt(Activity activity, final Preference preference, final boolean encrypt) {
        LayoutInflater inflater = activity.getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
        String message = encrypt
                ? "Your password is used to encrypt all data. It is important to remember this password, there is no recovery. It can be an easy one, since wrong entered passwords will decrypt the patterns in a different way and only you should know how your patterns look like."
                : "Enter your password to disable pattern encryption. It is important to type it correctly, otherwise the patterns will be decrypted in a different way. This is not undo-able.";
        final View passwordView = inflater.inflate(R.layout.dialog_setup_password, null);
        final AlertDialog dialog = builder.setTitle("Enter and confirm a password")
                .setMessage(message)
                .setView(passwordView)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        TextView firstPassword = passwordView.findViewById(R.id.first_password);
                        String pwd = firstPassword.getText().toString();
                        if (TextUtils.isEmpty(pwd)) {
                            firstPassword.setError("Password required");
                            return;
                        }

                        TextView secondPassword = passwordView.findViewById(R.id.second_password);
                        String pwd2 = secondPassword.getText().toString();
                        if (TextUtils.isEmpty(pwd2)) {
                            secondPassword.setError("Password confirmation required");
                            return;
                        }

                        if (!TextUtils.equals(pwd, pwd2)) {
                            secondPassword.setError("Password not equal");
                            return;
                        }


                        byte[] key = EncryptUtil.generateKey(pwd, SecureActivity.SecretChecker.getApplicationSalt(preference.getContext()));

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

                        dialog.dismiss();
                    }
                });

                Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean(PREF_ENABLE_PASSWORD, !encrypt);
                        editor.commit();
                        SwitchPreference switchPreference = (SwitchPreference) preference;
                        switchPreference.setChecked(!encrypt);

                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();


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
                || SecurityPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            ListPreference patternStylePref = (ListPreference) findPreference(PREF_PATTERN_STYLE);
            List<String> entries = new ArrayList<>();
            List<String> entryValues = new ArrayList<>();
            for (Representation representation : Representation.values()) {
                if (representation.isAvailable()) {
                    entries.add(representation.getTitle());
                    entryValues.add(representation.name());
                }
            }

            String currRepresentation = patternStylePref.getPreferenceManager().getDefaultSharedPreferences(this.getActivity()).getString(
                    PREF_PATTERN_STYLE, Representation.DEFAULT_BLOCKS.name());

            patternStylePref.setEntries(entries.toArray(new String[entries.size()]));
            patternStylePref.setEntryValues(entryValues.toArray(new String[entryValues.size()]));
            patternStylePref.setDefaultValue(currRepresentation);

            PatternStylePreferenceListener onPreferenceChangeListener = new PatternStylePreferenceListener(getActivity());
            patternStylePref.setOnPreferenceChangeListener(onPreferenceChangeListener);
            onPreferenceChangeListener.onPreferenceChange(patternStylePref, currRepresentation);
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
            passwordEnablePref.setOnPreferenceChangeListener(
                    new EnablePasswordPreferenceListener(getActivity()));

            Preference passwordShowPattern = findPreference(PREF_SHOW_PATTERN_IN_OVERVIEW);
            passwordShowPattern.setOnPreferenceChangeListener(
                    new ShowPatternsInOverviewPreferenceListener(getActivity()));

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



}
