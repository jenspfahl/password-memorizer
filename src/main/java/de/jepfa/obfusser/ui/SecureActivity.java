package de.jepfa.obfusser.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.EncryptUtil;
import de.jepfa.obfusser.model.Secret;

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


        public static String getApplicationUuid(Context context) {
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
            AlertDialog dialog = builder.setTitle("Password required")
                    .setMessage("Please enter your password to encrypt your patterns.")
                    .setView(input)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            secretDialogOpened = 0;
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String pwd = input.getText().toString();
                            if (TextUtils.isEmpty(pwd)) {
                                secret.setDigest(null);
                            } else {
                                secret.setDigest(EncryptUtil.generateKey(pwd, getApplicationSalt(activity)));
                                activity.refresh(false); // show correct encrypted data
                            }

                            secretDialogOpened = 0;
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

        }

        private static boolean isRecentlyOpened(long secretDialogOpened) {
            long current = System.currentTimeMillis();

            return secretDialogOpened >= current - DELTA_DIALOG_OPENED;
        }
    }

}
