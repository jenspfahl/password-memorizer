package de.jepfa.obfusser.ui.credential.input;

import android.content.Intent;
import android.os.Build;
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

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.ObfusEditText;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialInputPatternActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;
    private ObfusEditText obfusEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_input_pattern);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();

        if (credential.isPersisted()) {
            setTitle("Change credential");
        }

        EditText editText = findViewById(R.id.credential_builder_editview);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptNextStep();
                    return true;
                }
                return false;
            }
        });

        byte[] secret = SecretChecker.getOrAskForSecret(this);
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
            buttonBackspace.setText("<X"); //TODO find better char
        }

        buttonBackspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                obfusEditText.backspace();
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

        String pattern = obfusEditText.getPattern();

        if (!isPatternValid(pattern)) {
            obfusEditText.getEditText().setError(getString(R.string.error_invalid_pattern));
        } else {
            Credential credential = credentialViewModel.getCredential().getValue();
            byte[] secret = SecretChecker.getOrAskForSecret(this);
            credential.setPatternFromUser(pattern, secret);

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

    private boolean isPatternValid(String pattern) {
        return pattern.length() >= Constants.MIN_PATTERN_LENGTH;
    }


}

