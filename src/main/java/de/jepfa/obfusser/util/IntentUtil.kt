package de.jepfa.obfusser.util

import android.content.Intent

import java.util.ArrayList
import java.util.HashMap

import de.jepfa.obfusser.model.CryptString
import de.jepfa.obfusser.model.Credential
import de.jepfa.obfusser.model.Group
import de.jepfa.obfusser.model.PatternHolder
import de.jepfa.obfusser.model.SecurePatternHolder
import de.jepfa.obfusser.model.Template

/**
 * Utils to easily work with Android Intents.
 *
 * @author Jens Pfahl
 */
object IntentUtil {


    val KV_DELIMITER = "_"

    private fun fillPatternFromIntent(pattern: SecurePatternHolder, intent: Intent) {

        pattern.id = intent.getIntExtra(SecurePatternHolder.ATTRIB_ID, 0)
        pattern.name = CryptString.of(intent.getStringExtra(SecurePatternHolder.ATTRIB_NAME))
        pattern.setInfo(CryptString.of(intent.getStringExtra(SecurePatternHolder.ATTRIB_INFO)))
        pattern.patternInternal = CryptString.of(intent.getStringExtra(SecurePatternHolder.ATTRIB_PATTERN_INTERNAL))
        pattern.uuid = intent.getStringExtra(SecurePatternHolder.ATTRIB_UUID)
        val obfusPatternLength = intent.getIntExtra(SecurePatternHolder.ATTRIB_OBFUS_PATTERN_LENGTH, 0)
        if (obfusPatternLength != 0) {
            pattern.obfusPatternLength = obfusPatternLength
        }


        val groupId = intent.getIntExtra(PatternHolder.ATTRIB_GROUP_ID, 0)
        if (groupId != 0) {
            pattern.groupId = groupId
        }

        val hintsList = intent.getStringArrayListExtra(PatternHolder.ATTRIB_HINTS)
        convertAndSetHintsFromTransport(pattern, hintsList)
    }


    fun createCredentialFromIntent(intent: Intent): Credential {
        val credential = Credential()
        fillPatternFromIntent(credential, intent)

        val templateId = intent.getIntExtra(Credential.ATTRIB_TEMPLATE_ID, 0)
        if (templateId != 0) {
            credential.templateId = templateId
        }

        return credential
    }

    fun createTemplateFromIntent(intent: Intent): Template {
        val template = Template()
        fillPatternFromIntent(template, intent)

        return template
    }

    fun createGroupFromIntent(intent: Intent): Group {
        val group = Group()
        group.id = intent.getIntExtra(Group.ATTRIB_ID, 0)
        group.name = CryptString.of(intent.getStringExtra(Group.ATTRIB_NAME))
        group.info = CryptString.of(intent.getStringExtra(Group.ATTRIB_INFO))
        group.color = intent.getIntExtra(Group.ATTRIB_COLOR, 0)

        return group
    }

    private fun setPatternExtra(intent: Intent, pattern: SecurePatternHolder) {
        intent.putExtra(SecurePatternHolder.ATTRIB_ID, pattern.id)
        intent.putExtra(SecurePatternHolder.ATTRIB_NAME, CryptString.from(pattern.name))
        intent.putExtra(SecurePatternHolder.ATTRIB_INFO, CryptString.from(pattern.info))
        intent.putExtra(SecurePatternHolder.ATTRIB_GROUP_ID, pattern.groupId)
        intent.putExtra(SecurePatternHolder.ATTRIB_PATTERN_INTERNAL, CryptString.from(pattern.patternInternal))
        intent.putExtra(SecurePatternHolder.ATTRIB_UUID, pattern.uuid)
        intent.putExtra(SecurePatternHolder.ATTRIB_OBFUS_PATTERN_LENGTH, pattern.obfusPatternLength)

        val hints = convertHintsForTransport(pattern)
        intent.putStringArrayListExtra(Credential.ATTRIB_HINTS, hints)

    }


    fun convertAndSetHintsFromTransport(pattern: SecurePatternHolder, hintsList: ArrayList<String>?) {
        if (hintsList != null) {
            val hints = HashMap<Int, String>(hintsList.size)
            for (hintElem in hintsList) {
                val key = getKeyFromHintElem(hintElem)
                val value = getValueFromHintElem(hintElem)
                hints[key] = value
            }
            pattern.hints = hints
        }
    }

    fun convertHintsForTransport(pattern: SecurePatternHolder): ArrayList<String> {
        val hints = ArrayList<String>(pattern.hintsCount)
        for ((key, value) in pattern.hints) {
            hints.add(createHintElem(key, value))
        }
        return hints
    }

    fun setCredentialExtra(intent: Intent, credential: Credential) {
        setPatternExtra(intent, credential)
        intent.putExtra(Credential.ATTRIB_TEMPLATE_ID, credential.templateId)

    }

    fun setTemplateExtra(intent: Intent, template: Template) {
        setPatternExtra(intent, template)
    }

    fun setGroupExtra(intent: Intent, group: Group) {
        intent.putExtra(Group.ATTRIB_ID, group.id)
        intent.putExtra(Group.ATTRIB_NAME, group.name)
        intent.putExtra(Group.ATTRIB_INFO, group.info)
        intent.putExtra(Group.ATTRIB_COLOR, group.color)
    }

    private fun createHintElem(key: Int?, value: String?): String {
        return key.toString() + KV_DELIMITER + (value ?: "")
    }

    private fun getKeyFromHintElem(hintElem: String): Int {
        return Integer.parseInt(hintElem.substring(0, hintElem.indexOf(KV_DELIMITER)))
    }

    private fun getValueFromHintElem(hintElem: String): String {
        return hintElem.substring(hintElem.indexOf(KV_DELIMITER) + 1)
    }


}
