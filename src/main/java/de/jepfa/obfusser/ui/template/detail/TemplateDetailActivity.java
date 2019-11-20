package de.jepfa.obfusser.ui.template.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.DeletionHelper;
import de.jepfa.obfusser.ui.common.LegendShower;
import de.jepfa.obfusser.ui.common.detail.PatternDetailFragment;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.ui.template.input.TemplateInputNameActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;


public class TemplateDetailActivity extends SecureActivity {

    private TemplateViewModel templateViewModel;
    private TemplateDetailFragment templateDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_detail);

        templateViewModel = TemplateViewModel.getFromIntent(this, getIntent());
        Template template = templateViewModel.getTemplate().getValue();

        Toolbar toolbar = findViewById(R.id.activity_template_detail_toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout appBarLayout = findViewById(R.id.template_detail_toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(template.getName());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle arguments = new Bundle();
        if (savedInstanceState != null) {
            int currentClickStep = savedInstanceState.getInt(PatternDetailFragment.CURRENT_CLICK_STEP,
                    PatternDetailFragment.DEFAULT_CLICK_STEP);
            arguments.putInt(PatternDetailFragment.CURRENT_CLICK_STEP, currentClickStep);
        }

        templateDetailFragment = new TemplateDetailFragment();
        templateDetailFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.template_detail_container, templateDetailFragment)
                .commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int currentClickStep = templateDetailFragment.getArguments().getInt(PatternDetailFragment.CURRENT_CLICK_STEP,
                PatternDetailFragment.DEFAULT_CLICK_STEP);
        outState.putInt(PatternDetailFragment.CURRENT_CLICK_STEP, currentClickStep);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.legend);
        getMenuInflater().inflate(R.menu.template_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, NavigationActivity.class);
            upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_templates);
            navigateUpTo(upIntent);
            return true;
        }

        Template template = templateViewModel.getTemplate().getValue();
        switch (item.getItemId()) {
            case 0:
                LegendShower.INSTANCE.showLegend(this, getPatternRepresentation());
                return true;
            case R.id.menu_change_template:
                Intent intent = new Intent(this, TemplateInputNameActivity.class);
                IntentUtil.INSTANCE.setTemplateExtra(intent, template);
                startActivity(intent);
                return true;

            case R.id.menu_delete_template:
                DeletionHelper.INSTANCE.askAndDelete(templateViewModel.getRepo(), template, this, new Runnable() {
                    @Override
                    public void run() {
                        Intent upIntent = new Intent(TemplateDetailActivity.this, NavigationActivity.class);
                        upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_templates);
                        navigateUpTo(upIntent);
                    }
                });

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
