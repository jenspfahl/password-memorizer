package de.jepfa.obfusser.ui.template.list;

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

import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.ui.template.input.TemplateInputNameActivity;
import de.jepfa.obfusser.util.IntentUtil;
import de.jepfa.obfusser.viewmodel.template.TemplateListViewModel;


public class TemplateListFragment extends SecureFragment implements View.OnClickListener{

    private TemplateListViewModel templateListViewModel;
    private TemplateListAdapter adapter;


    public TemplateListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templateListViewModel = ViewModelProviders
                .of(this.getActivity())
                .get(TemplateListViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.navtab_template_list, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TemplateInputNameActivity.class);
                startActivity(intent);
            }
        });

        getActivity().setTitle("Templates");

        RecyclerView recyclerView = view.findViewById(R.id.template_list);
        assert recyclerView != null;

        adapter = new TemplateListAdapter(this, this.getContext(), getBaseActivity());
        recyclerView.setAdapter(adapter);


        templateListViewModel
                .getRepo()
                .getAllTemplatesSortByGroupAndName()
                .observe(this, new Observer<List<Template>>() {
                    @Override
                    public void onChanged(@Nullable final List<Template> templates) {
                        adapter.setTemplates(templates);
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
                Template template = (Template) v.getTag();
                switch (item.getItemId()) {
                    case R.id.menu_change_template:
                        Intent intent = new Intent(v.getContext(), TemplateInputNameActivity.class);
                        IntentUtil.setTemplateExtra(intent, template);
                        startActivity(intent);
                        return true;
                    case R.id.menu_delete_template:
                        templateListViewModel.getRepo().delete(template);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.template_list_menu);
        popup.show();
    }

    @Override
    public void refresh() {
        adapter.notifyDataSetChanged();
    }

}
