package de.jepfa.obfusser.util;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.PatternHolder;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.model.Template;

/**
 * Utils to easily work with Android Intents.
 *
 * @author Jens Pfahl
 */
public class IntentUtil {


    public static final String KV_DELIMITER = "_";

    private static void fillPatternFromIntent(SecurePatternHolder pattern, Intent intent) {

        pattern.setId(intent.getIntExtra(SecurePatternHolder.ATTRIB_ID, 0));
        pattern.setName(intent.getStringExtra(SecurePatternHolder.ATTRIB_NAME));
        pattern.setInfo(intent.getStringExtra(SecurePatternHolder.ATTRIB_INFO));
        pattern.setPatternInternal(intent.getStringExtra(SecurePatternHolder.ATTRIB_PATTERN_INTERNAL));
        pattern.setUuid(intent.getStringExtra(SecurePatternHolder.ATTRIB_UUID));


        int groupId = intent.getIntExtra(PatternHolder.ATTRIB_GROUP_ID, 0);
        if (groupId != 0) {
            pattern.setGroupId(groupId);
        }

        ArrayList<String> hintsList = intent.getStringArrayListExtra(PatternHolder.ATTRIB_HINTS);
        convertAndSetHintsFromTransport(pattern, hintsList);
    }


    public static Credential createCredentialFromIntent(Intent intent) {
        Credential credential = new Credential();
        fillPatternFromIntent(credential, intent);

        int templateId = intent.getIntExtra(Credential.ATTRIB_TEMPLATE_ID, 0);
        if (templateId != 0) {
            credential.setTemplateId(templateId);
        }

        return credential;
    }

    public static Template createTemplateFromIntent(Intent intent) {
        Template template = new Template();
        fillPatternFromIntent(template, intent);

        return template;
    }

    public static Group createGroupFromIntent(Intent intent) {
        Group group = new Group();
        group.setId(intent.getIntExtra(Group.ATTRIB_ID, 0));
        group.setName(intent.getStringExtra(Group.ATTRIB_NAME));
        group.setInfo(intent.getStringExtra(Group.ATTRIB_INFO));
        group.setColor(intent.getIntExtra(Group.ATTRIB_COLOR, 0));

        return group;
    }

    private static void setPatternExtra(Intent intent, SecurePatternHolder pattern) {
        intent.putExtra(SecurePatternHolder.ATTRIB_ID, pattern.getId());
        intent.putExtra(SecurePatternHolder.ATTRIB_NAME, pattern.getName());
        intent.putExtra(SecurePatternHolder.ATTRIB_INFO, pattern.getInfo());
        intent.putExtra(SecurePatternHolder.ATTRIB_GROUP_ID, pattern.getGroupId());
        intent.putExtra(SecurePatternHolder.ATTRIB_PATTERN_INTERNAL, pattern.getPatternInternal());
        intent.putExtra(SecurePatternHolder.ATTRIB_UUID, pattern.getUuid());

        ArrayList<String> hints = convertHintsForTransport(pattern);
        intent.putStringArrayListExtra(Credential.ATTRIB_HINTS, hints);

    }


    public static void convertAndSetHintsFromTransport(SecurePatternHolder pattern, ArrayList<String> hintsList) {
        if (hintsList != null) {
            Map<Integer, String> hints = new HashMap<>(hintsList.size());
            for (String hintElem : hintsList) {
                Integer key = getKeyFromHintElem(hintElem);
                String value = getValueFromHintElem(hintElem);
                hints.put(key, value);
            }
            pattern.setHints(hints);
        }
    }

    @NonNull
    public static ArrayList<String> convertHintsForTransport(SecurePatternHolder pattern) {
        ArrayList<String> hints = new ArrayList<>(pattern.getHintsCount());
        for (Map.Entry<Integer, String> hintsEntry : pattern.getHints().entrySet()) {
            hints.add(createHintElem(hintsEntry.getKey(), hintsEntry.getValue()));
        }
        return hints;
    }

    public static void setCredentialExtra(Intent intent, Credential credential) {
        setPatternExtra(intent, credential);
        intent.putExtra(Credential.ATTRIB_TEMPLATE_ID, credential.getTemplateId());

    }

    public static void setTemplateExtra(Intent intent, Template template) {
        setPatternExtra(intent, template);
    }

    public static void setGroupExtra(Intent intent, Group group) {
        intent.putExtra(Group.ATTRIB_ID, group.getId());
        intent.putExtra(Group.ATTRIB_NAME, group.getName());
        intent.putExtra(Group.ATTRIB_INFO, group.getInfo());
        intent.putExtra(Group.ATTRIB_COLOR, group.getColor());
    }

    private static String createHintElem(Integer key, String value) {
        return key + KV_DELIMITER + (value == null ? "" : value);
    }

    private static Integer getKeyFromHintElem(String hintElem) {
        return Integer.parseInt(hintElem.substring(0, hintElem.indexOf(KV_DELIMITER)));
    }

    private static String getValueFromHintElem(String hintElem) {
        return hintElem.substring(hintElem.indexOf(KV_DELIMITER) + 1);
    }


}
