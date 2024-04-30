package com.collabolab.Services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.collabolab.SharedLayout;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {
    private static NotificationService instance;
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    private static String fcmToken;
    public static String getToken() {
        return fcmToken;
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCMTokenService", "Refreshed token: " + token);
        sendRegistrationToServer(token);
        fcmToken = token;
    }


    public void sendRegistrationToServer(String token) {
        if (User.currentUser == null) return;
        if (SharedLayout.parentActivity == null) return;
        SharedLayout.parentActivity.sendFCMToken(token);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d("FCMTokenService", "onCreate");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCMTokenService", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    fcmToken = task.getResult();
                    sendRegistrationToServer(fcmToken);

                    // Log and toast
                    Log.d("FCMTokenService", "Token: " + fcmToken);
                });
        
    }
}
