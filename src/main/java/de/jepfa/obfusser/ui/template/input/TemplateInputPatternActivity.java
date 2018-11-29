package de.jepfa.obfusser.ui.template.input;

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
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;


public class TemplateInputPatternActivity extends SecureActivity {

    private TemplateViewModel templateViewModel;
    private EditText mPatternView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_input_pattern);

        templateViewModel = TemplateViewModel.getFromIntent(this, getIntent());
        Template template = templateViewModel.getTemplate().getValue();

        mPatternView = findViewById(R.id.template_pattern);
        String pattern = template.getPatternAsExchangeFormatHinted(SecretChecker.getOrAskForSecret(this));
        if (pattern != null) {
            mPatternView.setText(pattern);
        }

        if (template.isPersisted()) {
            setTitle("Change template");
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

        Button nextStepButton = findViewById(R.id.template_next_step);
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
            Template template = templateViewModel.getTemplate().getValue();
            Intent intent = new Intent(this, TemplateInputNameActivity.class);
            IntentUtil.setTemplateExtra(intent, template);
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
            Template template = templateViewModel.getTemplate().getValue();
            template.setPatternFromUser(mPatternView.getText().toString(), SecretChecker.getOrAskForSecret(this));

            Intent intent = new Intent(getBaseContext(), TemplateInputHintsActivity.class);
            IntentUtil.setTemplateExtra(intent, template);
            startActivity(intent);

        }
    }

    private boolean isPatternValid(String pattern) {
        return pattern.length() >= 4;
    }


}

