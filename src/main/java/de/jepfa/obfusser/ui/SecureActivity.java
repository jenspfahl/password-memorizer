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
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.UUID;
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
        public static final String KEY_ALIAS = "key_passwd";

        private static final String PREF_APPLICATION_UUID = "application.salt";
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


        public static synchronized String getApplicationUuid(Context context) {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String uuid = defaultSharedPreferences
                    .getString(PREF_APPLICATION_UUID, null);
            if (uuid == null) {
                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                uuid = UUID.randomUUID().toString();
                editor.putString(PREF_APPLICATION_UUID, uuid);
                editor.commit();
            }

            return uuid;
        }

        public static byte[] getApplicationSalt(Context context) {
            String uuid = getApplicationUuid(context);
            Log.d("SALT", uuid);
            if (uuid != null) {
                return uuid.getBytes();
            }

            return null;
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
                            String pwd = input.getText().toString();
                            if (TextUtils.isEmpty(pwd)) {
                                input.setError(activity.getString(R.string.title_encryption_password_required));
                                return;
                            } else if (EncryptUtil.isPasswdEncryptionSupported() &&
                                    !isPasswordValid(pwd, activity, getApplicationSalt(activity))) {
                                input.setError(activity.getString(R.string.wrong_password));
                                if (failCounter.incrementAndGet() < Constants.MAX_PASSWD_ATTEMPTS) {
                                    return; // try again
                                }
                            } else {
                                secret.setDigest(EncryptUtil.generateKey(pwd, getApplicationSalt(activity)));
                                activity.refresh(false); // show correct encrypted data
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

        public static boolean isPasswordValid(String pwd, Activity activity, byte[] salt) {
            SharedPreferences defaultSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(activity);
            String encPasswdBase64 = defaultSharedPreferences
                    .getString(PREF_PASSWD, null);
            String ivBase64 = defaultSharedPreferences
                    .getString(PREF_PASSWD_IV, null);

            if (encPasswdBase64 != null && ivBase64 != null) {
                byte[] encPasswd = Base64.decode(encPasswdBase64, 0);
                byte[] iv = Base64.decode(ivBase64, 0);

                String passwdBase64 = EncryptUtil.decryptData(KEY_ALIAS,
                        new Pair<>(iv, encPasswd));
                byte[] key = EncryptUtil.generateKey(pwd, salt);
                byte[] storedKey = Base64.decode(passwdBase64, 0);

                return Arrays.equals(key, storedKey);
            }

            return true; //bypass if nothing is stored
        }

        private static boolean isRecentlyOpened(long secretDialogOpened) {
            long current = System.currentTimeMillis();

            return secretDialogOpened >= current - DELTA_DIALOG_OPENED;
        }
    }

}
