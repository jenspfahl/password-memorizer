package de.jepfa.obfusser.ui.credential.input;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.util.DataSorter;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;
import de.jepfa.obfusser.viewmodel.template.TemplateListViewModel;

public class SelectTemplateForCredentialActivity extends SecureActivity {

    private TemplateListViewModel templateListViewModel;
    private CredentialViewModel credentialViewModel;
    private RadioGroup radioGroup;
    private Credential credential;
    private Template selectedTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_template_for_credential);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        credential = credentialViewModel.getCredential().getValue();
        if (credential.isPersisted()) {
            setTitle(R.string.title_change_credential);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        templateListViewModel = ViewModelProviders
                .of(this)
                .get(TemplateListViewModel.class);

        radioGroup = findViewById(R.id.select_template);
        createRadioButtons();


        final Button nextStepButton = findViewById(R.id.credential_next_step);
        nextStepButton.setEnabled(false);
        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Credential credential = credentialViewModel.getCredential().getValue();
                if (selectedTemplate != null) {
                    credential.copyFrom(selectedTemplate,
                            SecureActivity.SecretChecker.getOrAskForSecret(SelectTemplateForCredentialActivity.this),
                            SecureActivity.SecretChecker.isEncWithUUIDEnabled(SelectTemplateForCredentialActivity.this));
                }
                Intent intent = new Intent(getBaseContext(), CredentialInputHintsActivity.class);
                IntentUtil.INSTANCE.setCredentialExtra(intent, credential);

                startActivity(intent);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == Constants.NO_ID) {
                    selectedTemplate = null;
                }
                else {
                    //copy from template
                    templateListViewModel
                            .getRepo()
                            .getTemplateById(checkedId)
                            .observe(SelectTemplateForCredentialActivity.this, new Observer<Template>() {

                                @Override
                                public void onChanged(@Nullable Template template) {
                                    selectedTemplate = template;
                                    nextStepButton.setEnabled(selectedTemplate != null);
                                }
                            });
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Credential credential = credentialViewModel.getCredential().getValue();

            Intent intent = new Intent(this, CredentialInputPatternActivity.class);
            IntentUtil.INSTANCE.setCredentialExtra(intent, credential);
            navigateUpTo(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean before) {
        radioGroup.removeAllViews();
        createRadioButtons();
    }


    private void createRadioButtons() {
        templateListViewModel
                .getRepo()
                .getAllTemplates()
                .observe(this, new Observer<List<Template>>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onChanged(@Nullable final List<Template> templates) {
                        DataSorter.INSTANCE.sortPatternsByName(templates);
                        for (Template template : templates) {
                            RadioButton groupRadioButton = new RadioButton(SelectTemplateForCredentialActivity.this);
                            groupRadioButton.setId(template.getId());
                            String pattern = template.getPatternRepresentationWithNumberedPlaceholder(
                                    SecretChecker.getOrAskForSecret(SelectTemplateForCredentialActivity.this),
                                    getPatternRepresentation(),
                                    SecureActivity.SecretChecker.isEncWithUUIDEnabled(SelectTemplateForCredentialActivity.this));
                            groupRadioButton.setText(template.getName() + " — " + pattern);
                            radioGroup.addView(groupRadioButton);
                        }
                    }
                });
    }
}
