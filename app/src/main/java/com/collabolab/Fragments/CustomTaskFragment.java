package com.collabolab.Fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.collabolab.model.WorkTask;

public abstract class CustomTaskFragment extends CustomProjectFragment{
    public WorkTask currentTask;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WorkTask.currentTask = currentTask;
        currentTask.load();
    }


    public CustomTaskFragment(){
        super();
        currentTask= WorkTask.currentTask;
    }
}
