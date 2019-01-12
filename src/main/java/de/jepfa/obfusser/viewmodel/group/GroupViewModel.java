package de.jepfa.obfusser.viewmodel.group;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.util.IntentUtil;

public class GroupViewModel extends GroupViewModelBase {

    private MutableLiveData<Group> group = new MutableLiveData<>();


    public GroupViewModel(Application application) {
        super(application);
    }

    public LiveData<Group> getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group.setValue(group);
    }

    public static GroupViewModel getFromIntent(FragmentActivity activity, Intent intent) {
        GroupViewModel groupViewModel = get(activity);
        groupViewModel.setGroup(
                IntentUtil.createGroupFromIntent(intent));

        return groupViewModel;
    }

    public static GroupViewModel get(FragmentActivity activity) {
        return ViewModelProviders
                .of(activity)
                .get(GroupViewModel.class);
    }
}
