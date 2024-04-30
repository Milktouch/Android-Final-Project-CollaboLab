package com.collabolab.model;

import com.collabolab.Utilities.FirebaseTools;
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.List;

public class UserUpdates {
    private static UserUpdates instance;
    public static UserUpdates getInstance(){
        if(instance == null){
            instance = new UserUpdates(User.currentUser);
        }
        return instance;
    }
    private UpdateCallback callback;

    public static void resetInstance(){
        getInstance().removeCallback();
        instance = null;
    }
    public static void startListening(User user){
        instance = new UserUpdates(user);
    }
    private UserUpdates(User user){
        updates = new ArrayList<>();
        user.getDocRef().collection("otherUpdates").addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            synchronized (updates) {
                for (DocumentChange doc : value.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String type = doc.getDocument().getString("viewType");
                        String title = doc.getDocument().getString("title");
                        String description = doc.getDocument().getString("description");
                        Update update = new Update(title, description, type, doc.getDocument().getReference().getId());
                        updates.add(update);
                    }
                }
            }
            if(callback != null){
                callback.onUpdatesReceived();
            }
        });
    }
    private ArrayList<Update> updates;

    public ArrayList<Update> getUpdates() {
        ArrayList<Update> copy = new ArrayList<>();
        synchronized (updates) {
            copy.addAll(updates);
        }
        return copy;
    }

    public void setCallback(UpdateCallback callback) {
        this.callback = callback;
        if(!getUpdates().isEmpty())
            callback.onUpdatesReceived();
    }
    public void removeCallback(){
        this.callback = null;
    }
}
