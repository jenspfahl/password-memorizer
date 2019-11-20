package de.jepfa.obfusser.ui.group.list;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.CommonMenuFragmentBase;
import de.jepfa.obfusser.ui.common.DeletionHelper;
import de.jepfa.obfusser.ui.group.detail.SelectGroupColorActivity;
import de.jepfa.obfusser.ui.group.input.GroupInputNameActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.group.GroupListViewModel;


public class GroupListFragment extends CommonMenuFragmentBase implements View.OnClickListener{

    private GroupListViewModel groupListViewModel;


    public GroupListFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getMenuId() {
        return R.menu.toolbar_menu_group;
    }

    @Override
    protected Filterable getFilterable() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupListViewModel = ViewModelProviders
                .of(this.getActivity())
                .get(GroupListViewModel.class);

    }

    @Override
    public void refresh() {
        refreshMenuLockItem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navtab_group_list, container, false);

        // get secret also here to avoid unexpected user timeouts
        SecureActivity.SecretChecker.getOrAskForSecret(getSecureActivity());

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, GroupInputNameActivity.class);
                startActivity(intent);
            }
        });

        getActivity().setTitle(R.string.title_groups);

        RecyclerView recyclerView = view.findViewById(R.id.group_list);
        assert recyclerView != null;

        final GroupListAdapter adapter = new GroupListAdapter(this, this.getContext());
        recyclerView.setAdapter(adapter);


        groupListViewModel
                .getGroups()
                .observe(this, new Observer<List<Group>>() {
                    @Override
                    public void onChanged(@Nullable final List<Group> groups) {
                        adapter.setGroups(groups);
                    }
                });

        return view;
    }

    @Override
    public void onClick(final View v) {
        PopupMenu popup = new PopupMenu(this.getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Group group = (Group) v.getTag();
                switch (item.getItemId()) {
                    case R.id.menu_change_group:
                        Intent intent = new Intent(v.getContext(), GroupInputNameActivity.class);
                        IntentUtil.INSTANCE.setGroupExtra(intent, group);
                        startActivity(intent);
                        return true;
                    case R.id.menu_change_color:
                        intent = new Intent(v.getContext(), SelectGroupColorActivity.class);
                        IntentUtil.INSTANCE.setGroupExtra(intent, group);
                        startActivity(intent);
                        return true;
                    case R.id.menu_delete_group:
                        DeletionHelper.INSTANCE.askAndDelete(groupListViewModel.getRepo(), group, getActivity(), null);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.group_list_menu);
        popup.show();
    }

}
