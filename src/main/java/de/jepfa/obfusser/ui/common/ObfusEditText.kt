package de.jepfa.obfusser.ui.common

import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.widget.EditText

import de.jepfa.obfusser.model.ObfusChar
import de.jepfa.obfusser.model.ObfusString
import de.jepfa.obfusser.model.Representation

class ObfusEditText(val editText: EditText, private val representation: Representation,
                    initialPattern: String, isRecreation: Boolean) {
    private var recreation: Boolean = false


    val pattern: String
        get() = ObfusString.fromRepresentation(editText.text.toString(), representation, null).toExchangeFormat()

    private val text: String
        get() = editText.text.toString()

    init {
        this.recreation = isRecreation

        editText.setText(ObfusString.fromExchangeFormat(initialPattern, null).toRepresentation(representation))
        editText.setSelection(initialPattern.length)

        ObfusTextAdjuster.adjustTextForRepresentation(representation, editText)

        val filter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {

                val v = CharArray(end - start)
                TextUtils.getChars(source, start, end, v, 0)
                val currString = String(v)
                val s: String
                if (recreation) {
                    // during recreation no obfuscating to avoid double obfuscating
                    s = currString
                    recreation = false
                } else {
                    s = ObfusString.obfuscate(currString).toRepresentation(representation)
                }

                if (source is Spanned) {
                    val sp = SpannableString(s)
                    TextUtils.copySpansFrom(source,
                            start, end, null, sp, 0)
                    return@InputFilter sp
                } else {
                    return@InputFilter s
                }
            }
            null
        }

        editText.filters = arrayOf(filter)
    }

    fun insert(obfusChar: ObfusChar) {
        val selectionStart = editText.selectionStart
        if (isSelectionValid(selectionStart)) {
            editText.text.replace(selectionStart, editText.selectionEnd, obfusChar.toExchangeFormat())
        } else {
            editText.text.append(obfusChar.toExchangeFormat())
        }
    }

    fun backspace() {
        var selectionStart = editText.selectionStart
        if (isSelectionValid(selectionStart)) {
            selectionStart = ensureNotNegative(selectionStart - 1)
            editText.text.delete(selectionStart, editText.selectionEnd)
        }
    }

    private fun ensureNotNegative(value: Int): Int {
        return Math.max(0, value)
    }

    private fun isSelectionValid(selection: Int): Boolean {
        return selection != -1 && selection <= text.length
    }
}
