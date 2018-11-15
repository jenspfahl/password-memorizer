package de.jepfa.obfusser.viewmodel.template;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import de.jepfa.obfusser.model.Template;

public class TemplateListViewModel extends TemplateViewModelBase {

    private LiveData<List<Template>> templates;

    public TemplateListViewModel(Application application) {
        super(application);
        templates = getRepo().getAllTemplatesSortByGroupAndName();
    }

    public LiveData<List<Template>> getTemplates() {
        return templates;
    }

}
