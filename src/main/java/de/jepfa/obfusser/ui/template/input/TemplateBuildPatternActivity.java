package de.jepfa.obfusser.ui.template.input;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.template.detail.TemplateDetailFragment;
import de.jepfa.obfusser.ui.template.input.TemplateInputHintsActivity;
import de.jepfa.obfusser.ui.template.input.TemplateInputPatternActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;


public class TemplateBuildPatternActivity extends SecureActivity {

    private TemplateViewModel templateViewModel;
    private String originPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_build_pattern);

        templateViewModel = TemplateViewModel.getFromIntent(this, getIntent());
        final Template template = templateViewModel.getTemplate().getValue();
        originPattern = template.getPatternInternal();
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putInt(TemplateDetailFragment.ARG_MODE,
                    TemplateDetailFragment.NEW_CREDENTIAL_BUILDER);

            TemplateDetailFragment fragment = new TemplateDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.template_detail_container_for_builder, fragment)
                    .commit();
        }

        if (template.isPersisted()) {
            setTitle("Build template");
        }

        Button lowerCaseButton = findViewById(R.id.button_pattern_lower_case);
        createObfusCharButton(template, lowerCaseButton, ObfusChar.LOWER_CASE_CHAR);

        Button upperCaseButton = findViewById(R.id.button_pattern_upper_case);
        createObfusCharButton(template, upperCaseButton, ObfusChar.UPPER_CASE_CHAR);

        Button digitButton = findViewById(R.id.button_pattern_digit);
        createObfusCharButton(template, digitButton, ObfusChar.DIGIT);

        Button specialCharButton = findViewById(R.id.button_pattern_special_char);
        createObfusCharButton(template, specialCharButton, ObfusChar.SPECIAL_CHAR);

        Button buttonBackspace = findViewById(R.id.button_pattern_backspace);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buttonBackspace.setText("<X");
        }

        buttonBackspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int length = template.getPatternLength();
                if (length > 0) {
                    byte[] secret = SecretChecker.getOrAskForSecret(TemplateBuildPatternActivity.this);
                    String pattern = template.getPatternAsExchangeFormatHinted(secret);
                    pattern = pattern.substring(0, length - 1);
                    template.setPatternFromUser(pattern, secret);
                    refreshPatternFragment();
                }
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
            template.setPatternInternal(originPattern);
            Intent intent = new Intent(this, TemplateInputPatternActivity.class);
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

        boolean cancel = templateViewModel.getTemplate().getValue().getPatternLength() < 4; //TODO global const for min pattern length
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {
            Template template = templateViewModel.getTemplate().getValue();

            Intent intent = new Intent(getBaseContext(), TemplateInputHintsActivity.class);
            IntentUtil.setTemplateExtra(intent, template);
            startActivity(intent);

        }
    }

    private void createObfusCharButton(final Template template, Button button, final ObfusChar obfusChar) {
        button.setText(obfusChar.toRepresentation());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] secret = SecretChecker.getOrAskForSecret(TemplateBuildPatternActivity.this);
                String pattern = template.getPatternAsExchangeFormatHinted(secret);
                pattern += obfusChar.toExchangeFormat();
                template.setPatternFromUser(pattern, secret);

                refreshPatternFragment();
            }
        });
    }

    private void refreshPatternFragment() {
        Bundle arguments = new Bundle();
        arguments.putInt(TemplateDetailFragment.ARG_MODE,
                TemplateDetailFragment.NEW_CREDENTIAL_BUILDER);

        TemplateDetailFragment fragment = new TemplateDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.template_detail_container_for_builder, fragment)
                .commit();
    }

}

