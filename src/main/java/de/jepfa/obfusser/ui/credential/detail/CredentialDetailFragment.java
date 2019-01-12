package de.jepfa.obfusser.ui.credential.detail;

import android.os.Bundle;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.PatternDetailFragment;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialDetailFragment extends PatternDetailFragment {

    private CredentialViewModel credentialViewModel;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentialViewModel = CredentialViewModel.get(this.getActivity());
    }

    @Override
    protected int getFragmentDetailId() {
        return R.layout.credential_detail;
    }

    @Override
    protected int getTextViewId() {
        return R.id.credential_detail_textview;
    }

    @Override
    protected SecurePatternHolder getPattern() {
        return credentialViewModel.getCredential().getValue();
    }

    @Override
    protected String getFinalPatternForDetails(SecurePatternHolder pattern, int counter) {
        String finalPatternString;
        if (counter % 3 == 0) {
            finalPatternString = buildPatternString(pattern, pattern.getPatternRepresentationHinted(
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                    getSecureActivity().getPatternRepresentation()), false);
        }
        else if (counter % 3 == 1) {
            finalPatternString = buildPatternString(pattern, pattern.getPatternRepresentationWithNumberedPlaceholder(
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                    getSecureActivity().getPatternRepresentation()), true);
        }
        else {
            finalPatternString = buildPatternString(pattern, pattern.getPatternRepresentationRevealed(
                    SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                    getSecureActivity().getPatternRepresentation()), false);
        }
        return finalPatternString;
    }

    @Override
    protected String getPatternRepresentationForDetails(SecurePatternHolder pattern) {
        return pattern.getPatternRepresentationHinted(
                SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity()),
                getSecureActivity().getPatternRepresentation());
    }


}
