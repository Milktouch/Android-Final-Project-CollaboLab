package com.collabolab.model;

import android.util.Pair;

import com.collabolab.Utilities.FirebaseTools;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;


public class WorkTask extends FirestoreLoadable{

    public static WorkTask currentTask;
    public User assignedTo;
    public String name;
    public String description;
    public String status;
    public Timestamp deadline;
    public Timestamp timestamp;

    public WorkTask(DocumentReference docRef) {
        super(docRef);
    }

    public WorkTask(DocumentSnapshot doc) {
        super(doc);
    }

    @Override
    protected void setData(DocumentSnapshot doc) {
        this.name = doc.get("name").toString();
        this.description = doc.get("description").toString();
        this.status = doc.get("status").toString();
        this.deadline = doc.getTimestamp("deadline");
        this.timestamp = doc.getTimestamp("timestamp");
        String userId = doc.get("assignedTo").toString();
        DocumentReference docRef = FirebaseTools.firestore.collection("users").document(userId);
        User user = new User(docRef);
        user.load();
        assignedTo = user;

    }

    @Override
    protected HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("status", status);
        map.put("deadline", deadline);
        map.put("timestamp", timestamp);
        map.put("assignedTo", assignedTo.getId());
        return map;
    }

    public void logChanges(HashMap<String, Pair<String,String>> changes , User user){
        StringBuilder logString = new StringBuilder();
        for (String key : changes.keySet()) {
            logString.append(key).append(" changed from: ").append(changes.get(key).first).append(" to: ").append(changes.get(key).second).append("\n");
        }
        logChanges(logString.toString(), user);
    }
    public void logChanges(String change, User user){
        HashMap<String, Object> log = new HashMap<>();
        log.put("changer", user.getId());
        log.put("timestamp", Timestamp.now());
        log.put("changes", change);
        getDocRef().collection("logs").document().set(log);
    }
}
