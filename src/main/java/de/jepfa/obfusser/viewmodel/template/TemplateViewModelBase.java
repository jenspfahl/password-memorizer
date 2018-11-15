package de.jepfa.obfusser.viewmodel.template;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import de.jepfa.obfusser.repository.template.TemplateRepository;

public abstract class TemplateViewModelBase extends AndroidViewModel {
    private TemplateRepository templateRepo;


    public TemplateViewModelBase(Application application) {
        super(application);
        templateRepo = new TemplateRepository(application);
    }

    public TemplateRepository getRepo() {
        return templateRepo;
    }

}
