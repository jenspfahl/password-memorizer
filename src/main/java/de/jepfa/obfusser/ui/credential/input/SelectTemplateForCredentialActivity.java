package de.jepfa.obfusser.ui.credential.input;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;
import de.jepfa.obfusser.viewmodel.template.TemplateListViewModel;

public class SelectTemplateForCredentialActivity extends SecureActivity
implements AdapterView.OnItemSelectedListener{

    private TemplateListViewModel templateListViewModel;
    private CredentialViewModel credentialViewModel;
    private SelectTemplateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_template_for_credential);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        Credential credential = credentialViewModel.getCredential().getValue();
        if (credential.isPersisted()) {
            setTitle("Change credential");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        templateListViewModel = ViewModelProviders
                .of(this)
                .get(TemplateListViewModel.class);

        Spinner templateSpinner = findViewById(R.id.select_template);

        adapter = new SelectTemplateAdapter(
                getBaseContext(), this);
        templateSpinner.setAdapter(adapter);

        templateListViewModel
                .getRepo()
                .getAllTemplatesSortByGroupAndName()
                .observe(this, new Observer<List<Template>>() {
                    @Override
                    public void onChanged(@Nullable final List<Template> templates) {
                        adapter.setTemplates(templates);
                    }
                });

        templateSpinner.setOnItemSelectedListener(this);

        Button nextStepButton = findViewById(R.id.credential_next_step);
        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Credential credential = credentialViewModel.getCredential().getValue();
                Intent intent = new Intent(getBaseContext(), CredentialInputHintsTextActivity.class);
                IntentUtil.setCredentialExtra(intent, credential);

                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Credential credential = credentialViewModel.getCredential().getValue();

            Intent intent = new Intent(this, CredentialInputPatternActivity.class);
            IntentUtil.setCredentialExtra(intent, credential);
            navigateUpTo(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        templateListViewModel
                .getRepo()
                .getTemplateById((int) id)
                .observe(this, new Observer<Template>() {

                    @Override
                    public void onChanged(@Nullable Template template) {
                        if (template != null) {
                            Credential credential = credentialViewModel.getCredential().getValue();
                            credential.copyFrom(template);
                        }
                    }
                });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Credential credential = credentialViewModel.getCredential().getValue();
        credential.setTemplateId(null);
    }

    @Override
    public void refresh(boolean before) {
        adapter.notifyDataSetChanged();
    }
}
