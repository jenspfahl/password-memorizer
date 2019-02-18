package de.jepfa.obfusser.ui.credential.input;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.jepfa.obfusser.ui.common.Debug;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialInputNameActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;
    private EditText nameView;
    private EditText infoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_input_name);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        Credential credential = credentialViewModel.getCredential().getValue();

        View explanationView = findViewById(R.id.credential_explanation);
        explanationView.setLongClickable(true);
        explanationView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Debug.INSTANCE.toggleDebug();
                Toast.makeText(CredentialInputNameActivity.this, "Debug mode " + (Debug.INSTANCE.isDebug() ? "ON" : "OFF"), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        nameView = findViewById(R.id.credential_name);
        String name = credential.getName();
        if (name != null) {
            nameView.setText(name);
        }

        infoView = findViewById(R.id.credential_info);
        String info = credential.getInfo();
        if (info != null) {
            infoView.setText(info);
        }

        if (credential.isPersisted()) {
            setTitle(R.string.title_change_credential);
        }

        infoView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptNextStep();
                    return true;
                }
                return false;
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
            Intent upIntent = new Intent(this, NavigationActivity.class);
            upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_credentials);
            navigateUpTo(upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean before) {
    }

    private void attemptNextStep() {
        nameView.setError(null);

        String name = nameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Credential credential = credentialViewModel.getCredential().getValue();
            credential.setName(nameView.getText().toString());
            credential.setInfo(infoView.getText().toString());

            Intent intent = new Intent(getBaseContext(), CredentialInputPatternActivity.class);
            IntentUtil.setCredentialExtra(intent, credential);
            startActivity(intent);

        }
    }

}

