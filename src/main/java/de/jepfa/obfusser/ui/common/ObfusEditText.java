package de.jepfa.obfusser.ui.common;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.model.Representation;

public class ObfusEditText {

    private EditText editText;
    private Representation representation;
    private boolean recreation;

    public ObfusEditText(final EditText editText, final Representation representation,
                         final String initialPattern, boolean isRecreation) {
        this.editText = editText;
        this.representation = representation;
        this.recreation = isRecreation;

        editText.setText(ObfusString.fromExchangeFormat(initialPattern).toRepresentation(representation));
        editText.setSelection(initialPattern.length());

        ObfusTextAdjuster.adjustTextForRepresentation(representation, editText);

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                for (int i = start; i < end; i++) {

                    char[] v = new char[end - start];
                    TextUtils.getChars(source, start, end, v, 0);
                    String currString = new String(v);
                    String s;
                    if (recreation) {
                        // during recreation no obfuscating to avoid double obfuscating
                        s = currString;
                        recreation = false;
                    }
                    else {
                        s = ObfusString.obfuscate(currString).toRepresentation(representation);
                    }

                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(s);
                        TextUtils.copySpansFrom((Spanned) source,
                                start, end, null, sp, 0);
                        return sp;
                    } else {
                        return s;
                    }
                }
                return null;
            }
        };

        editText.setFilters(new InputFilter[]{filter});
    }

    public void insert(ObfusChar obfusChar) {
        int selectionStart = editText.getSelectionStart();
        if (isSelectionValid(selectionStart)) {
            editText.getText().replace(selectionStart, editText.getSelectionEnd(), obfusChar.toExchangeFormat());
        }
        else {
            editText.getText().append(obfusChar.toExchangeFormat());
        }
    }

    public void backspace() {
        int selectionStart = editText.getSelectionStart();
        if (isSelectionValid(selectionStart)) {
            selectionStart = ensureNotNegative(selectionStart - 1);
            editText.getText().delete(selectionStart, editText.getSelectionEnd());
        }
    }


    public String getPattern() {
        return ObfusString.fromRepresentation(editText.getText().toString(), representation).toExchangeFormat();
    }

    public EditText getEditText() {
        return editText;
    }

    private int ensureNotNegative(int value) {
        return Math.max(0, value);
    }

    private boolean isSelectionValid(int selection) {
        return selection != -1 && selection <= getText().length();
    }

    private String getText() {
        return editText.getText().toString();
    }
}
