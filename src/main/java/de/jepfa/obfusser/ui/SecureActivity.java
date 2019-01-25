package de.jepfa.obfusser.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.EncryptUtil;

public abstract class SecureActivity extends BaseActivity {

    private volatile boolean secretDialogOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        securityCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        securityCheck();
    }

    protected abstract void refresh(boolean before);


    protected synchronized void securityCheck() {
        SecretChecker.getOrAskForSecret(this);
    }

    /**
     * Helper class to check the user secret.
     */
    public static class SecretChecker {

        public static final String PREF_PASSWD = "passwd";
        public static final String PREF_PASSWD_IV = "passwd_iv";
        public static final String KEY_ALIAS_PASSWD = "key_passwd";
        public static final String KEY_ALIAS_SALT = "key_salt";

        private static final String PREF_SALT = "application.salt";
        private static final String PREF_SALT_IV = "application.salt_iv";
        private static final long DELTA_DIALOG_OPENED = TimeUnit.SECONDS.toMillis(5);

        private static volatile long secretDialogOpened;

        public static byte[] getOrAskForSecret(SecureActivity activity) {
            boolean passwordCheckEnabled = PreferenceManager
                    .getDefaultSharedPreferences(activity)
                    .getBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, false);

            if (passwordCheckEnabled) {
                Secret secret = Secret.getOrCreate();

                if (secret.isOutdated() || !secret.hasDigest()) {
                    // make all not readable by setting key as invalid
                    secret.invalidate();
                    // open user secret dialoh
                    openDialog(secret, activity);
                } else {
                    secret.renew();
                }

                return secret.getDigest();
            }

            return null;
        }


        public static synchronized byte[] getSalt(Context context) {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String saltBase64 = defaultSharedPreferences
                    .getString(PREF_SALT, null);
            byte[] salt;
            if (saltBase64 == null) {
                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                salt = EncryptUtil.generateSalt();
                if (EncryptUtil.isPasswdEncryptionSupported()) {
                    encryptAndStoreSalt(salt, editor);
                }
                else {
                    saltBase64 = Base64.encodeToString(salt, 0);
                    editor.putString(PREF_SALT, saltBase64);
                }
                editor.commit();
            }
            else {

                if (EncryptUtil.isPasswdEncryptionSupported()) {
                    String ivBase64 = defaultSharedPreferences
                            .getString(PREF_SALT_IV, null);
                    if (ivBase64 == null) {
                        // salt not encrypted, do it now
                        salt = Base64.decode(saltBase64, 0);
                        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                        encryptAndStoreSalt(salt, editor);
                        editor.commit();
                    }
                    else {
                        // decrypt salt
                        byte[] iv = Base64.decode(ivBase64, 0);
                        byte[] encSalt = Base64.decode(saltBase64, 0);
                        Pair<byte[], byte[]> encrypted = new Pair<>(iv, encSalt);
                        salt = EncryptUtil.decryptData(SecretChecker.KEY_ALIAS_SALT, encrypted);
                    }
                }
                else {
                    // get unencrypted salt
                    salt = Base64.decode(saltBase64, 0);
                }
            }


            Log.d("SALT", Arrays.toString(salt));
            return salt;
        }

        private static void encryptAndStoreSalt(byte[] salt, SharedPreferences.Editor editor) {

            Pair<byte[], byte[]> encrypted = EncryptUtil.encryptData(SecretChecker.KEY_ALIAS_SALT, salt);

            String encSaltBase64 = Base64.encodeToString(encrypted.second, 0);
            String ivBase64 = Base64.encodeToString(encrypted.first, 0);

            editor.putString(PREF_SALT, encSaltBase64);
            editor.putString(PREF_SALT_IV, ivBase64);
        }


        private synchronized static void openDialog(final Secret secret, final SecureActivity activity) {

            if (isRecentlyOpened(secretDialogOpened)) {
                return;
            }
            secretDialogOpened = System.currentTimeMillis();

            activity.refresh(true); // show all data as invalid

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            final EditText input = new EditText(activity);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.requestFocus();

            final AlertDialog dialog = builder.setTitle(R.string.title_encryption_password_required)
                    .setMessage(R.string.message_encrypt_password_required)
                    .setView(input)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            secretDialogOpened = 0;
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {

                    final AtomicInteger failCounter = new AtomicInteger();
                    Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    buttonPositive.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            char[] pwd = EncryptUtil.getCharArray(input.getText());
                            try {
                                if (pwd == null || pwd.length == 0) {
                                    input.setError(activity.getString(R.string.title_encryption_password_required));
                                    return;
                                } else if (EncryptUtil.isPasswdEncryptionSupported() &&
                                        !isPasswordValid(pwd, activity, getSalt(activity))) {
                                    input.setError(activity.getString(R.string.wrong_password));
                                    if (failCounter.incrementAndGet() < Constants.MAX_PASSWD_ATTEMPTS) {
                                        return; // try again
                                    }
                                } else {
                                    secret.setDigest(EncryptUtil.generateKey(pwd, getSalt(activity)));
                                    activity.refresh(false); // show correct encrypted data
                                }
                            } finally {
                                EncryptUtil.clearPwd(pwd);
                            }

                            secretDialogOpened = 0;

                            dialog.dismiss();
                        }
                    });

                }
            });
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

        }

        public static boolean isPasswordStored(Activity activity) {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(activity);
            String encPasswdBase64 = defaultSharedPreferences
                    .getString(PREF_PASSWD, null);
            String ivBase64 = defaultSharedPreferences
                    .getString(PREF_PASSWD_IV, null);

            return (encPasswdBase64 != null && ivBase64 != null);
        }

        public static boolean isPasswordValid(char[] pwd, Activity activity, byte[] salt) {
            if (pwd != null) {
                SharedPreferences defaultSharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(activity);
                String encPasswdBase64 = defaultSharedPreferences
                        .getString(PREF_PASSWD, null);
                String ivBase64 = defaultSharedPreferences
                        .getString(PREF_PASSWD_IV, null);

                if (encPasswdBase64 != null && ivBase64 != null) {
                    byte[] encPasswd = Base64.decode(encPasswdBase64, 0);
                    byte[] iv = Base64.decode(ivBase64, 0);

                    byte[] key = EncryptUtil.generateKey(pwd, salt);
                    byte[] hashedStoredKey = EncryptUtil.decryptData(KEY_ALIAS_PASSWD, new Pair<>(iv, encPasswd));
                    byte[] hashedPwd = EncryptUtil.fastHash(key, salt);

                    return Arrays.equals(hashedPwd, hashedStoredKey);
                }
            }
            return true; //bypass if nothing is stored
        }

        public static boolean isSaltEncrypted(Activity activity) {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(activity);
            String ivBase64 = defaultSharedPreferences
                    .getString(PREF_SALT_IV, null);
            return  ivBase64 != null;
        }

        private static boolean isRecentlyOpened(long secretDialogOpened) {
            long current = System.currentTimeMillis();

            return secretDialogOpened >= current - DELTA_DIALOG_OPENED;
        }
    }

}
