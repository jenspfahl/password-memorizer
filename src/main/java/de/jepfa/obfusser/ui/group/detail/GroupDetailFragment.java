package de.jepfa.obfusser.ui.group.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.R;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.ui.SecureActivity;
import de.jepfa.obfusser.ui.common.Debug;
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
            TextView textView = rootView.findViewById(R.id.group_detail_textview);
            if (group.getInfo() != null) {
                textView.setText(group.getInfo());
            }

            final GestureDetector longPressGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                public void onLongPress(MotionEvent event) {
                    String message = group.toString();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Debug group")
                            .setMessage(message)
                            .show();
                }
            });


            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (Debug.isDebug()) {
                        return longPressGestureDetector.onTouchEvent(motionEvent);
                    }
                    return false;

                }
            });
        }

        return rootView;
    }

}
