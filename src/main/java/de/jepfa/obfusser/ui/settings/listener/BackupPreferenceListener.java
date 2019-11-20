package de.jepfa.obfusser.ui.settings.listener;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import java.util.Arrays;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.service.BackupRestoreService;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.PermissionChecker;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.encrypt.EncryptUtil;

public class BackupPreferenceListener implements Preference.OnPreferenceClickListener {

    public static final int REQUEST_CODE_BACKUP_FILE = 1001;

    private final Activity activity;

    public BackupPreferenceListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {

        PermissionChecker.INSTANCE.verifyRWStoragePermissions(activity);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType("text/json");
        intent.putExtra(Intent.EXTRA_TITLE, BackupRestoreService.getBackupFileName());
        activity.startActivityForResult(Intent.createChooser(intent, "Save as"), REQUEST_CODE_BACKUP_FILE);

        return false; // it is a pseudo preference
    }


    public static void doBackupProcess(final Activity activity, Intent data) {
        final Uri uri = data.getData();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

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


                                byte[] applicationSalt = SecureActivity.SecretChecker.getSalt(activity);
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
                        BackupRestoreService.startBackupAll(activity,
                                uri, key, transferKey, transferSalt,
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

    }
}