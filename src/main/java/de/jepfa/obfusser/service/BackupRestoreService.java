package de.jepfa.obfusser.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.repository.credential.CredentialRepository;
import de.jepfa.obfusser.repository.group.GroupRepository;
import de.jepfa.obfusser.repository.template.TemplateRepository;
import de.jepfa.obfusser.util.encrypt.FileUtil;

/**
 * Service to backup and restore all {@link Credential}s, {@link Template}s anf {@link de.jepfa.obfusser.model.Group}s.
 *
 * @author Jens Pfahl
 */
public class BackupRestoreService extends IntentService {

    private static final String ACTION_BACKUP_ALL = "de.jepfa.obfusser.service.action.backup_all";
    private static final String ACTION_RESTORE_ALL = "de.jepfa.obfusser.service.action.restore_all";
    private static final String PARAM_KEY = "de.jepfa.obfusser.service.param.key";
    private static final String PARAM_TRANSFER_KEY = "de.jepfa.obfusser.service.param.transferkey";
    private static final String PARAM_TRANSFER_SALT = "de.jepfa.obfusser.service.param.transfersalt";
    private static final String PARAM_WITH_UUID = "de.jepfa.obfusser.service.param.with_uuid";
    private static final String BACKUP_FILE_BASE = "password-memorizer-data-";
    private static final String CHANNEL_ID = "de.jepfa.notificationchannel.0";

    private final CredentialRepository credentialRepo;
    private final TemplateRepository templateRepo;
    private final GroupRepository groupRepo;


    public BackupRestoreService() {
        super("BackupRestoreService");

        credentialRepo = new CredentialRepository(getApplication());
        templateRepo = new TemplateRepository(getApplication());
        groupRepo = new GroupRepository(getApplication());
    }


    public static void startBackupAll(Context context, byte[] encryptKey, byte[] transferKey, byte[] transferSalt, boolean withUuid) {
        Intent intent = new Intent(context, BackupRestoreService.class);
        intent.setAction(ACTION_BACKUP_ALL);
        intent.putExtra(PARAM_KEY, encryptKey);
        intent.putExtra(PARAM_TRANSFER_KEY, transferKey);
        intent.putExtra(PARAM_TRANSFER_SALT, transferSalt);
        intent.putExtra(PARAM_WITH_UUID, withUuid);
        context.startService(intent);
    }

    public static void startRestoreAll(Context context, byte[] key, byte[] salt, boolean withUuid) {
        Intent intent = new Intent(context, BackupRestoreService.class);
        intent.setAction(ACTION_RESTORE_ALL);
        intent.putExtra(PARAM_KEY, key);
        intent.putExtra(PARAM_WITH_UUID, withUuid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            boolean withUuid = intent.getBooleanExtra(PARAM_WITH_UUID, false);
            final String action = intent.getAction();
            if (ACTION_BACKUP_ALL.equals(action)) {
                backupAll(intent.getByteArrayExtra(PARAM_KEY),
                        intent.getByteArrayExtra(PARAM_TRANSFER_KEY),
                        intent.getByteArrayExtra(PARAM_TRANSFER_SALT),
                        withUuid);
            }
            if (ACTION_RESTORE_ALL.equals(action)) {
                restoreAll(intent.getByteArrayExtra(PARAM_KEY), withUuid);
            }
        }
    }

    private void backupAll(byte[] encryptKey, byte[] transferKey, byte[] transferSalt, boolean encWithUuid) {
        JsonObject root = new JsonObject();
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            root.addProperty("version", pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("BACKUPALL", "cannot get version code", e);
        }


        root.addProperty("date", new Date().toString());
        root.addProperty("enc", encryptKey != null);
        root.addProperty("encWithUuid", encWithUuid);
        root.addProperty("salt", Base64.encodeToString(transferSalt, Base64.NO_WRAP));
        Gson gson = new Gson();

        List<Credential> credentials = credentialRepo.getAllCredentialsSync();
        for (Credential credential : credentials) {
            if (encryptKey != null) {
                credential.decrypt(encryptKey, encWithUuid);
                credential.encrypt(transferKey, encWithUuid);
            }
        }

        Type credentialsType = new TypeToken<List<Credential>>(){}.getType();
        root.add("Credentials", gson.toJsonTree(credentials, credentialsType));


        List<Template> templates = templateRepo.getAllTemplatesSync();
        for (Template template : templates) {
            if (encryptKey != null) {
                template.encrypt(encryptKey, encWithUuid);
                template.decrypt(transferKey, encWithUuid);
            }
        }

        Type templatesType = new TypeToken<List<Template>>(){}.getType();
        root.add("Templates", gson.toJsonTree(templates, templatesType));

        List<Group> groups = groupRepo.getAllGroupsSync();

        Type groupsType = new TypeToken<List<Group>>(){}.getType();
        root.add("Groups", gson.toJsonTree(groups, groupsType));



        createNotificationChannel();

        boolean success = false;

        if (FileUtil.isExternalStorageWritable()) {
            String backupFileName = BACKUP_FILE_BASE + System.currentTimeMillis() + ".json";
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File backupFile = new File(path, backupFileName);

            try {
                backupFile.createNewFile();

                try (FileWriter file = new FileWriter(backupFile)) {
                    file.write(root.toString());
                }

                Log.d("BACKUP", "to file " + backupFile);

                Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);


                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(android.R.drawable.stat_notify_sdcard)
                        .setContentTitle("Backup ready")
                        .setContentText("Backup file " + backupFileName + " created under Download folder.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .addAction(new NotificationCompat.Action(android.R.drawable.stat_notify_sdcard,"open download folder", pIntent));

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(10001, mBuilder.build());

                success = true;

            } catch (IOException e) {
                Log.e("BACKUP", "cannot write to file " + backupFile, e);
            }
        }

        if (!success) {

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("Backup failure")
                    .setContentText("Cannot create backup file.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(10002, mBuilder.build());
        }
    }

    private void restoreAll(byte[] key, boolean decWithUuid) {
        List<Template> allTemplates = templateRepo.getAllTemplatesSync();
        for (Template template : allTemplates) {
            template.decrypt(key, decWithUuid);
            templateRepo.update(template);
        }

        List<Credential> allCredentials = credentialRepo.getAllCredentialsSync();
        for (Credential credential : allCredentials) {
            credential.decrypt(key, decWithUuid);
            credentialRepo.update(credential);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ("Password Memorizer Notifications");
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
