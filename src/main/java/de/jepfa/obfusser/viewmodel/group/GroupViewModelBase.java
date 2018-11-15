package de.jepfa.obfusser.viewmodel.group;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import de.jepfa.obfusser.repository.group.GroupRepository;

public abstract class GroupViewModelBase extends AndroidViewModel {
    private GroupRepository groupRepo;


    public GroupViewModelBase(Application application) {
        super(application);
        groupRepo = new GroupRepository(application);
    }

    public GroupRepository getRepo() {
        return groupRepo;
    }

}
