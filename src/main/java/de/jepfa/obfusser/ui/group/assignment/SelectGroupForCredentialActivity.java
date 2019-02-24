package de.jepfa.obfusser.ui.group.assignment;

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
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.GroupColorizer;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;

public class SelectGroupForCredentialActivity extends SecureActivity {

    private CredentialViewModel credentialViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_for_credential);

        credentialViewModel = CredentialViewModel.getFromIntent(this, getIntent());
        final Credential credential = credentialViewModel.getCredential().getValue();
        setTitle(getString(R.string.title_assign_group) + " " + credential.getName());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        GroupListViewModel groupListViewModel = ViewModelProviders
                .of(this)
                .get(GroupListViewModel.class);

        final RadioGroup radioGroup = findViewById(R.id.group_selection);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                credential.setGroupId(checkedId != Constants.NO_ID ? checkedId : null);
            }
        });

        groupListViewModel
                .getRepo()
                .getAllGroupsSortByName()
                .observe(this, new Observer<List<Group>>() {
                    @Override
                    public void onChanged(@Nullable final List<Group> groups) {

                        Integer selectedGroupId = null;
                        if (credential.getGroupId() != null) {
                            selectedGroupId  = credential.getGroupId();
                        }

                        RadioButton noGroupRadioButton = new RadioButton(SelectGroupForCredentialActivity.this);
                        noGroupRadioButton.setId(Constants.NO_ID);
                        noGroupRadioButton.setText(getString(R.string.no_group));
                        if (selectedGroupId == null) {
                            noGroupRadioButton.setChecked(true);
                        }
                        radioGroup.addView(noGroupRadioButton);

                        for (Group group : groups) {
                            RadioButton groupRadioButton = new RadioButton(SelectGroupForCredentialActivity.this);
                            groupRadioButton.setId(group.getId());
                            groupRadioButton.setText(GroupColorizer.getColorizedText(group, group.getName()));
                            if (selectedGroupId != null && group.getId() == selectedGroupId.intValue()) {
                                groupRadioButton.setChecked(true);
                            }
                            radioGroup.addView(groupRadioButton);
                        }
                    }
                });


        Button nextStepButton = findViewById(R.id.credential_next_step);
        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Credential credential = credentialViewModel.getCredential().getValue();
                credentialViewModel.getRepo().update(credential);

                // TODO onBackPressed(); go back to detail if we come from there
                Intent upIntent = new Intent(getBaseContext(), NavigationActivity.class);
                upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_credentials); //TODO destination should be dynamic
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
    public void refresh(boolean before) {
    }
}
