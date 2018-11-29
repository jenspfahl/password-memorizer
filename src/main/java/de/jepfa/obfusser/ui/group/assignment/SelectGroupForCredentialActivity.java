package de.jepfa.obfusser.ui.group.assignment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;

public class SelectGroupForCredentialActivity extends SecureActivity
implements AdapterView.OnItemSelectedListener{

    private GroupListViewModel groupListViewModel;
    private CredentialViewModel credentialViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_for_credential);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();
        setTitle("Assign group for " + credential.getName());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        groupListViewModel = ViewModelProviders
                .of(this)
                .get(GroupListViewModel.class);

        final Spinner spinner = findViewById(R.id.select_group);

        final SelectGroupAdapter adapter = new SelectGroupAdapter(getBaseContext());

        spinner.setAdapter(adapter);

        groupListViewModel
                .getRepo()
                .getAllGroupsSortByName()
                .observe(this, new Observer<List<Group>>() {
                    @Override
                    public void onChanged(@Nullable final List<Group> groups) {
                        adapter.setGroups(groups);

                        spinner.setSelected(false);
                        if (credential.getGroupId() != null) {
                            Integer position = adapter.getPositionForGroupId(credential.getGroupId());
                            if (position != null) {
                                spinner.setSelection(position);
                            }
                        }

                    }
                });


        spinner.setOnItemSelectedListener(this);

        Button nextStepButton = findViewById(R.id.credential_next_step);
        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Credential credential = credentialViewModel.getCredential().getValue();
                credentialViewModel.getRepo().update(credential);

                //Intent replyIntent = new Intent(getBaseContext(), NavigationActivity.class);
                //startActivity(replyIntent);
                Intent upIntent = new Intent(getBaseContext(), NavigationActivity.class);
                upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_credentials);
                navigateUpTo(upIntent);
            }
        });

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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Credential credential = credentialViewModel.getCredential().getValue();
        credential.setGroupId((int)id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Credential credential = credentialViewModel.getCredential().getValue();
        credential.setGroupId(null);
    }

    @Override
    public void refresh(boolean before) {
    }
}
