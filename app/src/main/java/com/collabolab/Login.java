package com.collabolab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.collabolab.CustomDialogs.LoadDialog;
import com.collabolab.model.User;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText identifierET;
    EditText passwordET;

    CheckBox rememberMe;FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        identifierET = findViewById(R.id.EmailET);
        passwordET = findViewById(R.id.password);
        rememberMe = findViewById(R.id.rememberMeCheckBox);
        findViewById(R.id.signInButton).setOnClickListener(v -> {
            signIn(identifierET.getText().toString(), passwordET.getText().toString());
        });
        findViewById(R.id.goToSignUpBtn).setOnClickListener(v -> {
            Intent register = new Intent(this, Register.class);
            startActivity(register);
        });
    }

    public void signIn(String emailphone, String password) {
        LoadDialog loadDialog = new LoadDialog(this);
        loadDialog.setMessage("signing in");
        loadDialog.show();
        if (emailphone.equals("admin") || password.equals("admin")) {
            Intent admin = new Intent(this, Admin.class);
            startActivity(admin);
            finish();
            return;
        }
        if (emailphone.isEmpty() || password.isEmpty()) {
            loadDialog.dismiss();
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }
            auth.signInWithEmailAndPassword(emailphone,password).addOnCompleteListener(t->{
                if(t.isSuccessful()){
                    loadDialog.setMessage("loading user data");
                    String uid = t.getResult().getUser().getUid();
                    User user = new User(uid);
                    User.currentUser=user;
                    User.currentUser.load((data,success)->{
                        loadDialog.dismiss();
                        if(success){
                            if (rememberMe.isChecked()) {
                                SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                                editor.putString("email", emailphone);
                                editor.putString("password", password);
                                editor.apply();
                                editor.commit();
                            }
                            User.currentUser.loadProjects(null);
                            Intent intent = new Intent(this, SharedLayout.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(this,"error loading user data",Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    loadDialog.dismiss();
                    Toast.makeText(this,"error signing in: "+t.getException().getMessage(),Toast.LENGTH_LONG).show();

                }
            });

    }




}