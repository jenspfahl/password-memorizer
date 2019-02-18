package de.jepfa.obfusser.ui.template.input;
import android.os.Bundle;

import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.common.input.PatternSelectHintsFragment;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;

public class TemplateSelectHintsFragment extends PatternSelectHintsFragment {

    private TemplateViewModel templateViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templateViewModel = TemplateViewModel.get(this.getActivity());
    }

    @Override
    protected SecurePatternHolder getPattern() {
        return templateViewModel.getTemplate().getValue();
    }

}
