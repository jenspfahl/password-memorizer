package de.jepfa.obfusser.ui.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.service.BackupRestoreService;
import de.jepfa.obfusser.service.SecurityService;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.PermissionChecker;
import de.jepfa.obfusser.util.encrypt.EncryptUtil;
import de.jepfa.obfusser.util.encrypt.FileUtil;

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
    public static final String PREF_BACKUP = "pref_backup";
    public static final String PREF_RESTORE = "pref_restore";

    public static final int REQUEST_CODE_RESTORE_FILE = 1001;


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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(android.R.string.dialog_alert_title)
                        .setMessage(R.string.not_supported)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }

            inputPasswordAndCrypt(activity, preference, enabled);

            return false; // above method will save the preference, therefor false here
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

    private static class BackupPreferenceListener implements Preference.OnPreferenceClickListener {

        private final Activity activity;

        public BackupPreferenceListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {

            PermissionChecker.verifyRWStoragePermissions(activity);

            LayoutInflater inflater = activity.getLayoutInflater();

            final View passwordView = inflater.inflate(R.layout.dialog_setup_password, null);
            final EditText firstPassword = passwordView.findViewById(R.id.first_password);
            final EditText secondPassword = passwordView.findViewById(R.id.second_password);
            final Switch storePasswdSwitch = passwordView.findViewById(R.id.switch_store_password);
            storePasswdSwitch.setVisibility(View.GONE);
            final Switch disturbPatternsSwitch = passwordView.findViewById(R.id.disturb_equal_patterns);
            disturbPatternsSwitch.setVisibility(View.GONE);

            final boolean passwordCheckEnabled = PreferenceManager
                    .getDefaultSharedPreferences(activity)
                    .getBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, false);

            String message;
            if (SecureActivity.SecretChecker.isPasswordStored(activity)) {
                message = activity.getString(R.string.message_backup_dialog_encrypted_single);
                secondPassword.setVisibility(View.GONE);
            }
            else if (passwordCheckEnabled) {
                message = activity.getString(R.string.message_backup_dialog_encrypted);
            }
            else {
                message = activity.getString(R.string.message_backup_dialog_noenc);
                firstPassword.setVisibility(View.GONE);
                secondPassword.setVisibility(View.GONE);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());

            final AlertDialog dialog = builder.setTitle(activity.getString(R.string.title_backup_dialog))
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
                            byte[] key = null;
                            byte[] transferSalt = EncryptUtil.generateSalt();
                            byte[] transferKey = null;

                            if (passwordCheckEnabled) {
                                char[] pwd = EncryptUtil.getCharArray(firstPassword.getText());
                                char[] pwd2 = null;
                                try {
                                    if (pwd == null || pwd.length == 0) {
                                        firstPassword.setError(activity.getString(R.string.password_required));
                                        firstPassword.requestFocus();
                                        return;
                                    }

                                    if (!SecureActivity.SecretChecker.isPasswordStored(activity)) {
                                        pwd2 = EncryptUtil.getCharArray(secondPassword.getText());
                                        if (pwd2 == null || pwd2.length == 0) {
                                            secondPassword.setError(activity.getString(R.string.password_confirmation_required));
                                            secondPassword.requestFocus();
                                            return;
                                        }

                                        if (!Arrays.equals(pwd, pwd2)) {
                                            secondPassword.setError(activity.getString(R.string.password_not_equal));
                                            secondPassword.requestFocus();
                                            return;
                                        }
                                    }


                                    byte[] applicationSalt = SecureActivity.SecretChecker.getSalt(preference.getContext());
                                    key = EncryptUtil.generateKey(pwd, applicationSalt);

                                    if (!SecureActivity.SecretChecker.isPasswordValid(pwd, activity, applicationSalt)) {
                                        firstPassword.setError(activity.getString(R.string.wrong_password));
                                        firstPassword.requestFocus();
                                        secondPassword.setText(null);
                                        return;
                                    }

                                    transferKey = EncryptUtil.generateKey(pwd, transferSalt);

                                } finally {
                                    EncryptUtil.clearPwd(pwd);
                                    EncryptUtil.clearPwd(pwd2);
                                }
                            }
                            // export it
                            BackupRestoreService.startBackupAll(preference.getContext(), key,
                                    transferKey, transferSalt,
                                    SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity));


                            dialog.dismiss();
                        }
                    });

                    Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    buttonNegative.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            dialog.dismiss();
                        }
                    });
                }
            });
            dialog.show();

            return false; // it is a pseudo preference
        }
    }

    private static class RestorePreferenceListener implements Preference.OnPreferenceClickListener {

        private final Activity activity;

        public RestorePreferenceListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {

            PermissionChecker.verifyReadStoragePermissions(activity);

            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.chooser_select_restore_file)),
                    REQUEST_CODE_RESTORE_FILE);

            return false; // it is a pseudo preference
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_RESTORE_FILE && resultCode==RESULT_OK) {
            Uri selectedFile = data.getData();

            String content = null;
            JsonObject jsonContent = null;

            if (FileUtil.isExternalStorageReadable()) {
                try {
                    content = FileUtil.readFile(this, selectedFile);
                    if (content != null) {
                        JsonParser parser = new JsonParser();
                        jsonContent = parser.parse(content).getAsJsonObject();
                    }
                } catch (Exception e) {
                    Log.e("RESTORE", "cannot import file " + selectedFile, e);
                }
            }
            final String fcontent = content;
            final JsonObject fjsonContent = jsonContent;


            if (jsonContent == null) {
                Toast.makeText(this, getString(R.string.toast_restore_failure), Toast.LENGTH_LONG).show();
            }
            else {
                LayoutInflater inflater = getLayoutInflater();

                final View passwordView = inflater.inflate(R.layout.dialog_setup_password, null);
                final EditText firstPassword = passwordView.findViewById(R.id.first_password);
                final EditText secondPassword = passwordView.findViewById(R.id.second_password);
                final Switch storePasswdSwitch = passwordView.findViewById(R.id.switch_store_password);
                storePasswdSwitch.setVisibility(View.GONE);
                final Switch disturbPatternsSwitch = passwordView.findViewById(R.id.disturb_equal_patterns);
                disturbPatternsSwitch.setVisibility(View.GONE);

                final boolean passwordCheckEnabled = jsonContent.get(BackupRestoreService.JSON_ENC).getAsBoolean();
                final int credentialsCount = jsonContent.get(BackupRestoreService.JSON_CREDENTIALS_COUNT).getAsInt();
                final int templatesCount = jsonContent.get(BackupRestoreService.JSON_TEMPLATES_COUNT).getAsInt();
                final int groupsCount = jsonContent.get(BackupRestoreService.JSON_GROUPS_COUNT).getAsInt();
                final String fromDate = jsonContent.get(BackupRestoreService.JSON_DATE).getAsString();

                if (credentialsCount == 0 && templatesCount == 0 && groupsCount == 0) {
                    Toast.makeText(this, R.string.toast_restore_nodata, Toast.LENGTH_LONG).show();
                    return;
                }

                String message;
                if (passwordCheckEnabled) {
                    message = getString(R.string.message_restore_dialog_encrypted, credentialsCount, templatesCount, groupsCount);
                }
                else {
                    message = getString(R.string.message_restore_dialog_noenc, credentialsCount, templatesCount, groupsCount);
                    firstPassword.setVisibility(View.GONE);
                    secondPassword.setVisibility(View.GONE);
                }

                final byte[] key = SecureActivity.SecretChecker.getOrAskForSecret(this);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final AlertDialog dialog = builder.setTitle(getString(R.string.title_restore_dialog, fromDate))
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
                                byte[] transferKey = null;

                                if (passwordCheckEnabled) {
                                    char[] pwd = EncryptUtil.getCharArray(firstPassword.getText());
                                    char[] pwd2 = null;
                                    try {
                                        if (pwd == null || pwd.length == 0) {
                                            firstPassword.setError(getString(R.string.password_required));
                                            firstPassword.requestFocus();
                                            return;
                                        }

                                        pwd2 = EncryptUtil.getCharArray(secondPassword.getText());
                                        if (pwd2 == null || pwd2.length == 0) {
                                            secondPassword.setError(getString(R.string.password_confirmation_required));
                                            secondPassword.requestFocus();
                                            return;
                                        }

                                        if (!Arrays.equals(pwd, pwd2)) {
                                            secondPassword.setError(getString(R.string.password_not_equal));
                                            secondPassword.requestFocus();
                                            return;
                                        }


                                        String saltBase64 = fjsonContent.get(BackupRestoreService.JSON_SALT).getAsString();
                                        if (saltBase64 != null) {
                                            byte[] transferSalt = Base64.decode(saltBase64, Base64.NO_WRAP);
                                            transferKey = EncryptUtil.generateKey(pwd, transferSalt);
                                        }

                                    } finally {
                                        EncryptUtil.clearPwd(pwd);
                                        EncryptUtil.clearPwd(pwd2);
                                    }
                                }
                                // import it
                                BackupRestoreService.startRestoreAll(
                                        SettingsActivity.this,
                                        fcontent,
                                        transferKey,
                                        key,
                                        SecureActivity.SecretChecker.isEncWithUUIDEnabled(SettingsActivity.this));


                                dialog.dismiss();
                            }
                        });

                        Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        buttonNegative.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();
            }
        }
    }



    private static void inputPasswordAndCrypt(final Activity activity, final Preference preference, final boolean encrypt) {
        LayoutInflater inflater = activity.getLayoutInflater();

        String title = encrypt
                ? activity.getString(R.string.title_enter_password_for_encryption)
                : activity.getString(R.string.title_enter_password_for_decryption);
        String message = encrypt
                ? activity.getString(R.string.message_password_to_encrypt)
                : activity.getString(R.string.message_password_to_decrypt);

        final View passwordView = inflater.inflate(R.layout.dialog_setup_password, null);
        final EditText firstPassword = passwordView.findViewById(R.id.first_password);
        final EditText secondPassword = passwordView.findViewById(R.id.second_password);
        final Switch storePasswdSwitch = passwordView.findViewById(R.id.switch_store_password);
        final Switch disturbPatternsSwitch = passwordView.findViewById(R.id.disturb_equal_patterns);

        final boolean isSingleDecryptionMode = !encrypt && SecureActivity.SecretChecker.isPasswordStored(activity);

        if (isSingleDecryptionMode) {
            secondPassword.setVisibility(View.GONE);
            message = activity.getString(R.string.message_password_to_decrypt_single);
        }
        if (EncryptUtil.isPasswdEncryptionSupported() && encrypt) {
            message = activity.getString(R.string.message_password_to_encrypt_single);
            storePasswdSwitch.setChecked(true);
        }
        else {
            storePasswdSwitch.setVisibility(View.GONE);
        }

        if (!encrypt) {
            disturbPatternsSwitch.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());

        final AlertDialog dialog = builder.setTitle(title)
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
                        char[] pwd = EncryptUtil.getCharArray(firstPassword.getText());
                        char[] pwd2 = null;
                        try {
                            if (pwd == null || pwd.length == 0) {
                                firstPassword.setError(activity.getString(R.string.password_required));
                                firstPassword.requestFocus();
                                return;
                            }

                            if (!isSingleDecryptionMode) {
                                pwd2 = EncryptUtil.getCharArray(secondPassword.getText());
                                if (pwd2 == null || pwd2.length == 0) {
                                    secondPassword.setError(activity.getString(R.string.password_confirmation_required));
                                    secondPassword.requestFocus();
                                    return;
                                }

                                if (!Arrays.equals(pwd, pwd2)) {
                                    secondPassword.setError(activity.getString(R.string.password_not_equal));
                                    secondPassword.requestFocus();
                                    return;
                                }
                            }


                            byte[] applicationSalt = SecureActivity.SecretChecker.getSalt(preference.getContext());
                            byte[] key = EncryptUtil.generateKey(pwd, applicationSalt);


                            if (encrypt) {
                                SharedPreferences defaultSharedPreferences = PreferenceManager
                                        .getDefaultSharedPreferences(activity);
                                SharedPreferences.Editor passwdEditor = defaultSharedPreferences.edit();
                                passwdEditor.putBoolean(SecureActivity.SecretChecker.PREF_ENC_WITH_UUID, disturbPatternsSwitch.isChecked());
                                passwdEditor.commit();

                                SecurityService.startEncryptAll(preference.getContext(), key, SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity));

                                Secret secret = Secret.getOrCreate();
                                secret.setDigest(key);

                                if (EncryptUtil.isPasswdEncryptionSupported() && storePasswdSwitch.isChecked()) {
                                    storeKeySavely(key, applicationSalt, activity);
                                }

                            }
                            else {
                                if (!SecureActivity.SecretChecker.isPasswordValid(pwd, activity, applicationSalt)) {
                                    firstPassword.setError(activity.getString(R.string.wrong_password));
                                    firstPassword.requestFocus();
                                    secondPassword.setText(null);
                                    return;
                                }
                                else {
                                    SecurityService.startDecryptAll(preference.getContext(), key, SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity));
                                    Secret secret = Secret.getOrCreate();
                                    secret.setDigest(null);
                                    removeSavelyStoredKey(key, preference.getPreferenceManager(), activity);
                                }
                            }
                        } finally {
                            EncryptUtil.clearPwd(pwd);
                            EncryptUtil.clearPwd(pwd2);
                        }
                        // save it
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean(PREF_ENABLE_PASSWORD, encrypt);
                        editor.commit();
                        SwitchPreference switchPreference = (SwitchPreference) preference;
                        switchPreference.setChecked(encrypt);

                        dialog.dismiss();
                    }
                });

                Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private static void storeKeySavely(byte[] key, byte[] salt, Activity activity) {

        byte[] hashedKey = EncryptUtil.fastHash(key, salt);
        Pair<byte[], byte[]> encrypted = EncryptUtil.encryptData(SecureActivity.SecretChecker.KEY_ALIAS_PASSWD, hashedKey);
        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);

        String passwdAsBase64 = Base64.encodeToString(encrypted.second, 0);
        SharedPreferences.Editor passwdEditor = defaultSharedPreferences.edit();
        passwdEditor.putString(SecureActivity.SecretChecker.PREF_PASSWD, passwdAsBase64);
        passwdEditor.commit();

        String ivAsBase64 = Base64.encodeToString(encrypted.first, 0);
        SharedPreferences.Editor ivEditor = defaultSharedPreferences.edit();
        ivEditor.putString(SecureActivity.SecretChecker.PREF_PASSWD_IV, ivAsBase64);
        ivEditor.commit();

    }


    private static void removeSavelyStoredKey(byte[] key, PreferenceManager preferenceManager, Activity activity) {
        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);

        SharedPreferences.Editor passwdEditor = defaultSharedPreferences.edit();
        passwdEditor.remove(SecureActivity.SecretChecker.PREF_PASSWD);
        passwdEditor.commit();

        SharedPreferences.Editor ivEditor = defaultSharedPreferences.edit();
        ivEditor.remove(SecureActivity.SecretChecker.PREF_PASSWD_IV);
        ivEditor.commit();
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
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || BackupRestorePreferenceFragment.class.getName().equals(fragmentName);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BackupRestorePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_backuprestore);
            setHasOptionsMenu(true);

            Preference backupPref = findPreference(PREF_BACKUP);
            backupPref.setOnPreferenceClickListener(
                    new BackupPreferenceListener(getActivity()));

            Preference restorePref = findPreference(PREF_RESTORE);
            restorePref.setOnPreferenceClickListener(
                    new RestorePreferenceListener(getActivity()));
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
