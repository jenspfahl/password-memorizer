package de.jepfa.obfusser.ui.common

import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

import de.jepfa.obfusser.BuildConfig
import de.jepfa.obfusser.model.GroupColor
import de.jepfa.obfusser.model.Group


object GroupColorizer {

    val COLOR_INDICATION_FULL = '\u25A0' // ■
    val COLOR_INDICATION_EMPTY = '\u25A1' // □
    private val COLOR_INDICATION_LIST = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) '\u220e' else '\u25A0' // ∎ alt: ■

    fun getColorizedText(group: Group?, text: String): CharSequence {
        return if (group == null || group.color == 0) {
            text
        } else {
            getColorizedText(group.color, text)
        }
    }

    fun getColorizedText(colorInt: Int, text: String): SpannableString {
        return getColorizedSpan(colorInt, text, COLOR_INDICATION_LIST)
    }

    private fun getColorizedSpan(colorInt: Int, text: String, indicator: Char): SpannableString {
        val span = SpannableString(indicator + (if (text.isEmpty()) "" else " ") + text)
        val color = GroupColor.getAndroidColor(colorInt)
        span.setSpan(ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return span
    }
}
