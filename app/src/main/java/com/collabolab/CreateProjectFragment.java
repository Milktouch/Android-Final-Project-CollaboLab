package com.collabolab;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.collabolab.CustomDialogs.LoadDialog;
import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Services.NotificationService;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.Project;
import com.collabolab.model.User;

import java.util.HashMap;

public class CreateProjectFragment extends CustomFragment {
    EditText projectName;
    EditText projectDescription;
    Button createProjectButton;

    public CreateProjectFragment() {
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
        return inflater.inflate(R.layout.fragment_create_project, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedLayout.setTitle("Create Project");
        projectName = view.findViewById(R.id.projectNameET);
        projectDescription = view.findViewById(R.id.projectDescET);
        createProjectButton = view.findViewById(R.id.createProjectButton);
        createProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = projectName.getText().toString();
                String desc = projectDescription.getText().toString();
                if(name.isEmpty() || desc.isEmpty()){
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<String, String> data = new HashMap<>();
                data.put("name", name);
                data.put("description", desc);
                data.put("fcmToken", NotificationService.getToken());
                Log.d("FCM", "Token: "+ NotificationService.getToken());
                LoadDialog loadDialog = new LoadDialog(getContext());
                loadDialog.setMessage("Creating project");
                loadDialog.show();
                FirebaseTools.functions.getHttpsCallable("createProject").call(data).addOnCompleteListener(task -> {
                    Log.d("CreateProject", "onComplete: "+task.getResult().getData());
                    HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                    String error = (String) result.get("error");
                    if(error != null){
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadDialog.dismiss();
                    Toast.makeText(getContext(), "Project created", Toast.LENGTH_SHORT).show();
                    String projectId = (String) result.get("projectId");
                    FirebaseTools.firestore.collection("projects").document(projectId).get().addOnSuccessListener(documentSnapshot -> {
                        Project.currentProject = new Project(documentSnapshot);
                        User.currentUser.projects.add(Project.currentProject);
                        parent.setFragmentNoBackStack(new ProjectViewFragment());
                    });

                });
            }
        });

    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        return "Create Project";
    }
}