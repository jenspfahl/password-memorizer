package de.jepfa.obfusser.ui.template.input;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.ui.SecureFragment;
import de.jepfa.obfusser.viewmodel.template.TemplateViewModel;


public class TemplateHintFragment extends SecureFragment {

    private TemplateViewModel templateViewModel;
    private TemplateHintRecyclerViewAdapter adapter;

    public TemplateHintFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        templateViewModel = ViewModelProviders
                .of(this.getActivity())
                .get(TemplateViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.template_hint_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            adapter = new TemplateHintRecyclerViewAdapter(
                    templateViewModel.getTemplate().getValue(), getSecureActivity());
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    public boolean checkMandatoryFields() {
        return true; //TODO
    }

    @Override
    public void refresh() {
        adapter.notifyDataSetChanged();
    }
}
