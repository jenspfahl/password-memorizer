package de.jepfa.obfusser.ui.common;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.PatternHolder;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;

public abstract class PatternDetailFragment extends SecureFragment {

    public static final String ARG_MODE = "mode";
    public static final int SHOW_DETAIL = 1;
    public static final int SELECT_HINTS = 2;
    public static final int INPUT_HINTS = 3;

    protected int mode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(ARG_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(getFragmentDetailId(), container, false);
        SecurePatternHolder pattern = getPattern();

        if (pattern != null) {
            final TextView obfusTextView = rootView.findViewById(getTextViewId());

            switch (mode) {
                case SELECT_HINTS:
                    onCreateForNewPatternSelectHints(pattern, obfusTextView);
                    break;
                case INPUT_HINTS:
                    onCreateForNewPatternInputHints(pattern, obfusTextView);
                    break;
                case SHOW_DETAIL:
                    onCreateForShowPatternDetails(pattern, obfusTextView);
                    break;
            }

            //TODO fitSizeToScreen(obfusTextView);
        }

        return rootView;
    }

    protected abstract int getFragmentDetailId();

    protected abstract int getTextViewId();

    protected abstract SecurePatternHolder getPattern();

    protected abstract String getFinalPatternForDetails(SecurePatternHolder pattern, int counter);

    protected abstract String getPatternRepresentationForDetails(SecurePatternHolder pattern);

    private void onCreateForShowPatternDetails(final SecurePatternHolder pattern, final TextView obfusTextView) {
        String patternString = buildPatternString(
                pattern,
                getPatternRepresentationForDetails(pattern),
                false);
        SpannableString span = getSpannableString(pattern, patternString, pattern.getInfo());

        obfusTextView.setText(span, TextView.BufferType.NORMAL);

        final AtomicInteger clickCounter = new AtomicInteger(1);
        obfusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int counter = clickCounter.getAndIncrement();

                String finalPatternString = getFinalPatternForDetails(pattern, counter);

                SpannableString span = getSpannableString(pattern, finalPatternString, pattern.getInfo());
                obfusTextView.setText(span, TextView.BufferType.NORMAL);

            }

        });
    }


    private void onCreateForNewPatternInputHints(SecurePatternHolder pattern, TextView obfusTextView) {
        String patternString = pattern.getPatternRepresentationWithNumberedPlaceholder(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
        SpannableString span = getSpannableString(pattern, patternString, null);

        obfusTextView.setText(span, TextView.BufferType.NORMAL);
    }

    private void onCreateForNewPatternSelectHints(final SecurePatternHolder pattern, final TextView obfusTextView) {
        String patternString = pattern.getPatternRepresentationRevealed(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
        final SpannableString span = new SpannableString( patternString);

        for (int i = 0; i < patternString.length(); i++) {
            int j = i + 1;

            final boolean fenabled;
            String hint = pattern.getHint(i, SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()));
            if (hint != null) {
                fenabled = true;
            }
            else {
                fenabled = false;
             }

            final int fi = i;
            final int fj = j;
            ClickableSpan clickSpan = new ClickableSpan() {

                private boolean enabled = fenabled;

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {

                    int color;
                    if (enabled) {
                        color = getResources().getColor(R.color.colorAccent);
                    }
                    else {
                        color = Color.BLACK;
                    }

                    ds.setColor(color);
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(View yourTextView) {

                    enabled = !enabled;
                    int color;
                    if (enabled) {
                        pattern.addPotentialHint(fi);
                        color = getResources().getColor(R.color.colorAccent);
                    }
                    else {
                        pattern.removePotentialHint(fi);
                        color = Color.BLACK;
                    }

                    span.setSpan(new ForegroundColorSpan(color), fi, fj, 0);
                    obfusTextView.setText(span, TextView.BufferType.SPANNABLE);
                    obfusTextView.setMovementMethod(LinkMovementMethod.getInstance());

                }
            };
            span.setSpan(clickSpan, i, j, 0);

        }

        obfusTextView.setText(span);
        obfusTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @NonNull
    protected String buildPatternString(SecurePatternHolder pattern, String patternString, boolean withHints) {
        StringBuilder sb = new StringBuilder(patternString);
        if (withHints && pattern.getHints() != null && !pattern.getHints().isEmpty()) {
            //TODO move this in anotherUI component, so pattern can be fit automatically to the screen size w/o hints
            sb.append(System.lineSeparator());
            int counter = 0;
            for (String hint : pattern.getHints(SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity())).values()) {
                counter++;
                sb.append(System.lineSeparator());
                sb.append(NumberedPlaceholder.fromPlaceholderNumber(counter).toRepresentation());
                sb.append("=");
                if (hint == null || hint.isEmpty()) {
                    hint = "t.b.d.";
                }
                sb.append(hint);
            }
        }
        return sb.toString();
    }


    @NonNull
    private SpannableString getSpannableString(SecurePatternHolder pattern, String patternString, String prefixText) {
        SpannableString span;
        int start = 0;
        if (TextUtils.isEmpty(prefixText)) {
            span = new SpannableString(patternString);
        }
        else {
            span = new SpannableString(prefixText + Constants.NL + patternString);
            start = prefixText.length() + 1;
        }

        for (int i = 0; i < pattern.getPatternLength(); i++) {
            int j = i + 1;
            String hint = pattern.getHint(i, SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()));
            if (hint != null) {
                span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start + i, start + j, Spanned.SPAN_MARK_MARK);
            }

        }

        for (int i = pattern.getPatternLength(); i < patternString.length(); i++) {
            int j = i + 1;
            span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), start + i, start + j, Spanned.SPAN_MARK_MARK);

        }
        return span;
    }

    private void fitSizeToScreen(TextView textView) {
        Display display = getActivity().getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x - 10;

        Paint paint = new Paint();
        Rect bounds = new Rect();

        paint.setTypeface(textView.getTypeface());
        float textSize = 1000;//textView.getTextSize();
        paint.setTextSize(textSize);
        String text = textView.getText().toString();
        paint.getTextBounds(text, 0, text.length(), bounds);

        while (bounds.width() > displayWidth) {
            textSize--;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }
//TODO
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(textSize, displayWidth));
       // textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
    }

    @Override
    public void refresh() {
        getActivity().recreate(); //TODO
    }

}
