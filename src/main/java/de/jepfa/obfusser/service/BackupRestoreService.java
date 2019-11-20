package de.jepfa.obfusser.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.repository.credential.CredentialRepository;
import de.jepfa.obfusser.repository.group.GroupRepository;
import de.jepfa.obfusser.repository.template.TemplateRepository;
import de.jepfa.obfusser.ui.common.NotificationHelper;
import de.jepfa.obfusser.util.FileUtil;

/**
 * Service to backup and restore all {@link Credential}s, {@link Template}s and {@link de.jepfa.obfusser.model.Group}s.
 *
 * @author Jens Pfahl
 */
public class BackupRestoreService extends IntentService {

    private static final String ACTION_BACKUP_ALL = "de.jepfa.obfusser.service.action.backup_all";
    private static final String ACTION_RESTORE_ALL = "de.jepfa.obfusser.service.action.restore_all";
    private static final String PARAM_FILE_URI = "de.jepfa.obfusser.service.param.file_uri";
    private static final String PARAM_ENCRYPT_KEY = "de.jepfa.obfusser.service.param.encryptkey";
    private static final String PARAM_WITH_UUID = "de.jepfa.obfusser.service.param.with_uuid";
    private static final String PARAM_TRANSFER_KEY = "de.jepfa.obfusser.service.param.transferkey";
    private static final String PARAM_TRANSFER_SALT = "de.jepfa.obfusser.service.param.transfersalt";
    private static final String PARAM_CONTENT = "de.jepfa.obfusser.service.param.content";
    private static final String PARAM_OVERWRITE_EXISTING = "de.jepfa.obfusser.service.param.overwrite_existing";

    public static final String BACKUP_FILE_BASE = "password-memorizer-";

    public static final String JSON_APP_VERSIONCODE = "appVersionCode";
    public static final String JSON_APP_VERSIONNAME = "appVersionName";
    public static final String JSON_DATE = "date";
    public static final String JSON_ENC = "enc";
    public static final String JSON_ENC_WITH_UUID = "encWithUuid";
    public static final String JSON_SALT = "salt";
    public static final String JSON_CREDENTIALS = "Credentials";
    public static final String JSON_TEMPLATES = "Templates";
    public static final String JSON_GROUPS = "Groups";
    public static final String JSON_CREDENTIALS_COUNT = "CredentialsCount";
    public static final String JSON_TEMPLATES_COUNT = "TemplatesCount";
    public static final String JSON_GROUPS_COUNT = "GroupsCount";
    public static final Type CREDENTIALS_TYPE = new TypeToken<List<Credential>>(){}.getType();
    public static final Type TEMPLATES_TYPE = new TypeToken<List<Template>>(){}.getType();
    public static final Type GROUPS_TYPE = new TypeToken<List<Group>>(){}.getType();
    public static final String MIME_TYPE_JSON = "text/json";


    private final CredentialRepository credentialRepo;
    private final TemplateRepository templateRepo;
    private final GroupRepository groupRepo;

    private Handler handler;


    public BackupRestoreService() {
        super("BackupRestoreService");

        credentialRepo = new CredentialRepository(getApplication());
        templateRepo = new TemplateRepository(getApplication());
        groupRepo = new GroupRepository(getApplication());

        handler = new Handler();
    }


    public static void startBackupAll(Context context, Uri fileUri, byte[] encryptKey, byte[] transferKey, byte[] transferSalt, boolean withUuid) {
        Intent intent = new Intent(context, BackupRestoreService.class);
        intent.setAction(ACTION_BACKUP_ALL);
        intent.putExtra(PARAM_FILE_URI, fileUri);
        intent.putExtra(PARAM_ENCRYPT_KEY, encryptKey);
        intent.putExtra(PARAM_TRANSFER_KEY, transferKey);
        intent.putExtra(PARAM_TRANSFER_SALT, transferSalt);
        intent.putExtra(PARAM_WITH_UUID, withUuid);
        context.startService(intent);
    }

    public static void startRestoreAll(Context context, String jsonContent, boolean overwriteExisting,
                                       byte[] transferKey, byte[] encryptKey, boolean withUuid) {
        Intent intent = new Intent(context, BackupRestoreService.class);
        intent.setAction(ACTION_RESTORE_ALL);
        intent.putExtra(PARAM_ENCRYPT_KEY, encryptKey);
        intent.putExtra(PARAM_WITH_UUID, withUuid);
        intent.putExtra(PARAM_CONTENT, jsonContent);
        intent.putExtra(PARAM_OVERWRITE_EXISTING, overwriteExisting);
        intent.putExtra(PARAM_TRANSFER_KEY, transferKey);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            boolean withUuid = intent.getBooleanExtra(PARAM_WITH_UUID, false);
            final String action = intent.getAction();
            if (ACTION_BACKUP_ALL.equals(action)) {
                backupAll(
                        (Uri)intent.getParcelableExtra(PARAM_FILE_URI),
                        intent.getByteArrayExtra(PARAM_ENCRYPT_KEY),
                        intent.getByteArrayExtra(PARAM_TRANSFER_KEY),
                        intent.getByteArrayExtra(PARAM_TRANSFER_SALT),
                        withUuid);
            }
            if (ACTION_RESTORE_ALL.equals(action)) {
                restoreAll(
                        intent.getStringExtra(PARAM_CONTENT),
                        intent.getBooleanExtra(PARAM_OVERWRITE_EXISTING, false),
                        intent.getByteArrayExtra(PARAM_TRANSFER_KEY),
                        intent.getByteArrayExtra(PARAM_ENCRYPT_KEY),
                        withUuid);
            }
        }
    }

    private void backupAll(Uri fileUri, byte[] encryptKey, byte[] transferKey, byte[] transferSalt, boolean encWithUuid) {
        JsonObject root = new JsonObject();
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            root.addProperty(JSON_APP_VERSIONCODE, pInfo.versionCode);
            root.addProperty(JSON_APP_VERSIONNAME, pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("BACKUPALL", "cannot get version code", e);
        }

        root.addProperty(JSON_DATE, Constants.SDF_DT_MEDIUM.format(new Date()));
        root.addProperty(JSON_ENC, encryptKey != null);
        root.addProperty(JSON_ENC_WITH_UUID, encWithUuid);
        root.addProperty(JSON_SALT, Base64.encodeToString(transferSalt, Base64.NO_WRAP));
        Gson gson = new Gson();

        List<Credential> credentials = credentialRepo.getAllCredentialsSync();
        for (Credential credential : credentials) {
            if (encryptKey != null) {
                credential.decrypt(encryptKey, encWithUuid);
                credential.encrypt(transferKey, encWithUuid);
            }
        }
        root.add(JSON_CREDENTIALS, gson.toJsonTree(credentials, CREDENTIALS_TYPE));
        root.addProperty(JSON_CREDENTIALS_COUNT, credentials.size());


        List<Template> templates = templateRepo.getAllTemplatesSync();
        for (Template template : templates) {
            if (encryptKey != null) {
                template.decrypt(encryptKey, encWithUuid);
                template.encrypt(transferKey, encWithUuid);
            }
        }
        root.add(JSON_TEMPLATES, gson.toJsonTree(templates, TEMPLATES_TYPE));
        root.addProperty(JSON_TEMPLATES_COUNT, templates.size());

        List<Group> groups = groupRepo.getAllGroupsSync();
        root.add(JSON_GROUPS, gson.toJsonTree(groups, GROUPS_TYPE));
        root.addProperty(JSON_GROUPS_COUNT, groups.size());


        NotificationHelper.INSTANCE.createNotificationChannel(this,
                NotificationHelper.INSTANCE.getCHANNEL_ID_BACKUP(), getString(R.string.notification_channel_backup_title));

        boolean success = false;

        if (FileUtil.INSTANCE.isExternalStorageWritable()) {

            try {
                success = FileUtil.INSTANCE.writeFile(this, fileUri, root.toString());
                String content = FileUtil.INSTANCE.readFile(this, fileUri);
                if (TextUtils.isEmpty(content)) {
                    Log.e("BACKUP", "Empty file created: " + fileUri);
                    success = false;
                }
            } catch (Exception e) {
                Log.e("BACKUP", "Cannot write file " + fileUri, e);
            }

            if (success) {
                String fileName = FileUtil.INSTANCE.getFileName(this, fileUri);

                Log.e("BACKUP", "to file " + fileName);
                //MediaScannerConnection.scanFile(this, new String[] {fileUri.getPath()}, new String[] {MIME_TYPE_JSON}, null);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setData(fileUri); //TODO this doesnt work
                PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                        Intent.createChooser(intent, getString(R.string.notification_action_backup_done)), 0);


                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), NotificationHelper.INSTANCE.getCHANNEL_ID_BACKUP())
                        .setSmallIcon(R.drawable.ic_notif_obfusser)
                        .setContentTitle(getString(R.string.notification_backup_title))
                        .setContentText(getString(R.string.notification_backup_message, fileName))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                /* TODO doesnt work
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBuilder.addAction(
                            new NotificationCompat.Action(
                                    android.R.drawable.stat_notify_sdcard,
                                    getString(R.string.notification_action_backup_done), pIntent));
                }
                else {
                    mBuilder.setContentIntent(pIntent);
                }*/
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(NotificationHelper.INSTANCE.getNOTIFICATION_ID_BACKUP_SUCCESS(), mBuilder.build());

                success = true;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), R.string.toast_backup_done, Toast.LENGTH_LONG).show();
                    }
                });

            }
        }

        if (!success) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getBaseContext(), R.string.toast_backup_failed, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });

        }
    }

    @NonNull
    public static String getBackupFileName() {
        return BACKUP_FILE_BASE + Constants.SDF_D_INTERNATIONAL.format(new Date()) + ".json";
    }

    private void restoreAll(String content, boolean overwriteExisting, byte[] transferKey, byte[] encryptKey, boolean withUuid) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonContent = parser.parse(content).getAsJsonObject();

            boolean decWithUuid = jsonContent.get(JSON_ENC_WITH_UUID).getAsBoolean();

            Gson gson = new Gson();

            JsonArray jsonCredentials = jsonContent.get(JSON_CREDENTIALS).getAsJsonArray();
            List<Credential> otherCredentials = gson.fromJson(jsonCredentials, CREDENTIALS_TYPE);

            JsonArray jsonTemplates = jsonContent.get(JSON_TEMPLATES).getAsJsonArray();
            List<Template> otherTemplates = gson.fromJson(jsonTemplates, TEMPLATES_TYPE);

            JsonArray jsonGroups = jsonContent.get(JSON_GROUPS).getAsJsonArray();
            List<Group> otherGroups = gson.fromJson(jsonGroups, GROUPS_TYPE);

            Map<Integer, Group> otherGroupToExistingMap = new HashMap<>();

            List<Group> existingGroups = groupRepo.getAllGroupsSync();
            outer: for (Group otherGroup : otherGroups) {
                for (Group existingGroup : existingGroups) {
                    if (existingGroup.getName().equals(otherGroup.getName())) {
                        if (overwriteExisting) {
                            existingGroup.setInfo(otherGroup.getInfo());
                            existingGroup.setColor(otherGroup.getColor());
                            groupRepo.updateSync(existingGroup);
                        }
                        otherGroupToExistingMap.put(otherGroup.getId(), existingGroup);
                        continue outer;
                    }
                }
                int oldId = otherGroup.getId();
                otherGroup.unsetId();
                long newId = groupRepo.insertSync(otherGroup);
                otherGroup.setId((int) newId);
                otherGroupToExistingMap.put(oldId, otherGroup);
            }

            List<Template> existingTemplates = templateRepo.getAllTemplatesSync();
            outer: for (Template otherTemplate : otherTemplates) {
                Group existingGroup = otherGroupToExistingMap.get(otherTemplate.getGroupId());

                for (Template existingTemplate : existingTemplates) {
                    if (existingTemplate.getName().equals(otherTemplate.getName())) {

                        if (overwriteExisting) {
                            existingTemplate.setInfo(otherTemplate.getInfo());
                            existingTemplate.setGroupId(otherTemplate.getGroupId());

                            existingTemplate.setPatternFromExchangeFormat(
                                    otherTemplate.getPatternAsExchangeFormat(false, transferKey, decWithUuid),
                                    encryptKey,
                                    withUuid);

                            existingTemplate.setHints(
                                    otherTemplate.getHints(transferKey, decWithUuid),
                                    encryptKey,
                                    withUuid);

                            if (existingGroup != null) {
                                existingTemplate.setGroupId(existingGroup.getId());
                            }
                            templateRepo.updateSync(existingTemplate);
                        }

                        continue outer;
                    }
                }

                if (existingGroup != null) {
                    otherTemplate.setGroupId(existingGroup.getId());
                }
                otherTemplate.unsetId();
                otherTemplate.decrypt(transferKey, decWithUuid);
                otherTemplate.encrypt(encryptKey, withUuid);
                templateRepo.insertSync(otherTemplate);
            }

            List<Credential> existingCredentials = credentialRepo.getAllCredentialsSync();
            outer: for (Credential otherCredential : otherCredentials) {
                Group existingGroup = otherGroupToExistingMap.get(otherCredential.getGroupId());

                for (Credential existingCredential : existingCredentials) {
                    if (existingCredential.getName().equals(otherCredential.getName())) {

                        if (overwriteExisting) {
                            existingCredential.setInfo(otherCredential.getInfo());
                            existingCredential.setGroupId(otherCredential.getGroupId());
                            //no template assoc existingCredential.setTemplateId(otherCredential.getTemplateId());

                            existingCredential.setPatternFromExchangeFormat(
                                    otherCredential.getPatternAsExchangeFormat(false, transferKey, decWithUuid),
                                    encryptKey,
                                    withUuid);

                            existingCredential.setHints(
                                    otherCredential.getHints(transferKey, decWithUuid),
                                    encryptKey,
                                    withUuid);

                            if (existingGroup != null) {
                                existingCredential.setGroupId(existingGroup.getId());
                            }
                            credentialRepo.updateSync(existingCredential);
                        }

                        continue outer;
                    }
                }

                if (existingGroup != null) {
                    otherCredential.setGroupId(existingGroup.getId());
                }
                otherCredential.unsetId();
                otherCredential.decrypt(transferKey, decWithUuid);
                otherCredential.encrypt(encryptKey, withUuid);
                otherCredential.setTemplateId(null); //no template assoc

                credentialRepo.insertSync(otherCredential);
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), R.string.toast_restore_done, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e("RESTORE", "Import error!", e);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getBaseContext(), R.string.toast_restore_aborted, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });
        }

    }


}
