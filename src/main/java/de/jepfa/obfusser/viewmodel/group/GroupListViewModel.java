package de.jepfa.obfusser.viewmodel.group;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.viewmodel.group.GroupViewModelBase;

public class GroupListViewModel extends GroupViewModelBase {

    public GroupListViewModel(Application application) {
        super(application);
    }

}
