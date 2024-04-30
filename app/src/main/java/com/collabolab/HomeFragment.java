package com.collabolab;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.constraintlayout.widget.ConstraintLayout;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.Update;
import com.collabolab.model.User;
import com.collabolab.model.UserUpdates;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class HomeFragment extends CustomFragment {



    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    final float[] marginsPerNumber = {7.5f, 8, 7, 8, 7, 7, 8, 8, 7, 7,4};

    private LinearLayout updatesLayout;
    private TextView noUpdatesTV;
    private TextView notificationsTV;

    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatesLayout = view.findViewById(R.id.updatesContainer);
        noUpdatesTV = view.findViewById(R.id.noUpdatesTV);
        notificationsTV = view.findViewById(R.id.notificationCounter);
    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.homeBtn;
    }

    @Override
    public String getTitle() {
        return "Home";
    }

    @Override
    public void onResume() {
        super.onResume();
        UserUpdates.getInstance().setCallback(() -> {
            setUpdates(UserUpdates.getInstance().getUpdates());
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        UserUpdates.getInstance().removeCallback();
    }

    public void createUpdateCard(Update u) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.title_decription_card, null);
        updatesLayout.addView(view);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(0, 0, 0, GeneralUtilities.dpToPixelsInt(10, getResources()));
        TextView title = view.findViewById(R.id.titleTV);
        TextView description = view.findViewById(R.id.descriptionTV);
        title.setText(u.getTitle());
        description.setText(u.getDescription());
        String type = u.getViewType();
        if (type.equals("one time")) {
            User.currentUser.getDocRef().collection("otherUpdates").document(u.getId()).delete();
        }

    }

    public void setUpdates(List<Update> updates) {
        updatesLayout.removeAllViews();
        int i = Math.min(updates.size(), 10);
        int margin = GeneralUtilities.dpToPixelsInt(marginsPerNumber[i], getResources());
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)notificationsTV.getLayoutParams();
        params.setMargins(0,GeneralUtilities.dpToPixelsInt(-1,getResources()),margin,0);
        notificationsTV.setLayoutParams(params);
        String notificationCount = (i == 10) ? "9+" : String.valueOf(i);
        notificationsTV.setText(notificationCount);
        if (updates.size() == 0) {
            noUpdatesTV.setVisibility(View.VISIBLE);
        } else {
            noUpdatesTV.setVisibility(View.GONE);
            for (Update u : updates) {
                createUpdateCard(u);
            }
        }
    }




}