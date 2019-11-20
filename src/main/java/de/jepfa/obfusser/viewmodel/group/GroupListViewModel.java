package de.jepfa.obfusser.viewmodel.group;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import de.jepfa.obfusser.model.Group;

public class GroupListViewModel extends GroupViewModelBase {

    private LiveData<List<Group>> groups;

    public GroupListViewModel(Application application) {
        super(application);
        groups = getRepo().getAllGroups();
    }

    public LiveData<List<Group>> getGroups() {
        return groups;
    }

}
