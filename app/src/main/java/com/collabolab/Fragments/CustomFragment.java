package com.collabolab.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.collabolab.SharedLayout;

public abstract class CustomFragment extends Fragment {

    protected View container;
    protected SharedLayout parent;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view;
        parent = SharedLayout.parentActivity;
    }

    public abstract ImageButton getActiveButton();
    public abstract String getTitle();

}
