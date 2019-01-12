package de.jepfa.obfusser.ui.credential.input;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.PatternDetailFragment;
import de.jepfa.obfusser.ui.credential.detail.CredentialDetailFragment;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;

public class CredentialInputHintsActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_input_hints_text);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        Credential credential = credentialViewModel.getCredential().getValue();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putInt(CredentialDetailFragment.ARG_MODE,
                    CredentialDetailFragment.SELECT_HINTS);

            CredentialDetailFragment detailFragment = new CredentialDetailFragment();
            detailFragment.setArguments(arguments);

            final CredentialHintFragment hintsFragment = new CredentialHintFragment();
            hintsFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.credential_detail_container_for_input, detailFragment)
                    .add(R.id.credential_hints_list, hintsFragment)
                    .commit();

            detailFragment.setHintUpdateListener(new PatternDetailFragment.HintUpdateListener() {
                @Override
                public void onHintUpdated(int index) {
                    hintsFragment.refresh();
                }
            });
        }


        Button button = findViewById(R.id.create_credential_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CredentialHintFragment hintsFragment = (CredentialHintFragment) getSupportFragmentManager().findFragmentById(R.id.credential_hints_list);
                Credential credential = credentialViewModel.getCredential().getValue();

                boolean check = hintsFragment.checkMandatoryFields();

                for (String hint : credential.getHints().values()) {
                    if (hint == null || hint.isEmpty()) {
                        check = false;
                        break;
                    }
                }

                //TODO check mandatory fields in a better way

                if (check) {

                    credential.mergeHintsIntoPattern();
                    if (credential.isPersisted()) {
                        credentialViewModel.getRepo().update(credential);
                    }
                    else {
                        credentialViewModel.getRepo().insert(credential);
                    }

                    Intent upIntent = new Intent(getBaseContext(), NavigationActivity.class);
                    upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_credentials);
                    navigateUpTo(upIntent);
                }
                else {
                    Snackbar.make(view, "Please fill all hints.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        if (credential.isPersisted()) {
            setTitle(R.string.title_change_credential);
            button.setText(R.string.button_change_credential);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, CredentialInputPatternActivity.class);
            IntentUtil.setCredentialExtra(upIntent, credentialViewModel.getCredential().getValue());
            navigateUpTo(upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean before) {
        if (!before) {
            recreate();
        } //TODO
    }

}
