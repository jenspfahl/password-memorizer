package de.jepfa.obfusser.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;

import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.EncryptUtil;
import de.jepfa.obfusser.model.Secret;

public abstract class BaseActivity extends AppCompatActivity {

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


        private static volatile boolean secretDialogOpen;

        public static byte[] getOrAskForSecret(BaseActivity activity) {
            boolean passwordCheckEnabled = PreferenceManager
                    .getDefaultSharedPreferences(activity)
                    .getBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, false);

            if (passwordCheckEnabled) {
                Secret secret = Secret.getOrCreate();

                if (secret.isOutdated() || !secret.isFilled()) {
                    // make all not readable by setting key as invalid
                    secret.setInvalidDigest();
                    // open user secret dialoh
                    openDialog(secret, activity);
                } else {
                    secret.renew();
                }

                return secret.getDigest();
            }

            return null;
        }


        private synchronized static void openDialog(final Secret secret, final BaseActivity activity) {

            if (secretDialogOpen) {
                return;
            }
            secretDialogOpen = true;

            activity.refresh(true); // show all data as invalid

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            final EditText input = new EditText(activity);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            builder.setTitle("Enter PIN")
                    .setMessage("Login!")
                    .setView(input)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String pwd = input.getText().toString();
                            if (TextUtils.isEmpty(pwd)) {
                                secret.setDigest(null);
                            }
                            else {
                                secret.setDigest(EncryptUtil.generateKey(pwd));
                            }
                            secret.renew();
                            secretDialogOpen = false;

                            activity.refresh(false); // show correct encrypted data

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();

        }
    }

}
