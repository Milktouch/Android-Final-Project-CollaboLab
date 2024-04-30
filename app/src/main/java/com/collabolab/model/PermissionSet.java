package com.collabolab.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class PermissionSet extends FirestoreLoadable{
    public static final String PERMISSION_CREATE_TASK = "create task";
    public static final String PERMISSION_DELETE_TASK = "delete task";
    public static final String PERMISSION_EDIT_TASK = "edit task";
    public static final String PERMISSION_REVIEW_TASK = "review task";
    public static final String PERMISSION_INVITE = "invite";
    public static final String PERMISSION_MANAGE_PERMISSIONS = "manage permissions";
    public static final String PERMISSION_KICK_MEMBER = "kick member";

    public static PermissionSet userPermissions;

    private HashMap<String, Boolean> permissions = new HashMap<>();
    public PermissionSet(DocumentReference docRef) {
        super(docRef);
    }

    public PermissionSet(DocumentSnapshot doc) {
        super(doc);
    }

    @Override
    protected void setData(DocumentSnapshot doc) {
        Map<String, Object> map = doc.getData();
        for (String key : map.keySet()) {
            permissions.put(key, (Boolean) map.get(key));
        }
    }

    @Override
    protected HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : permissions.keySet()) {
            map.put(key, permissions.get(key));
        }
        return map;
    }

    public boolean hasPermission(String permission) {
        return permissions.containsKey(permission) && permissions.get(permission);
    }

    public void setPermission(String permission, boolean value) {
        permissions.put(permission, value);
    }

    public void togglePermission(String permission) {
        if (permissions.containsKey(permission))
            permissions.put(permission, !permissions.get(permission));
        else
            permissions.put(permission, true);

    }
}
