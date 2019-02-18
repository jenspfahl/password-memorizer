package de.jepfa.obfusser.ui.common.input;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.ui.SecureFragment;

public abstract class PatternSelectHintsFragment extends SecureFragment {

    private PatternSelectHintsAdapter adapter;
    private HintUpdateListener hintUpdateListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView rootView = (RecyclerView) inflater.inflate(R.layout.pattern_select_hints, container, false);
        Context context = rootView.getContext();
        rootView.setLayoutManager(new LinearLayoutManager(context, LinearLayout.HORIZONTAL, false));

        SecurePatternHolder pattern = getPattern();

        adapter = new PatternSelectHintsAdapter(pattern, getSecureActivity(), hintUpdateListener);
        rootView.setAdapter(adapter);

        return rootView;
    }

    protected abstract SecurePatternHolder getPattern();

    public void setHintUpdateListener(HintUpdateListener hintUpdateListener) {
        this.hintUpdateListener = hintUpdateListener;
    }

    @Override
    public void refresh() {
        getActivity().recreate(); //TODO
    }

}
