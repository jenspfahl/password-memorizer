package de.jepfa.obfusser.ui.group.detail;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.NumberedPlaceholder;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.viewmodel.group.GroupViewModel;

public class GroupDetailFragment extends Fragment {

    private GroupViewModel groupViewModel;


    public GroupDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupViewModel = GroupViewModel.get(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.group_detail, container, false);

        if (groupViewModel.getGroup() != null) {
            Group group = groupViewModel.getGroup().getValue();
            TextView textView = rootView.findViewById(R.id.group_detail_textview);
            textView.setText(group.getName() + Constants.NL + " " + group.getInfo());

        }

        return rootView;
    }

}
