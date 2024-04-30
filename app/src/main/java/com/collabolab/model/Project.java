package com.collabolab.model;

import com.collabolab.Utilities.OnTasksComplete;
import com.collabolab.model.WorkTask;
import com.collabolab.Utilities.FirebaseTools;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class Project extends FirestoreLoadable{
    private boolean tasksLoaded = false;
    public static Project currentProject;
    private String name;
    private String id;
    public String description;
    public User owner;
    public ArrayList<WorkTask> tasks;
    public ArrayList<ProjectMember> members;

    public Project(DocumentReference docRef) {
        super(docRef);
    }

    public Project(DocumentSnapshot doc) {
        super(doc);
    }
    public Project(String id) {
        super(FirebaseTools.firestore.collection("projects").document(id));
        this.id = id;
    }

    @Override
    protected void setData(DocumentSnapshot doc) {
        this.name = (String) doc.get("name");
        this.description = (String) doc.get("description");
        String ownerId = (String) doc.get("ownerId");
        this.owner = new User(ownerId);
        this.owner.load();
        this.members = new ArrayList<>();
        ArrayList<String> membersId = (ArrayList<String>) doc.get("members");
        for(String memberId : membersId){
            ProjectMember member = new ProjectMember(memberId, this);
            members.add(member);
        }
        doc.getReference().collection("tasks").get().addOnCompleteListener(task -> {
            tasks = new ArrayList<>();
            for(DocumentSnapshot taskDoc : task.getResult()){
                WorkTask workTask = new WorkTask(taskDoc);
                tasks.add(workTask);
            }
            tasksLoaded = true;
        });
    }

    @Override
    protected HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("ownerId", owner.getId());
        map.put("members", members);
        return map;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    public void loadTasks(){
        loadTasks(null);
    }
    public void loadTasks(OnTasksComplete callback){
        getDocRef().collection("tasks").get().addOnCompleteListener(task -> {
            tasks = new ArrayList<>();
            for(DocumentSnapshot taskDoc : task.getResult()){
                WorkTask workTask = new WorkTask(taskDoc);
                tasks.add(workTask);
            }
            tasksLoaded = true;
            if (callback != null)
                callback.onTasksComplete();
        });
    }
    public boolean isTasksLoaded() {
        return tasksLoaded;
    }
}
