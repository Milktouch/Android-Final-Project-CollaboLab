package com.collabolab.model;

import android.app.Activity;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.Utilities.OnTasksComplete;
import com.collabolab.Utilities.TaskCombiner;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;

public class User extends FirestoreLoadable {
    public static final String NULL_USER_ID = "W2fwORpFcBjiTBICwnwm";
    public static User currentUser = null;
    public String name="";
    public String email="";
    public String phone="";
    public ArrayList<Project> projects = new ArrayList<Project>();

    public User(DocumentReference docRef) {
        super(docRef);
    }
    public User(DocumentSnapshot doc) {
        super(doc);
        setData(doc);
    }
    public User(String id) {
        super(FirebaseTools.firestore.collection("users").document(id));
    }

    //
    @Override
    protected void setData(DocumentSnapshot doc) {
        this.name = doc.get("name").toString();
        this.email = doc.get("email").toString();
        this.phone = doc.get("phone").toString();
        ArrayList<String> projectId = (ArrayList<String>) doc.get("projects");
        projects = new ArrayList<>();
        for (String id : projectId) {
            DocumentReference docRef = FirebaseTools.firestore.collection("projects").document(id);
            Project project = new Project(docRef);
            projects.add(project);
        }
    }
    public TaskCombiner loadProjects(OnTasksComplete listener){
        Task[] tasks = new Task[projects.size()];
        for (int i = 0; i < projects.size(); i++) {
            tasks[i] = projects.get(i).load();
        }
        TaskCombiner combiner = new TaskCombiner(tasks,listener);
        combiner.start();
        return combiner;
    }
    @Override
    protected HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        ArrayList<String> projectsId = new ArrayList<>();
        for (Project project : projects) {
            projectsId.add(project.getId());
        }
        map.put("projects", projectsId);
        return map;
    }
}
