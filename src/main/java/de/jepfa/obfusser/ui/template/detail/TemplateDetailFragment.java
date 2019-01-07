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
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.common.PatternDetailFragment;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;

public class TemplateDetailFragment extends PatternDetailFragment {

    private TemplateViewModel templateViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templateViewModel = TemplateViewModel.get(this.getActivity());
    }


    @Override
    protected int getFragmentDetailId() {
        return R.layout.template_detail;
    }

    @Override
    protected int getTextViewId() {
        return R.id.template_detail_textview;
    }

    @Override
    protected SecurePatternHolder getPattern() {
        return templateViewModel.getTemplate().getValue();
    }

    @Override
    protected String getFinalPatternForDetails(SecurePatternHolder pattern, int counter) {
        String finalPatternString;
        if (counter % 2 == 0) {
            finalPatternString = buildPatternString(pattern,
                    pattern.getPatternRepresentationWithNumberedPlaceholder(
                            SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                            getSecureActivity().getPatternRepresentation()),
                    false);
        }
        else {
            finalPatternString = buildPatternString(pattern,
                    pattern.getPatternRepresentationWithNumberedPlaceholder(
                            SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                            getSecureActivity().getPatternRepresentation()),
                    true);
        }
        return finalPatternString;
    }

    @Override
    protected String getPatternRepresentationForDetails(SecurePatternHolder pattern) {
        return pattern.getPatternRepresentationWithNumberedPlaceholder(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
    }

}
