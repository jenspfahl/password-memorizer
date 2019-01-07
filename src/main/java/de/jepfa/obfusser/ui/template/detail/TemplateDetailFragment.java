package de.jepfa.obfusser.ui.template.detail;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
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

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;

public class TemplateDetailFragment extends SecureFragment {

    public static final String ARG_MODE = "mode";
    public static final int SHOW_CREDENTIAL_DETAIL = 1;
    public static final int NEW_CREDENTIAL_SELECT_HINTS = 2;
    public static final int NEW_CREDENTIAL_INPUT_HINTS = 3;

    private int mode;
    private TemplateViewModel templateViewModel;


    public TemplateDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templateViewModel = TemplateViewModel.get(this.getActivity());
        mode = getArguments().getInt(ARG_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.template_detail, container, false);

        if (templateViewModel.getTemplate() != null) {
            final Template template = templateViewModel.getTemplate().getValue();
            final TextView obfusTextView = rootView.findViewById(R.id.template_detail_textview);

            switch (mode) {
                case NEW_CREDENTIAL_SELECT_HINTS :
                    onCreateForNewTemplateSelectHints(template, obfusTextView);
                    break;
                case NEW_CREDENTIAL_INPUT_HINTS :
                    onCreateForNewTemplateInputHints(template, obfusTextView);
                    break;
                case SHOW_CREDENTIAL_DETAIL :
                    onCreateForShowTemplateDetails(template, obfusTextView);
                    break;
            }

            //TODO fitSizeToScreen(obfusTextView);
        }

        return rootView;
    }

    private void onCreateForShowTemplateDetails(final Template template, final TextView obfusTextView) {
        String patternString = buildPatternString(template,
                template.getPatternRepresentationWithNumberedPlaceholder(
                        SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                        getSecureActivity().getPatternRepresentation()),
                false);
        SpannableString span = getSpannableString(template, patternString);

        obfusTextView.setText(span, TextView.BufferType.NORMAL);

        final AtomicInteger clickCounter = new AtomicInteger(1);
        obfusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int counter = clickCounter.getAndIncrement();

                String finalPatternString;
                if (counter % 2 == 0) {
                    finalPatternString = buildPatternString(template,
                            template.getPatternRepresentationWithNumberedPlaceholder(
                                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                                    getSecureActivity().getPatternRepresentation()),
                            false);
                }
                else {
                    finalPatternString = buildPatternString(template,
                            template.getPatternRepresentationWithNumberedPlaceholder(
                                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                                    getSecureActivity().getPatternRepresentation()),
                            true);
                }

                SpannableString span = getSpannableString(template, finalPatternString);
                obfusTextView.setText(span, TextView.BufferType.NORMAL);

            }

        });
    }

    private void onCreateForNewTemplateInputHints(Template template, TextView obfusTextView) {
        String patternString = template.getPatternRepresentationWithNumberedPlaceholder(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
        SpannableString span = getSpannableString(template, patternString);

        obfusTextView.setText(span, TextView.BufferType.NORMAL);
    }

    private void onCreateForNewTemplateSelectHints(final Template template, final TextView obfusTextView) {
        String patternString = template.getPatternRepresentationRevealed(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
        final SpannableString span = new SpannableString(patternString);

        for (int i = 0; i < patternString.length(); i++) {
            int j = i + 1;

            final boolean fenabled;
            String hint = template.getHint(i, SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()));
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
                        template.addPotentialHint(fi);
                        color = getResources().getColor(R.color.colorAccent);
                    }
                    else {
                        template.removePotentialHint(fi);
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
    private String buildPatternString(Template template, String patternString, boolean withHints) {
        StringBuilder sb = new StringBuilder(patternString);
        if (withHints && template.getHints() != null && !template.getHints().isEmpty()) {
            //TODO move this in anotherUI component, so pattern can be fit automatically to the screen size w/o hints
            sb.append(System.lineSeparator());
            int counter = 0;
            for (String hint : template.getHints(SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity())).values()) {
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
    private SpannableString getSpannableString(Template template, String patternString) {
        final SpannableString span = new SpannableString(patternString);

        int size = template.getPatternLength();
        for (int i = 0; i < size; i++) {
            int j = i + 1;
            String hint = template.getHint(i, SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()));
            if (hint != null) {
                span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), i, j, Spanned.SPAN_MARK_MARK);
            }
            else {
                span.setSpan(new ForegroundColorSpan(Color.BLACK), i, j, Spanned.SPAN_MARK_MARK);
            }

        }

        for (int i = size; i < patternString.length(); i++) {
            int j = i + 1;
            span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), i, j, Spanned.SPAN_MARK_MARK);

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
