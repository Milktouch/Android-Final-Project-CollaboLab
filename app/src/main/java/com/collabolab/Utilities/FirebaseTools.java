package com.collabolab.Utilities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

public class FirebaseTools {

    public static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static final FirebaseFunctions functions = FirebaseFunctions.getInstance("europe-west1");

    public static void init() {
        //functions.useEmulator("10.0.2.2", 5001);
    }


}
