package de.jepfa.obfusser.ui.credential.input;

import android.content.Intent;
import android.support.v7.app.ActionBar;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialInputPatternActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;
    private EditText mPatternView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_input_pattern);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();

        mPatternView = findViewById(R.id.credential_pattern);
        String pattern = credential.getPatternAsExchangeFormatHinted(SecretChecker.getOrAskForSecret(this));
        if (pattern != null) {
            mPatternView.setText(pattern);
        }

        if (credential.isPersisted()) {
            setTitle("Change credential");
        }

        mPatternView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptNextStep();
                    return true;
                }
                return false;
            }
        });

        View selectTemplate = findViewById(R.id.link_to_template_selection);
        selectTemplate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getBaseContext(), SelectTemplateForCredentialActivity.class);
                IntentUtil.setCredentialExtra(intent, credential);
                startActivity(intent);
            }
        });

        View buildPattern = findViewById(R.id.link_to_pattern_builder);
        buildPattern.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getBaseContext(), CredentialBuildPatternActivity.class);
                IntentUtil.setCredentialExtra(intent, credential);
                startActivity(intent);
            }
        });


        Button nextStepButton = findViewById(R.id.credential_next_step);
        nextStepButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptNextStep();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Credential credential = credentialViewModel.getCredential().getValue();
            Intent intent = new Intent(this, CredentialInputNameActivity.class);
            IntentUtil.setCredentialExtra(intent, credential);
            navigateUpTo(intent);
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

    private void attemptNextStep() {

        mPatternView.setError(null);

        String pattern = mPatternView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isPatternValid(pattern)) {
            mPatternView.setError(getString(R.string.error_invalid_pattern));
            focusView = mPatternView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Credential credential = credentialViewModel.getCredential().getValue();
            credential.setPatternFromUser(
                    mPatternView.getText().toString(),
                    SecretChecker.getOrAskForSecret(this));

            Intent intent = new Intent(getBaseContext(), CredentialInputHintsActivity.class);
            IntentUtil.setCredentialExtra(intent, credential);
            startActivity(intent);

        }
    }

    private boolean isPatternValid(String pattern) {
        return pattern.length() >= 4;
    }


}

