package de.jepfa.obfusser.util;

import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.PatternHolder;
import de.jepfa.obfusser.model.Template;

/**
 * Utils to easily work with Android Intents.
 *
 * @author Jens Pfahl
 */
public class IntentUtil {


    public static final String KV_DELIMITER = "_";

    private static void fillPatternFromIntent(PatternHolder pattern, Intent intent) {

        pattern.setId(intent.getIntExtra(PatternHolder.ATTRIB_ID, 0));
        pattern.setName(intent.getStringExtra(PatternHolder.ATTRIB_NAME));
        pattern.setInfo(intent.getStringExtra(PatternHolder.ATTRIB_INFO));
        pattern.setPatternInternal(intent.getStringExtra(PatternHolder.ATTRIB_PATTERN_INTERNAL));

        int groupId = intent.getIntExtra(PatternHolder.ATTRIB_GROUP_ID, 0);
        if (groupId != 0) {
            pattern.setGroupId(groupId);
        }

        ArrayList<String> hintsList = intent.getStringArrayListExtra(PatternHolder.ATTRIB_HINTS);
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

        return group;
    }

    private static void setPatternExtra(Intent intent, PatternHolder pattern) {
        intent.putExtra(Credential.ATTRIB_ID, pattern.getId());
        intent.putExtra(Credential.ATTRIB_NAME, pattern.getName());
        intent.putExtra(Credential.ATTRIB_INFO, pattern.getInfo());
        intent.putExtra(Credential.ATTRIB_GROUP_ID, pattern.getGroupId());
        intent.putExtra(Credential.ATTRIB_PATTERN_INTERNAL, pattern.getPatternInternal());

        ArrayList<String> hints = new ArrayList<>(pattern.getHintsCount());
        for (Map.Entry<Integer, String> hintsEntry : pattern.getHints().entrySet()) {
            hints.add(createHintElem(hintsEntry.getKey(), hintsEntry.getValue()));
        }
        intent.putStringArrayListExtra(Credential.ATTRIB_HINTS, hints);

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
