package de.jepfa.obfusser.ui.credential.list;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.credential.detail.CredentialDetailActivity;
import de.jepfa.obfusser.ui.settings.SettingsActivity;
import de.jepfa.obfusser.util.IntentUtil;

public class CredentialFlatListAdapter extends RecyclerView.Adapter<CredentialFlatListAdapter.ViewHolder>
implements Filterable {

    private final CredentialListFragmentBase fragment;

    private final LayoutInflater inflater;
    private List<Credential> credentials; // Cached copy of credentials
    private List<Credential> originCredentials;

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filterResults.values = originCredentials;
                } else {
                    List<Credential> filteredList = new ArrayList<>();
                    for (Credential credential : originCredentials) {
                        if (credential.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(credential);
                        }
                    }

                    filterResults.values = filteredList;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                credentials = (ArrayList<Credential>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView patternView;
        final ImageView iconView;

        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.credential_list_name);
            patternView = view.findViewById(R.id.credential_list_pattern);
            iconView = view.findViewById(R.id.credential_list_menu_popup);

            iconView.setOnClickListener(fragment);
        }
    }


    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Credential item = (Credential) view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context, CredentialDetailActivity.class);
            IntentUtil.setCredentialExtra(intent, item);
            context.startActivity(intent);
        }
    };

    CredentialFlatListAdapter(CredentialListFragmentBase fragment) {
        inflater = LayoutInflater.from(fragment.getContext());
        this.fragment = fragment;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.credential_flat_list_content,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (credentials != null || !credentials.isEmpty()) {

            Credential credential = credentials.get(position);

            holder.nameView.setText(credential.getName());

            boolean showPattern = PreferenceManager
                    .getDefaultSharedPreferences(fragment.getActivity())
                    .getBoolean(SettingsActivity.PREF_SHOW_PATTERN_IN_OVERVIEW, true);

            if (showPattern) {
                holder.patternView.setText(credential.getPatternRepresentationHinted(
                        SecureActivity.SecretChecker.getOrAskForSecret(fragment.getSecureActivity()),
                        fragment.getSecureActivity().getPatternRepresentation(),
                        SecureActivity.SecretChecker.isEncWithUUIDEnabled(fragment.getActivity())));
            }
            else {
                holder.patternView.setText(credential.getHiddenPatternRepresentation(
                        fragment.getSecureActivity().getPatternRepresentation()));
            }

            holder.iconView.setTag(credential);
            holder.nameView.setOnClickListener(onClickListener);
            holder.nameView.setTag(credential);
            holder.patternView.setOnClickListener(onClickListener);
            holder.patternView.setTag(credential);
        }
    }

    void setCredentials(List<Credential> credentials){
        this.credentials = credentials;
        this.originCredentials = credentials;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (credentials != null)
            return credentials.size();
        else return 0;
    }
}