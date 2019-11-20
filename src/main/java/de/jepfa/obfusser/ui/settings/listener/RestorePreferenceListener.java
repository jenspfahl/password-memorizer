package de.jepfa.obfusser.ui.settings.listener;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Arrays;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Secret;
import de.jepfa.obfusser.service.BackupRestoreService;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.PermissionChecker;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.FileUtil;
import de.jepfa.obfusser.util.encrypt.EncryptUtil;

public class RestorePreferenceListener implements Preference.OnPreferenceClickListener {

    public static final int REQUEST_CODE_RESTORE_FILE = 1002;

    private final Activity activity;

    public RestorePreferenceListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {

        final byte[] key = SecureActivity.SecretChecker.getOrAskForSecret(activity);
        if (key == Secret.INVALID_DIGEST) {
            Toast toast = Toast.makeText(activity, R.string.secret_disappeared, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return false;
        }

        PermissionChecker.INSTANCE.verifyReadStoragePermissions(activity);

        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        Intent chooserIntent = Intent.createChooser(intent, activity.getString(R.string.chooser_select_restore_file));
        activity.startActivityForResult(chooserIntent, REQUEST_CODE_RESTORE_FILE);

        return false; // it is a pseudo preference
    }

    public static void doRestoreProcess(final Activity activity, Intent data) {

        Uri selectedFile = data.getData();
        final byte[] key = SecureActivity.SecretChecker.getOrAskForSecret(activity);
        if (key == Secret.INVALID_DIGEST) {
            Toast toast = Toast.makeText(activity, R.string.secret_disappeared, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String content = null;
        JsonObject jsonContent = null;

        if (FileUtil.INSTANCE.isExternalStorageReadable()) {
            try {
                content = FileUtil.INSTANCE.readFile(activity, selectedFile);
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
            Toast toast = Toast.makeText(activity, activity.getString(R.string.toast_restore_failure), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        else {
            LayoutInflater inflater = activity.getLayoutInflater();

            final View passwordView = inflater.inflate(R.layout.dialog_setup_password, null);
            final EditText firstPassword = passwordView.findViewById(R.id.first_password);
            final EditText secondPassword = passwordView.findViewById(R.id.second_password);
            final Switch overwriteExistingSwitch = passwordView.findViewById(R.id.switch_store_password);
            overwriteExistingSwitch.setText(activity.getString(R.string.message_restore_dialog_overwrite_existing));
            final Switch disturbPatternsSwitch = passwordView.findViewById(R.id.disturb_equal_patterns);
            disturbPatternsSwitch.setVisibility(View.GONE);

            try {
                final boolean passwordCheckEnabled = jsonContent.get(BackupRestoreService.JSON_ENC).getAsBoolean();
                final int credentialsCount = jsonContent.get(BackupRestoreService.JSON_CREDENTIALS_COUNT).getAsInt();
                final int templatesCount = jsonContent.get(BackupRestoreService.JSON_TEMPLATES_COUNT).getAsInt();
                final int groupsCount = jsonContent.get(BackupRestoreService.JSON_GROUPS_COUNT).getAsInt();
                final String fromDate = jsonContent.get(BackupRestoreService.JSON_DATE).getAsString();


                if (credentialsCount == 0 && templatesCount == 0 && groupsCount == 0) {
                    Toast toast = Toast.makeText(activity, R.string.toast_restore_nodata, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                String message;
                if (passwordCheckEnabled) {
                    message = activity.getString(R.string.message_restore_dialog_encrypted, credentialsCount, templatesCount, groupsCount);
                }
                else {
                    message = activity.getString(R.string.message_restore_dialog_noenc, credentialsCount, templatesCount, groupsCount);
                    firstPassword.setVisibility(View.GONE);
                    secondPassword.setVisibility(View.GONE);
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                final AlertDialog dialog = builder.setTitle(activity.getString(R.string.title_restore_dialog, fromDate))
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
                                            firstPassword.setError(activity.getString(R.string.password_required));
                                            firstPassword.requestFocus();
                                            return;
                                        }

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
                                        activity,
                                        fcontent,
                                        overwriteExistingSwitch.isChecked(),
                                        transferKey,
                                        key,
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
            } catch (Exception e) {
                Log.e("RESTORE", "cannot read metadata for " + selectedFile, e);
                Toast toast = Toast.makeText(activity, activity.getString(R.string.toast_restore_failure), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

}