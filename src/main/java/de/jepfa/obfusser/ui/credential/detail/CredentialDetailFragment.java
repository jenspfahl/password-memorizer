package de.jepfa.obfusser.ui.credential.detail;

import android.os.Bundle;

import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.detail.PatternDetailFragment;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialDetailFragment extends PatternDetailFragment {

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

    @Override
    protected String getFinalPatternForDetails(SecurePatternHolder pattern, int counter) {
        String finalPatternString;
        if (counter % 3 == 0) {
            finalPatternString = pattern.getPatternRepresentationHinted(
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                    getSecureActivity().getPatternRepresentation(),
                    SecureActivity.SecretChecker.isEncWithUUIDEnabled(getSecureActivity()));
        }
        else if (counter % 3 == 1) {
            finalPatternString = pattern.getPatternRepresentationWithNumberedPlaceholder(
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                    getSecureActivity().getPatternRepresentation(),
                    SecureActivity.SecretChecker.isEncWithUUIDEnabled(getSecureActivity()));
        }
        else {
            finalPatternString = pattern.getPatternRepresentationRevealed(
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                    getSecureActivity().getPatternRepresentation(),
                    SecureActivity.SecretChecker.isEncWithUUIDEnabled(getSecureActivity()));
        }
        return finalPatternString;
    }

    @Override
    protected boolean showHints(int counter) {
        return counter % 3 == 1;
    }

}
