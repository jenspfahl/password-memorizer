package de.jepfa.obfusser.ui.template.input;

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
import de.jepfa.obfusser.model.CryptString;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.navigation.NavigationActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;


public class TemplateInputNameActivity extends SecureActivity {

    private TemplateViewModel templateViewModel;
    private EditText nameView;
    private EditText infoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_input_name);

        templateViewModel = TemplateViewModel.getFromIntent(this, getIntent());
        Template template = templateViewModel.getTemplate().getValue();

        nameView = findViewById(R.id.template_name);
        CryptString name = template.getName();
        if (name != null) {
            nameView.setText(name);
        }

        infoView = findViewById(R.id.template_info);
        CryptString info = template.getInfo();
        if (info != null) {
            infoView.setText(info);
        }

        if (template.isPersisted()) {
            setTitle(R.string.title_change_template);
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

        Button nextStepButton = findViewById(R.id.template_next_step);
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
            upIntent.putExtra(NavigationActivity.SELECTED_NAVTAB, R.id.navigation_templates);
            navigateUpTo(upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refresh(boolean before) {
    }

    private void attemptNextStep() {
        nameView.setError(null);

        String name = nameView.getText().toString();

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
            Template template = templateViewModel.getTemplate().getValue();
            template.setName(CryptString.of(nameView.getText().toString()));
            template.setInfo(CryptString.of(infoView.getText().toString()));

            Intent intent = new Intent(getBaseContext(), TemplateInputPatternActivity.class);
            IntentUtil.setTemplateExtra(intent, template);
            startActivity(intent);

        }
    }

}

