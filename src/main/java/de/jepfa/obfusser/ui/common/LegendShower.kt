package de.jepfa.obfusser.ui.common

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.v7.app.AlertDialog

import de.jepfa.obfusser.Constants
import de.jepfa.obfusser.R
import de.jepfa.obfusser.model.ObfusChar
import de.jepfa.obfusser.model.Representation

object LegendShower {
    fun showLegend(activity: Activity, representation: Representation) {
        val builder = AlertDialog.Builder(activity)

        val legend = buildLegend(activity, representation, true)

        val icon = activity.applicationInfo.loadIcon(activity.packageManager)
        builder.setTitle(R.string.title_legend)
                .setMessage(legend)
                .setIcon(icon)
                .show()
    }

    fun buildLegend(activity: Activity, representation: Representation, extraSpace: Boolean): String {
        val sb = StringBuilder()
        buildObfusChar(activity, representation, sb, ObfusChar.LOWER_CASE_CHAR, R.string.lower_char, R.string.lower_char_explanation)
        buildExtraSpace(sb, extraSpace)
        buildObfusChar(activity, representation, sb, ObfusChar.UPPER_CASE_CHAR, R.string.upper_char, R.string.upper_char_explanation)
        buildExtraSpace(sb, extraSpace)
        buildObfusChar(activity, representation, sb, ObfusChar.DIGIT, R.string.digit, R.string.digit_explanation)
        buildExtraSpace(sb, extraSpace)
        buildObfusChar(activity, representation, sb, ObfusChar.SPECIAL_CHAR, R.string.special_char, R.string.special_char_explanation)
        return sb.toString()
    }


    private fun buildObfusChar(activity: Activity, representation: Representation,
                               sb: StringBuilder, obfusChar: ObfusChar, stringId: Int, explanationId: Int) {
        sb.append(obfusChar.getRepresentation(representation))
        sb.append(" = ")
        sb.append(activity.getString(explanationId))
        sb.append(" (")
        sb.append(activity.getString(stringId))
        sb.append(")")
        sb.append(Constants.NL)
    }

    private fun buildExtraSpace(sb: StringBuilder, extraSpace: Boolean) {
        if (extraSpace) {
            sb.append(Constants.NL)
        }
    }
}
