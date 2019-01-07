package de.jepfa.obfusser.ui.toolkit;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.EditText;

import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.model.Representation;

public class ObfusEditText {

    private EditText editText;
    private Representation representation;
    private String pattern;
    private volatile boolean inTransition;

    public ObfusEditText(final EditText editText, final Representation representation, final String initialPattern) {
        this.editText = editText;
        this.representation = representation;
        this.pattern = initialPattern;

        update();
        editText.setSelection(initialPattern.length());

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (inTransition) {
                    return null;
                }

                String beginning = pattern.substring(0, dstart);
                String ends = pattern.substring(dend);

                pattern = beginning + ObfusString.obfuscate(source.toString()).toExchangeFormat() + ends;

                String s = ObfusString.obfuscate(source.toString()).toRepresentation(representation);

                Log.d("Filter", source.toString() + " start="+start + " end=" + end +  "span=" + dest
                        + " dstart=" + dstart + "dend=" + dend + " ed/pattern=" + s + "/"+pattern);


                return s;
            }
        };

        editText.setFilters(new InputFilter[]{filter});
    }

    public void insert(ObfusChar obfusChar) {
        int selectionStart = editText.getSelectionStart();
        if (isSelectionValid(selectionStart)) {
            String beginning = pattern.substring(0, selectionStart);
            String end = pattern.substring(editText.getSelectionEnd());
            pattern = beginning + obfusChar.toExchangeFormat() + end;
        }
        else {
            pattern += obfusChar.toExchangeFormat();
        }

        inTransition = true;
        update();
        editText.setSelection(selectionStart + 1);
        inTransition = false;
    }

    public void backspace() {
        int selectionStart = editText.getSelectionStart();
        if (isSelectionValid(selectionStart)) {
            selectionStart = ensureNotNegative(selectionStart - 1);
            String beginning = pattern.substring(0, selectionStart);
            String end = pattern.substring(editText.getSelectionEnd());
            pattern = beginning + end;

            editText.setSelection(selectionStart);

            inTransition = true;
            update();
            inTransition = false;
        }
    }

    private void update() {
        int selectionStart = editText.getSelectionStart();
        editText.setText(ObfusString.fromExchangeFormat(pattern).toRepresentation(representation));
        editText.setSelection(selectionStart);

        Log.d(ObfusEditText.class.getSimpleName(), pattern + "(" + pattern.length() + ") / "
                + editText.getText().toString() + "(" + editText.getText().toString().length() + ") curSel="
                + editText.getSelectionStart() + "/" + editText.getSelectionEnd());

    }

    public String getPattern() {
        return pattern;
    }

    private int ensureNotNegative(int value) {
        return Math.max(0, value);
    }

    private boolean isSelectionValid(int selection) {
        return selection != -1 && selection <= pattern.length();
    }
}
