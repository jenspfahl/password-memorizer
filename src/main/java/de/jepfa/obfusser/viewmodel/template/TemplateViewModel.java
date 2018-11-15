package de.jepfa.obfusser.viewmodel.template;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.util.IntentUtil;

public class TemplateViewModel extends TemplateViewModelBase {

    private MutableLiveData<Template> template = new MutableLiveData<>();


    public TemplateViewModel(Application application) {
        super(application);
    }

    public LiveData<Template> getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template.setValue(template);
    }

    public static TemplateViewModel getFromIntent(FragmentActivity activity, Intent intent) {
        TemplateViewModel templateViewModel = get(activity);
        templateViewModel.setTemplate(
                IntentUtil.createTemplateFromIntent(intent));

        return templateViewModel;
    }

    public static TemplateViewModel get(FragmentActivity activity) {
        return ViewModelProviders
                .of(activity)
                .get(TemplateViewModel.class);
    }
}
