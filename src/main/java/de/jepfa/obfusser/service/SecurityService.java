package de.jepfa.obfusser.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.repository.credential.CredentialRepository;
import de.jepfa.obfusser.repository.group.GroupRepository;
import de.jepfa.obfusser.repository.template.TemplateRepository;

/**
 * Service to do some crypto actions for {@link Credential}s and {@link Template}s.
 *
 * @author Jens Pfahl
 */
public class SecurityService extends IntentService {

    private static final String ACTION_ENCRYPT_ALL = "de.jepfa.obfusser.service.action.encrypt_all";
    private static final String ACTION_DECRYPT_ALL = "de.jepfa.obfusser.service.action.decrypt_all";
    private static final String ACTION_DO_STRING_CRYPT = "de.jepfa.obfusser.service.action.do_string_crypt";
    private static final String PARAM_KEY = "de.jepfa.obfusser.service.param.key";
    private static final String PARAM_WITH_UUID = "de.jepfa.obfusser.service.param.with_uuid";

    private final CredentialRepository credentialRepo;
    private final TemplateRepository templateRepo;
    private final GroupRepository groupRepo;


    public SecurityService() {
        super("SecurityService");

        credentialRepo = new CredentialRepository(getApplication());
        templateRepo = new TemplateRepository(getApplication());
        groupRepo = new GroupRepository(getApplication());
    }


    public static void startEncryptAll(Context context, byte[] key, boolean withUuid) {
        Intent intent = new Intent(context, SecurityService.class);
        intent.setAction(ACTION_ENCRYPT_ALL);
        intent.putExtra(PARAM_KEY, key);
        intent.putExtra(PARAM_WITH_UUID, withUuid);
        context.startService(intent);
    }

    public static void startDoStringCrypt(Context context) {
        Intent intent = new Intent(context, SecurityService.class);
        intent.setAction(ACTION_DO_STRING_CRYPT);
        context.startService(intent);
    }

    public static void startDecryptAll(Context context, byte[] key, boolean withUuid) {
        Intent intent = new Intent(context, SecurityService.class);
        intent.setAction(ACTION_DECRYPT_ALL);
        intent.putExtra(PARAM_KEY, key);
        intent.putExtra(PARAM_WITH_UUID, withUuid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            boolean withUuid = intent.getBooleanExtra(PARAM_WITH_UUID, false);
            final String action = intent.getAction();
            if (ACTION_ENCRYPT_ALL.equals(action)) {
                encryptAll(intent.getByteArrayExtra(PARAM_KEY), withUuid);
            }
            if (ACTION_DECRYPT_ALL.equals(action)) {
                decryptAll(intent.getByteArrayExtra(PARAM_KEY), withUuid);
            }
            if (ACTION_DO_STRING_CRYPT.equals(action)) {
                doStringCrypt(intent.getByteArrayExtra(PARAM_KEY), withUuid);
            }
        }
    }

    private void encryptAll(byte[] key, boolean encWithUuid) {
        List<Template> allTemplates = templateRepo.getAllTemplatesSync();
        for (Template template : allTemplates) {
            template.encrypt(key, encWithUuid);
            templateRepo.update(template);
        }

        List<Credential> allCredentials = credentialRepo.getAllCredentialsSync();
        for (Credential credential : allCredentials) {
            credential.encrypt(key, encWithUuid);
            credentialRepo.update(credential);
        }
    }

    private void decryptAll(byte[] key, boolean decWithUuid) {
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

    private void doStringCrypt(byte[] key, boolean decWithUuid) {
        List<Template> allTemplates = templateRepo.getAllTemplatesSync();
        for (Template template : allTemplates) {
            templateRepo.update(template);
        }

        List<Credential> allCredentials = credentialRepo.getAllCredentialsSync();
        for (Credential credential : allCredentials) {
            credentialRepo.update(credential);
        }

        List<Group> allGroups = groupRepo.getAllGroupsSync();
        for (Group group : allGroups) {
            groupRepo.update(group);
        }
    }


}
