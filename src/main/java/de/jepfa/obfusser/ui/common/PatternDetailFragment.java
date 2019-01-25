package de.jepfa.obfusser.ui.common;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;

public abstract class PatternDetailFragment extends SecureFragment {

    private TextView hintsTextView;
    private TextView infoTextView;

    public interface HintUpdateListener {
        void onHintUpdated(int index);
    }

    public static final String ARG_MODE = "mode";
    public static final int SHOW_DETAIL = 1;
    public static final int SELECT_HINTS = 2;

    protected int mode;

    private HintUpdateListener hintUpdateListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(ARG_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pattern_detail, container, false);
        SecurePatternHolder pattern = getPattern();

        if (pattern != null) {
            final TextView obfusTextView = rootView.findViewById(R.id.pattern_detail_obfuschar);
            ObfusTextAdjuster.adjustTextForRepresentation(getSecureActivity().getPatternRepresentation(), obfusTextView);

            infoTextView = rootView.findViewById(R.id.pattern_info_textview);
            hintsTextView = rootView.findViewById(R.id.pattern_hints_textview);


            switch (mode) {
                case SELECT_HINTS:
                    onCreateForNewPatternSelectHints(pattern, obfusTextView);
                    break;
                case SHOW_DETAIL:
                    onCreateForShowPatternDetails(pattern, obfusTextView);
                    break;
            }
        }

        return rootView;
    }

    protected abstract SecurePatternHolder getPattern();

    protected abstract String getFinalPatternForDetails(SecurePatternHolder pattern, int counter);

    protected abstract boolean showHints(int counter);

    protected abstract String getPatternRepresentationForDetails(SecurePatternHolder pattern);

    public void setHintUpdateListener(HintUpdateListener hintUpdateListener) {
        this.hintUpdateListener = hintUpdateListener;
    }

    private void onCreateForShowPatternDetails(final SecurePatternHolder pattern, final TextView obfusTextView) {
        if (pattern.getInfo() != null) {
            infoTextView.setText(pattern.getInfo());
        }

        String patternString = getPatternRepresentationForDetails(pattern);
        SpannableString span = getSpannableString(pattern, patternString);

        obfusTextView.setText(span, TextView.BufferType.NORMAL);
        ObfusTextAdjuster.fitSizeToScreen(getActivity(), obfusTextView, ObfusTextAdjuster.DEFAULT_MARGIN);

        final AtomicInteger clickCounter = new AtomicInteger(1);
        obfusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int counter = clickCounter.getAndIncrement();

                String finalPatternString = getFinalPatternForDetails(pattern, counter);
                if (showHints(counter)) {
                    hintsTextView.setText(buildHintsString(pattern));
                }
                else {
                    hintsTextView.setText(null);
                }

                SpannableString span = getSpannableString(pattern, finalPatternString);
                obfusTextView.setText(span, TextView.BufferType.NORMAL);
            }

        });

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float size = obfusTextView.getTextSize() * detector.getScaleFactor();
                obfusTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                return true;
            }
        });

        final GestureDetector longPressGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent event) {
                String message = pattern.toString();
                byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
                if (secret != null) {
                    message = message + Constants.NL
                            + "uuidkey="
                            + Debug.endOfArrayToString(
                                    pattern.getUUIDKey(secret, SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity())), 4);
                }
                new AlertDialog.Builder(getActivity())
                            .setTitle("Debug pattern")
                            .setMessage(message)
                            .show();
            }
        });


        obfusTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getPointerCount() == 2) {
                    return scaleGestureDetector.onTouchEvent(motionEvent);
                }
                else {
                    if (Debug.isDebug()) {
                        return longPressGestureDetector.onTouchEvent(motionEvent);
                    }
                    return false;
                }
            }
        });
    }

    private void onCreateForNewPatternSelectHints(final SecurePatternHolder pattern, final TextView obfusTextView) {
        infoTextView.setVisibility(TextView.GONE);
        hintsTextView.setVisibility(TextView.GONE);

        byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
        String patternString = pattern.getPatternRepresentationWithNumberedPlaceholder(
                secret,
                getSecureActivity().getPatternRepresentation(),
                SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity()));
        final SpannableStringBuilder span = new SpannableStringBuilder(patternString);

        for (int i = 0; i < patternString.length(); i++) {

            final boolean fenabled = pattern.hasHint(i, secret, SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity()));

            final int fi = i;
            ClickableSpan clickSpan = new ClickableSpan() {

                private boolean enabled = fenabled;
                private int index = fi;

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
                    ds.setUnderlineText(false); // overwrite super underline setting
                }

                @Override
                public void onClick(View yourTextView) {

                    if (!enabled && pattern.getHintsCount() == NumberedPlaceholder.count()) {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.title_set_revealed)
                                .setMessage(R.string.message_set_revealed)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                        return;
                    }

                    final byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
                    final boolean withUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity());

                    enabled = !enabled;
                    if (enabled) {
                        pattern.addHint(index, secret, SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity()));

                        if (hintUpdateListener != null) {
                            hintUpdateListener.onHintUpdated(index);
                        }
                    }
                    else {

                        if (pattern.isFilledHint(index, secret, withUuid)) {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.title_delete_revealed_character)
                                    .setMessage(getString(R.string.message_delete_revealed_character,
                                            pattern.getNumberedPlaceholder(index, secret, withUuid).toRepresentation()))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            pattern.removeHint(index, secret, withUuid);

                                            if (hintUpdateListener != null) {
                                                hintUpdateListener.onHintUpdated(index);
                                            }
                                            onCreateForNewPatternSelectHints(pattern, obfusTextView);

                                        }})
                                    .setNegativeButton(android.R.string.no, null)
                                    .show();
                        }
                        else {
                            pattern.removeHint(index, secret, withUuid);

                            if (hintUpdateListener != null) {
                                hintUpdateListener.onHintUpdated(index);
                            }
                        }

                    }

                    onCreateForNewPatternSelectHints(pattern, obfusTextView);
                }
            };
            span.setSpan(clickSpan, i, i + 1, 0);

        }

        obfusTextView.setText(span);
        obfusTextView.setMovementMethod(LinkMovementMethod.getInstance());

        ObfusTextAdjuster.adjustTextForRepresentation(getSecureActivity().getPatternRepresentation(), obfusTextView);
        ObfusTextAdjuster.fitSizeToScreen(getActivity(), obfusTextView, ObfusTextAdjuster.DEFAULT_MARGIN);
    }

    protected String buildHintsString(SecurePatternHolder pattern) {
        StringBuilder sb = new StringBuilder();
        byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
        final boolean withUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity());
        if (pattern.getHintsCount() > 0) {
            int counter = 0;
            for (String hint : pattern.getHints(secret, withUuid).values()) {
                counter++;
                sb.append(System.lineSeparator());
                sb.append(NumberedPlaceholder.fromPlaceholderNumber(counter).toRepresentation());
                sb.append("=");
                if (hint == null || hint.isEmpty()) {
                    hint = getString(R.string.hint_tbd);
                }
                sb.append(hint);
            }
            return sb.toString();
        }
        else {
            return getString(R.string.nothing_revealed);
        }

    }


    @NonNull
    private SpannableString getSpannableString(SecurePatternHolder pattern, String patternString) {
        SpannableString span = new SpannableString(patternString);;
        byte[] secret = SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());
        boolean withUuid = SecureActivity.SecretChecker.isEncWithUUIDEnabled(getActivity());

        for (int i = 0; i < pattern.getPatternLength(); i++) {
            int j = i + 1;
            String hint = pattern.getHint(i, secret, withUuid);
            if (hint != null) {
                span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), i, j, Spanned.SPAN_MARK_MARK);
            }

        }

        for (int i = pattern.getPatternLength(); i < patternString.length(); i++) {
            int j = i + 1;
            span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), i, j, Spanned.SPAN_MARK_MARK);

        }
        return span;
    }


    @Override
    public void refresh() {
        getActivity().recreate(); //TODO
    }




}
