package de.jepfa.obfusser.ui.template.input;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.BaseActivity;
import de.jepfa.obfusser.ui.template.detail.TemplateDetailFragment;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;

public class TemplateInputHintsTextActivity extends BaseActivity {

    private TemplateViewModel templateViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_input_hints_text);

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
                    TemplateDetailFragment.NEW_CREDENTIAL_INPUT_HINTS);

            TemplateDetailFragment detailFragment = new TemplateDetailFragment();
            detailFragment.setArguments(arguments);

            TemplateHintFragment hintsFragment = new TemplateHintFragment();
            hintsFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.template_detail_container_for_input, detailFragment)
                    .add(R.id.template_hints_list, hintsFragment)
                    .commit();
        }


        Button button = findViewById(R.id.create_template_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TemplateHintFragment hintsFragment = (TemplateHintFragment) getSupportFragmentManager().findFragmentById(R.id.template_hints_list);
                Template template = templateViewModel.getTemplate().getValue();

                template.mergeHintsIntoPattern();
                if (template.isPersisted()) {
                    templateViewModel.getRepo().update(template);
                }
                else {
                    templateViewModel.getRepo().insert(template);
                }

                //Intent replyIntent = new Intent(getBaseContext(), NavigationActivity.class);
                //startActivity(replyIntent);
                Intent upIntent = new Intent(getBaseContext(), NavigationActivity.class);
                upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_templates);
                navigateUpTo(upIntent);

            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, TemplateInputHintsActivity.class);
            IntentUtil.setTemplateExtra(upIntent, templateViewModel.getTemplate().getValue());
            navigateUpTo(upIntent);
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
