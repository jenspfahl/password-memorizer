package de.jepfa.obfusser.ui.credential.input;

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
import de.jepfa.obfusser.viewmodel.credential.CredentialViewModel;


public class CredentialHintFragment extends SecureFragment {

    private CredentialViewModel credentialViewModel;
    private CredentialHintRecyclerViewAdapter adapter;

    public CredentialHintFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentialViewModel = ViewModelProviders
                .of(this.getActivity())
                .get(CredentialViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.credential_hint_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            adapter = new CredentialHintRecyclerViewAdapter(
                    credentialViewModel.getCredential().getValue(),
                    getBaseActivity());

            recyclerView.setAdapter(
                    adapter);
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
