package com.collabolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.collabolab.CustomDialogs.LoadDialog;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;

public class Register extends AppCompatActivity {
    TextView warning;
    EditText emailET;
    EditText phoneET;
    EditText passwordET;
    EditText confirmPasswordET;
    EditText nameET;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        warning = findViewById(R.id.WarningTV);
        emailET = findViewById(R.id.emailET);
        phoneET = findViewById(R.id.phoneET);
        passwordET = findViewById(R.id.passwordET);
        confirmPasswordET = findViewById(R.id.ConfirmPasswordET);
        nameET = findViewById(R.id.nameET);
        findViewById(R.id.registerButton).setOnClickListener(v -> {
            signUp(emailET.getText().toString(), phoneET.getText().toString(), passwordET.getText().toString(), confirmPasswordET.getText().toString(), nameET.getText().toString());
        });

    }
    public void signUp(String email, String phone, String password, String confirmPassword, String name) {
        String emailRegex ="(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        String phoneRegex ="^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
        warning.setText("");
        if (!password.equals(confirmPassword)) {
            warning.setText("Passwords do not match");
            return;
        }
        if (password.length() < 8) {
            warning.setText("Password must be at least 8 characters");
            return;
        }
        if (name.length() < 3) {
            warning.setText("Name must be at least 3 characters");
            return;
        }
        if (!phone.matches(phoneRegex)) {
            warning.setText("Invalid phone number \n example +1234567890");
            return;
        }
        if (!email.matches(emailRegex)) {
            warning.setText("Invalid email \n example example@gmail.com");
            return;
        }
        LoadDialog loadDialog = new LoadDialog (this);
        loadDialog.setMessage("Creating account...");
        loadDialog.show();
        FirebaseTools.functions.getHttpsCallable("createUser").call(new HashMap<String, Object>() {{
            put("email", email);
            put("password", password);
            put("name", name);
            put("phone", phone);
        }}).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loadDialog.setMessage("Signing in...");
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        loadDialog.setMessage("Loading user data...");
                        FirebaseTools.firestore.collection("users").document(uid).get().addOnCompleteListener(task2 -> {
                            loadDialog.dismiss();
                            DocumentSnapshot userDoc = task2.getResult();
                            User user = new User(userDoc);
                            User.currentUser = user;
                            Intent home = new Intent(this, SharedLayout.class);
                            startActivity(home);
                            finish();
                        });

                    } else {
                        loadDialog.dismiss();
                        warning.setText("Error in auth: " + task1.getException().getMessage());
                    }
                });
            } else {
                loadDialog.dismiss();
                warning.setText("Error in functions: " + task.getException().getMessage());
                task.getException().printStackTrace();
            }
        });


    }
}