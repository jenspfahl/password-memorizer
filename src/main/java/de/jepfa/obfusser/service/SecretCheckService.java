package de.jepfa.obfusser.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.ui.BaseActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.EncryptUtil;

public class SecretCheckService extends JobService {

    private boolean secretDialogOpen;


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("CHECKER", "do it");
        boolean passwordCheckEnabled = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(SettingsActivity.PREF_ENABLE_PASSWORD, false);
        if (passwordCheckEnabled) {
            Secret secret = Secret.getOrCreate();
            if (secret.isOutdated() || !secret.isFilled()) {
                secret.setInvalidDigest();
                openDialog(secret);
            }
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private synchronized void openDialog(final Secret secret) {

        if (secretDialogOpen) {
            return;
        }
        secretDialogOpen = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

        final EditText input = new EditText(getApplicationContext());
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

                        //BaseActivity.this.recreate();//TODO find better way to reload patterns

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();


    }
}
