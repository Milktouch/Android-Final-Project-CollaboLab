package com.collabolab.model;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public abstract class FirestoreLoadable {
    private DocumentReference docRef;
    private boolean loaded = false;
    private String id;
    public void load(OnDataLoadedListener listener){
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                Log.d("Firestore Load", "loaded doc "+doc.getId()+" exists: "+doc.exists());
                if(doc.exists()){
                    setData(doc);
                    loaded = true;
                    listener.onDataLoaded(this, true);
                }
                else{
                    listener.onDataLoaded(this, false);
                }
            }
            else{
                listener.onDataLoaded(this, false);
            }
        });
    }
    public Task load(){
        return docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    setData(doc);
                    loaded = true;
                }
            }
        });
    }
    public void updateRealtime(OnDataLoadedListener listener){
        docRef.addSnapshotListener((value, error) -> {
            if(value != null){
                setData(value);
                loaded = true;
                listener.onDataLoaded(this, true);
            }
            else{
                listener.onDataLoaded(this, false);
            }
        });
    }
    public void updateRealtime(){
        docRef.addSnapshotListener((value, error) -> {
            if(value != null){
                setData(value);
                loaded = true;
            }
        });
    }
    protected FirestoreLoadable(DocumentReference docRef){
        this.docRef = docRef;
        id = docRef.getId();
    }
    protected FirestoreLoadable(DocumentSnapshot doc){
        this.docRef = doc.getReference();
        id = doc.getId();
        setData(doc);
        loaded = true;
    }

    public DocumentReference getDocRef() {
        return docRef;
    }
    protected abstract void setData(DocumentSnapshot doc);

    public Task<Void> save(){
        return docRef.set(this.toMap());
    }
    protected abstract HashMap<String, Object> toMap();

    public boolean isLoaded() {
        return loaded;
    }
    public String getId() {
        return id;
    }


}
