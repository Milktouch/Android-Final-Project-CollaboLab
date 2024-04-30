package com.collabolab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;


import com.collabolab.Receivers.AlarmNotificationReceiver;
import com.collabolab.Services.NotificationService;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.FirestoreLoadable;
import com.collabolab.model.OnDataLoadedListener;
import com.collabolab.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean signInFinished = false;
    private boolean signInFailed = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationChannel channel = new NotificationChannel(AlarmNotificationReceiver.notificationChannelId, AlarmNotificationReceiver.notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(AlarmNotificationReceiver.notificationChannelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        setContentView(R.layout.activity_splash_screen);
        FirebaseTools.init();
        signIn();

        requestNotificationPermission();
        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    while (!signInFinished){
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent;
                    if(!signInFailed){
                        intent = new Intent(SplashScreen.this, SharedLayout.class);
                    }else{
                        intent = new Intent(SplashScreen.this, Login.class);
                    }
                    startActivity(intent);
                    finish();
                }
            }
        };
        thread.start();

    }
    private void signIn(){
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        String email = preferences.getString("email", "");
        String password = preferences.getString("password", "");
        boolean isSingular = preferences.getBoolean("isSingular", false);
        if(isSingular){
            preferences.edit().clear().apply();
        }
        if(email.equals("")||password.equals("")){
            signInFinished=true;
            signInFailed=true;
            return;
        }

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(t->{
            if(t.isSuccessful()){
                String uid = t.getResult().getUser().getUid();
                User.currentUser = new User(uid);
                User.currentUser.load(new OnDataLoadedListener() {
                    @Override
                    public void onDataLoaded(FirestoreLoadable data, boolean success) {
                        if(success){
                            signInFinished=true;
                            signInFailed=false;
                        }else{
                            signInFinished=true;
                            signInFailed=true;
                        }
                    }
                });
                User.currentUser.loadProjects(null);
            }else{
                Toast.makeText(this,"error signing in",Toast.LENGTH_LONG).show();

            }
        });
    }
    private void requestNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

    }

}