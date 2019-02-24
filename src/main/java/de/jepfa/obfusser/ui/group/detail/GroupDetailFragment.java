package de.jepfa.obfusser.ui.group.detail;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.GroupColor;
import de.jepfa.obfusser.ui.common.Debug;
import de.jepfa.obfusser.ui.common.GroupColorizer;
import de.jepfa.obfusser.util.IntentUtil;
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
            final Group group = groupViewModel.getGroup().getValue();
            TextView infoTextView = rootView.findViewById(R.id.group_detail_textview);
            if (group.getInfo() != null) {
                infoTextView.setText(group.getInfo());
            }
            else {
                infoTextView.setText("         "); // be clickable for debugging
            }

            final TextView colorTextView = rootView.findViewById(R.id.group_detail_color);
            char indicator;
            if (group.getColor() == 0) {
                indicator = GroupColorizer.COLOR_INDICATION_EMPTY;
                colorTextView.setTextColor(Color.GRAY);
            }
            else {
                indicator = GroupColorizer.COLOR_INDICATION_FULL;
                colorTextView.setTextColor(GroupColor.getAndroidColor(group.getColor()));
            }
            colorTextView.setText(String.valueOf(indicator));

            colorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), SelectGroupColorActivity.class);
                    IntentUtil.setGroupExtra(intent, group);
                    startActivity(intent);
                }
            });

            final GestureDetector longPressGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                public void onLongPress(MotionEvent event) {
                    String message = group.toString();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Debug group")
                            .setMessage(message)
                            .show();
                }
            });


            infoTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (Debug.INSTANCE.isDebug()) {
                        return longPressGestureDetector.onTouchEvent(motionEvent);
                    }
                    return false;

                }
            });
        }

        return rootView;
    }

}
