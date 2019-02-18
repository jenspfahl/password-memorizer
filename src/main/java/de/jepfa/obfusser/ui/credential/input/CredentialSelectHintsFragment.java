package de.jepfa.obfusser.ui.credential.input;

import android.os.Bundle;

import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.common.input.PatternSelectHintsFragment;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialSelectHintsFragment extends PatternSelectHintsFragment {

    private CredentialViewModel credentialViewModel;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentialViewModel = CredentialViewModel.get(this.getActivity());
    }

    @Override
    protected SecurePatternHolder getPattern() {
        return credentialViewModel.getCredential().getValue();
    }

}
