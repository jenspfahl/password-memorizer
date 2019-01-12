package de.jepfa.obfusser.ui.group.input;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.BaseActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.viewmodel.group.GroupViewModel;


public class GroupInputNameActivity extends BaseActivity {

    private GroupViewModel groupViewModel;
    private EditText nameView;
    private EditText infoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_input_name);

        groupViewModel = GroupViewModel.getFromIntent(this, getIntent());
        Group group = groupViewModel.getGroup().getValue();

        nameView = findViewById(R.id.group_name);
        String name = group.getName();
        if (name != null) {
            nameView.setText(name);
        }

        infoView = findViewById(R.id.group_info);
        String info = group.getInfo();
        if (info != null) {
            infoView.setText(info);
        }

        if (group.isPersisted()) {
            setTitle("Change group");
        }

        infoView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptNextStep();
                    return true;
                }
                return false;
            }
        });

        Button nextStepButton = findViewById(R.id.create_group_button);
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
            Intent upIntent = new Intent(this, NavigationActivity.class);
            upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_groups);
            navigateUpTo(upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptNextStep() {
        nameView.setError(null);

        String name = nameView.getText().toString();
        String info = infoView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Group group = groupViewModel.getGroup().getValue();
            group.setName(name);
            group.setInfo(info);

            if (group.isPersisted()) {
                groupViewModel.getRepo().update(group);
            }
            else {
                groupViewModel.getRepo().insert(group);
            }

            Intent upIntent = new Intent(getBaseContext(), NavigationActivity.class);
            upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_groups);
            navigateUpTo(upIntent);

        }
    }

}

