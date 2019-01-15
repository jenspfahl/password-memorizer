package de.jepfa.obfusser.ui.credential.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.DeletionHelper;
import de.jepfa.obfusser.ui.common.LegendShower;
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity;
import de.jepfa.obfusser.ui.group.assignment.SelectGroupForCredentialActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;


public class CredentialDetailActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_detail);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();

        GroupListViewModel groupListViewModel = ViewModelProviders
                .of(this)
                .get(GroupListViewModel.class);

        Toolbar toolbar = findViewById(R.id.activity_credential_detail_toolbar);
        setSupportActionBar(toolbar);

        final CollapsingToolbarLayout appBarLayout = findViewById(R.id.credential_detail_toolbar_layout);
        if (appBarLayout != null) {
            StringBuilder sb = new StringBuilder(credential.getName());
            groupListViewModel
                    .getRepo()
                    .getGroupFromPattern(credential)
                    .observe(this, new Observer<Group>() {

                        @Override
                        public void onChanged(@Nullable Group group) {
                            if (group != null) {
                                appBarLayout.setTitle(credential.getName() + " (" + group.getName() + ")");
                            }
                        }
                    });

            appBarLayout.setTitle(sb.toString());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putInt(CredentialDetailFragment.ARG_MODE,
                    CredentialDetailFragment.SHOW_DETAIL);

            CredentialDetailFragment fragment = new CredentialDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.credential_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.legend);
        getMenuInflater().inflate(R.menu.credential_list_menu, menu);
        return true;
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

        Credential credential = credentialViewModel.getCredential().getValue();
        switch (item.getItemId()) {
            case 0:
                LegendShower.showLegend(this, getPatternRepresentation());
                return true;
            case R.id.menu_change_credential:
                Intent intent = new Intent(this, CredentialInputNameActivity.class);
                IntentUtil.setCredentialExtra(intent, credential);
                startActivity(intent);
                return true;

            case R.id.menu_assign_group_credential:
                intent = new Intent(this, SelectGroupForCredentialActivity.class);
                IntentUtil.setCredentialExtra(intent, credential);
                startActivity(intent);
                return true;

            case R.id.menu_delete_credential:
                DeletionHelper.askAndDelete(credentialViewModel.getRepo(), credential, this, new Runnable() {
                    @Override
                    public void run() {
                        Intent upIntent = new Intent(CredentialDetailActivity.this, NavigationActivity.class);
                        upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_credentials);
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
