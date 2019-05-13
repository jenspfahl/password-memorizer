package de.jepfa.obfusser.ui.group.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.GroupColor;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.credential.list.CredentialExpandableListFragment;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;
import de.jepfa.obfusser.viewmodel.group.GroupViewModel;

public class SelectGroupColorActivity extends SecureActivity {

    private GroupViewModel groupViewModel;
    private GroupListViewModel groupListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_color);

        groupViewModel = GroupViewModel.getFromIntent(this, getIntent());
        groupListViewModel = ViewModelProviders
                .of(this)
                .get(GroupListViewModel.class);
        final Group group = groupViewModel.getGroup().getValue();
        setTitle(getString(R.string.title_group_color) + " " + group.getName());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        final RadioGroup radioGroup = findViewById(R.id.color_selection);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                group.setColor(checkedId);
            }
        });

        final Observer<List<Group>> observer = new Observer<List<Group>>() {
            @Override
            public void onChanged(@Nullable final List<Group> groups) {
                buildRadioButtons(group, groups, radioGroup);
            }
        };
        final LiveData<List<Group>> allGroups = groupListViewModel
                .getRepo()
                .getAllGroups();
        allGroups.observe(this, observer);


        Button nextStepButton = findViewById(R.id.credential_next_step);
        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allGroups.removeObserver(observer); // avoid updating
                groupViewModel.getRepo().update(group);

                Intent upIntent = new Intent(getBaseContext(), NavigationActivity.class);
                upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_groups);
                navigateUpTo(upIntent);
            }
        });

    }

    private void buildRadioButtons(Group group, List<Group> groups, RadioGroup radioGroup) {
        int selectedColorId = group.getColor();
        String nocolorText = getString(R.string.group_colorize_no_color);
        for (GroupColor groupColor : GroupColor.values()) {
            RadioButton groupRadioButton = new RadioButton(SelectGroupColorActivity.this);
            groupRadioButton.setId(groupColor.getColorInt());
            if (groupColor.getColorInt() == 0) {
                groupRadioButton.setText(nocolorText);
            }
            else {
                int colorId = GroupColor.getAndroidColor(groupColor.getColorInt());
                String usedText = "";
                Group usedGroup = findUsedGroup(groupColor, groups);
                if (usedGroup != null && usedGroup.getId() != group.getId()) {
                    usedText = " (" + usedGroup.getName() + ")";
                }
                SpannableString span = new SpannableString(nocolorText + usedText);
                span.setSpan(new ForegroundColorSpan(colorId), 0, nocolorText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new BackgroundColorSpan(colorId), 0, nocolorText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                groupRadioButton.setText(span);
            }
            if (selectedColorId == groupColor.getColorInt()) {
                groupRadioButton.setChecked(true);
            }
            radioGroup.addView(groupRadioButton);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            //Intent upIntent = new Intent(this, NavigationActivity.class);
            //upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_groups);
            //navigateUpTo(upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean before) {
    }

    private Group findUsedGroup(GroupColor groupColor, List<Group> groups) {

        for (Group existingGroup : groups) {
            if (existingGroup.getColor() == groupColor.getColorInt()) {
                return existingGroup;
            }
        }
        return null;
    }
}
