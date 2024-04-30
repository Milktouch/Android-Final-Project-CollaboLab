package com.collabolab.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.collabolab.model.Project;

public abstract class CustomProjectFragment extends CustomFragment{
    public Project currentProject;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Project.currentProject = currentProject;
        currentProject.load();
        currentProject.loadTasks();
    }

    public CustomProjectFragment(){
        currentProject = Project.currentProject;
    }
}
