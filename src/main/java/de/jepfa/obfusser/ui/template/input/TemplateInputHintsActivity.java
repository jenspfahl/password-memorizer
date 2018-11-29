package de.jepfa.obfusser.ui.template.input;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.template.detail.TemplateDetailFragment;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;

public class TemplateInputHintsActivity extends SecureActivity {

    private TemplateViewModel templateViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_input_hints);

        templateViewModel = TemplateViewModel.getFromIntent(this, getIntent());
        Template template = templateViewModel.getTemplate().getValue();
        if (template.isPersisted()) {
            setTitle("Change template");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putInt(TemplateDetailFragment.ARG_MODE,
                    TemplateDetailFragment.NEW_CREDENTIAL_SELECT_HINTS);

            TemplateDetailFragment fragment = new TemplateDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.template_detail_container_for_input, fragment)
                    .commit();
        }


        Button button = findViewById(R.id.hints_template_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Template template = templateViewModel.getTemplate().getValue();


                if (!template.getHints().isEmpty()) {
                    Intent intent = new Intent(getBaseContext(), TemplateInputHintsTextActivity.class);
                    IntentUtil.setTemplateExtra(intent, template);

                    startActivity(intent);
                }
                else {
                    Snackbar.make(view, "Select at least one hint.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Template template = templateViewModel.getTemplate().getValue();

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
}
