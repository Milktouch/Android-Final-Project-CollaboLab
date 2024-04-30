package com.collabolab.model;


//T must be inherited from FirestoreLoadable
public interface OnDataLoadedListener<T extends FirestoreLoadable> {
    public void onDataLoaded(T data, boolean success);
}
