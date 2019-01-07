package de.jepfa.obfusser.ui.credential.input;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.credential.detail.CredentialDetailFragment;
import de.jepfa.obfusser.ui.toolkit.ObfusEditText;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialBuildPatternActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;
    private String originPattern;
    private ObfusEditText obfusEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_build_pattern);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();
        originPattern = credential.getPatternInternal();

        if (credential.isPersisted()) {
            setTitle("Build credential");
        }

        EditText editText = findViewById(R.id.credential_builder_editview);
        byte[] secret = SecretChecker.getOrAskForSecret(CredentialBuildPatternActivity.this);
        String pattern = credential.getPatternAsExchangeFormatHinted(secret);
        obfusEditText = new ObfusEditText(editText,
                getPatternRepresentation(), pattern);

        View selectTemplate = findViewById(R.id.link_to_template_selection);
        selectTemplate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getBaseContext(), SelectTemplateForCredentialActivity.class);
                IntentUtil.setCredentialExtra(intent, credential);
                startActivity(intent);
            }
        });


        Button lowerCaseButton = findViewById(R.id.button_pattern_lower_case);
        createObfusCharButton(lowerCaseButton, ObfusChar.LOWER_CASE_CHAR);

        Button upperCaseButton = findViewById(R.id.button_pattern_upper_case);
        createObfusCharButton(upperCaseButton, ObfusChar.UPPER_CASE_CHAR);

        Button digitButton = findViewById(R.id.button_pattern_digit);
        createObfusCharButton(digitButton, ObfusChar.DIGIT);

        Button specialCharButton = findViewById(R.id.button_pattern_special_char);
        createObfusCharButton(specialCharButton, ObfusChar.SPECIAL_CHAR);

        Button buttonBackspace = findViewById(R.id.button_pattern_backspace);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buttonBackspace.setText("<X");
        }

        buttonBackspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int length = credential.getPatternLength();
                if (length > 0) {
                    obfusEditText.backspace();
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
            byte[] secret = SecretChecker.getOrAskForSecret(CredentialBuildPatternActivity.this);
            credential.setPatternFromUser(obfusEditText.getPattern(), secret);

            Intent intent = new Intent(getBaseContext(), CredentialInputHintsActivity.class);
            IntentUtil.setCredentialExtra(intent, credential);
            startActivity(intent);

        }
    }

    private void createObfusCharButton(Button button, final ObfusChar obfusChar) {
        button.setText(obfusChar.toRepresentation(getPatternRepresentation()));
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                obfusEditText.insert(obfusChar);
            }
        });
    }

}

