package de.jepfa.obfusser.ui.settings.listener;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import java.util.Arrays;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.service.SecurityService;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.Noogler;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.encrypt.EncryptUtil;

public class EnablePasswordPreferenceListener implements Preference.OnPreferenceChangeListener {

    private final Activity activity;

    public EnablePasswordPreferenceListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object value) {
        final boolean encrypt = Boolean.parseBoolean(value.toString());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.not_supported)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return false;
        }

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

                            SharedPreferences defaultSharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(activity);

                            if (encrypt) {
                                SharedPreferences.Editor passwdEditor = defaultSharedPreferences.edit();
                                passwdEditor.putBoolean(SecureActivity.SecretChecker.PREF_ENC_WITH_UUID, disturbPatternsSwitch.isChecked());
                                passwdEditor.commit();

                                SecurityService.startEncryptAll(preference.getContext(),
                                        key, SecureActivity.SecretChecker.isEncWithUUIDEnabled(activity));

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

                                    Noogler.resetPrefs(activity);
                                }
                            }
                        } finally {
                            EncryptUtil.clearPwd(pwd);
                            EncryptUtil.clearPwd(pwd2);
                        }
                        // save it
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, encrypt);
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

        return false; // above method will save the preference, therefor false here
    }

    private void storeKeySavely(byte[] key, byte[] salt, Activity activity) {

        byte[] hashedKey = EncryptUtil.fastHash(key, salt);
        Pair<byte[], byte[]> encrypted = EncryptUtil.encryptData(SecureActivity.SecretChecker.KEY_ALIAS_PASSWD, hashedKey);
        if (encrypted == null) {
            Log.e("STORE_KEY", "Cannot store key cause it could not be encrypted");
            return;
        }

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


    private void removeSavelyStoredKey(byte[] key, PreferenceManager preferenceManager, Activity activity) {
        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);

        SharedPreferences.Editor passwdEditor = defaultSharedPreferences.edit();
        passwdEditor.remove(SecureActivity.SecretChecker.PREF_PASSWD);
        passwdEditor.commit();

        SharedPreferences.Editor ivEditor = defaultSharedPreferences.edit();
        ivEditor.remove(SecureActivity.SecretChecker.PREF_PASSWD_IV);
        ivEditor.commit();
    }

}