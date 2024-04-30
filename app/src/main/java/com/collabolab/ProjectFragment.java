package com.collabolab;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.CustomDialogs.LoadDialog;
import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.FirestoreLoadable;
import com.collabolab.model.OnDataLoadedListener;
import com.collabolab.model.PermissionSet;
import com.collabolab.model.Project;
import com.collabolab.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;



public class ProjectFragment extends CustomFragment {


    LinearLayout projectList;

    LinearLayout createProjectButton;

    

    public ProjectFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        projectList = container.findViewById(R.id.projectCardList);
        createProjectButton = container.findViewById(R.id.createProjectButton);
        createProjectButton.setOnClickListener(view1 -> {
            parent.setFragment(new CreateProjectFragment());
        });
        initProjectList();
        initProjectInviteList();
    }

    private void initProjectInviteList() {
        User.currentUser.getDocRef().collection("invites").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    String projectId = doc.getId();
                    String name = doc.getString("name");
                    String description = doc.getString("description");
                    createProjectInviteCard(name, description, projectId);
                }
            }
        });
    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        return "Projects";
    }

    private void addProjectCard(Project proj) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.project_card, projectList, false);
        projectList.addView(cardView);

        TextView title = cardView.findViewById(R.id.name);
        TextView desc = cardView.findViewById(R.id.description);
        title.setText(proj.getName());
        desc.setText(proj.description);

        ImageView deleteButton = cardView.findViewById(R.id.deleteProjectBtn);
        deleteButton.setOnClickListener(view -> {
            if (proj.owner.getId().equals(User.currentUser.getId())) {
                deleteProjectDialog(proj);
            } else {
                leaveProjectDialog(proj);
            }
        });

        cardView.setOnClickListener(view -> {
            Project.currentProject = proj;
            DocumentReference docRef = proj.getDocRef().collection("permissions").document(User.currentUser.getDocRef().getId());
            PermissionSet.userPermissions = new PermissionSet(docRef);
            PermissionSet.userPermissions.load((OnDataLoadedListener<PermissionSet>) (data, success) -> {
                parent.setFragment(new ProjectViewFragment());
            });
        });
    }
    private void initProjectList() {
        for (Project proj : User.currentUser.projects) {
            if (proj.isLoaded())
                addProjectCard(proj);
            else
                proj.load(new OnDataLoadedListener() {
                    @Override
                    public void onDataLoaded(FirestoreLoadable data, boolean success) {
                        if (success) {
                            addProjectCard(proj);
                        }
                    }
                });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container =inflater.inflate(R.layout.fragment_project, container, false);
        // Inflate the layout for this fragment
        return this.container;
    }


    private void deleteProjectDialog(Project proj) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Project");
        builder.setMessage("Are you sure you want to delete this project? \nThis action cannot be undone.");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            User.currentUser.projects.remove(proj);
            HashMap<String, Object> data = new HashMap<>();
            data.put("projectId", proj.getDocRef().getId());
            FirebaseTools.functions.getHttpsCallable("deleteProject").call(data);
            projectList.removeAllViews();
            initProjectList();
            dialog.cancel();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

    private void leaveProjectDialog(Project proj) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Leave Project");
        builder.setMessage("Are you sure you want to leave this project?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            User.currentUser.projects.remove(proj);
            HashMap<String, Object> data = new HashMap<>();
            data.put("projectId", proj.getDocRef().getId());
            data.put("userId", User.currentUser.getDocRef().getId());
            data.put("userDecision", true);
            FirebaseTools.functions.getHttpsCallable("removeFromProject").call(data);
            projectList.removeAllViews();
            initProjectList();
            dialog.cancel();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

    private void createProjectInviteCard(String name, String description,String id){
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.project_invite_card, projectList, false);
        projectList.addView(cardView);
        TextView title = cardView.findViewById(R.id.name);
        TextView desc = cardView.findViewById(R.id.description);
        title.setText(name);
        desc.setText(description);
        TextView acceptButton = cardView.findViewById(R.id.acceptInviteBtn);
        TextView declineButton = cardView.findViewById(R.id.rejectInviteBtn);
        acceptButton.setOnClickListener(view -> {
            LoadDialog loadDialog = new LoadDialog(getContext());
            loadDialog.setMessage("Accepting invite...");
            HashMap<String, Object> data = new HashMap<>();
            data.put("projectId", id);
            data.put("userId", User.currentUser.getId());
            cardView.setVisibility(View.GONE);
            FirebaseTools.functions.getHttpsCallable("acceptInvite").call(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadDialog.setMessage("Loading project...");
                    Project p = new Project(id);
                    p.load(new OnDataLoadedListener() {
                        @Override
                        public void onDataLoaded(FirestoreLoadable data, boolean success) {
                            loadDialog.dismiss();
                            if (success) {
                                User.currentUser.projects.add(p);
                                addProjectCard(p);
                            }
                            else{
                                Toast.makeText(getContext(), "Failed to load project", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    loadDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to accept invite", Toast.LENGTH_SHORT).show();
                }
            });
        });
        declineButton.setOnClickListener(view -> {
            User.currentUser.getDocRef().collection("invites").document(id).delete();
            cardView.setVisibility(View.GONE);
        });


    }
}