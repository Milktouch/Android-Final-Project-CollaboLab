package com.collabolab.model;

import com.collabolab.Utilities.FirebaseTools;
import com.google.firebase.firestore.DocumentReference;

public class ProjectMember {
    private PermissionSet permissions;
    private User userInfo;

    public ProjectMember(User userInfo,Project proj) {
        this.userInfo = userInfo;
        DocumentReference docRef = proj.getDocRef().collection("permissions").document(userInfo.getId());
        this.permissions = new PermissionSet(docRef);
    }

    public ProjectMember(String userId, Project proj) {
        DocumentReference userRef = FirebaseTools.firestore.collection("users").document(userId);
        User user = new User(userRef);
        this.userInfo = user;
        DocumentReference docRef = proj.getDocRef().collection("permissions").document(userId);
        this.permissions = new PermissionSet(docRef);
    }

    public User getUserInfo() {
        return userInfo;
    }
    public PermissionSet getPermissions() {
        return permissions;
    }




}
