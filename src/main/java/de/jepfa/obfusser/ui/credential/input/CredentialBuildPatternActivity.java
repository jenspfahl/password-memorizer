package de.jepfa.obfusser.ui.credential.input;

import android.arch.core.util.Function;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.credential.detail.CredentialDetailFragment;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialBuildPatternActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;
    private String originPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_build_pattern);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();
        originPattern = credential.getPatternInternal();
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putInt(CredentialDetailFragment.ARG_MODE,
                    CredentialDetailFragment.NEW_CREDENTIAL_BUILDER);

            CredentialDetailFragment fragment = new CredentialDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.credential_detail_container_for_builder, fragment)
                    .commit();
        }

        if (credential.isPersisted()) {
            setTitle("Build credential");
        }

        Button lowerCaseButton = findViewById(R.id.button_pattern_lower_case);
        createObfusCharButton(credential, lowerCaseButton, ObfusChar.LOWER_CASE_CHAR);

        Button upperCaseButton = findViewById(R.id.button_pattern_upper_case);
        createObfusCharButton(credential, upperCaseButton, ObfusChar.UPPER_CASE_CHAR);

        Button digitButton = findViewById(R.id.button_pattern_digit);
        createObfusCharButton(credential, digitButton, ObfusChar.DIGIT);

        Button specialCharButton = findViewById(R.id.button_pattern_special_char);
        createObfusCharButton(credential, specialCharButton, ObfusChar.SPECIAL_CHAR);

        Button buttonBackspace = findViewById(R.id.button_pattern_backspace);
        buttonBackspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int length = credential.getPatternLength();
                if (length > 0) {
                    byte[] secret = SecretChecker.getOrAskForSecret(CredentialBuildPatternActivity.this);
                    String pattern = credential.getPatternAsExchangeFormatHinted(secret);
                    pattern = pattern.substring(0, length - 1);
                    credential.setPatternFromUser(pattern, secret);
                    refreshPatternFragment();
                }
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
            credential.setPatternInternal(originPattern);
            Intent intent = new Intent(this, CredentialInputPatternActivity.class);
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

        boolean cancel = credentialViewModel.getCredential().getValue().getPatternLength() < 4; //TODO global const for min pattern length
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {
            Credential credential = credentialViewModel.getCredential().getValue();

            Intent intent = new Intent(getBaseContext(), CredentialInputHintsActivity.class);
            IntentUtil.setCredentialExtra(intent, credential);
            startActivity(intent);

        }
    }

    private void createObfusCharButton(final Credential credential, Button button, final ObfusChar obfusChar) {
        button.setText(obfusChar.toRepresentation());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] secret = SecretChecker.getOrAskForSecret(CredentialBuildPatternActivity.this);
                String pattern = credential.getPatternAsExchangeFormatHinted(secret);
                pattern += obfusChar.toExchangeFormat();
                credential.setPatternFromUser(pattern, secret);

                refreshPatternFragment();
            }
        });
    }

    private void refreshPatternFragment() {
        Bundle arguments = new Bundle();
        arguments.putInt(CredentialDetailFragment.ARG_MODE,
                CredentialDetailFragment.NEW_CREDENTIAL_BUILDER);

        CredentialDetailFragment fragment = new CredentialDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.credential_detail_container_for_builder, fragment)
                .commit();
    }

}

