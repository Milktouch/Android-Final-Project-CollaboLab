package com.collabolab;

import android.graphics.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.Fragments.CustomProjectFragment;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.Project;
import com.collabolab.model.SearchUser;
import com.collabolab.model.User;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;


public class InviteFragment extends CustomProjectFragment {


    User selectedUser;

    EditText collabuddySearch;

    LinearLayout collabuddyList;

    LinearLayout selectedCollabuddy;

    TextView usernameTV, emailTV,userIdTV;

    ImageView scanQrIV;

    TextView inviteBtn;

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    collabuddySearch.setText(result.getContents());
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        collabuddyList = view.findViewById(R.id.searchResultLL);
        collabuddySearch = view.findViewById(R.id.searchET);
        selectedCollabuddy = view.findViewById(R.id.userInviteCardLL);
        usernameTV = view.findViewById(R.id.usernameTV);
        emailTV = view.findViewById(R.id.userEmailTV);
        userIdTV = view.findViewById(R.id.userIdTV);
        scanQrIV = view.findViewById(R.id.qrCodeIV);
        inviteBtn = view.findViewById(R.id.inviteBtn);

        collabuddySearch.addTextChangedListener(new TextWatcher() {
            Timer timer = new Timer();
            final int delay = 700;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                queryUser(collabuddySearch.getText().toString());
                            }
                        },
                        delay
                );
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        scanQrIV.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a QR code");
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setBarcodeImageEnabled(true);
            options.setOrientationLocked(false);
            barcodeLauncher.launch(options);
        });
    }


    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        return currentProject.getName();
    }


    public void queryUser(String text){
        if (text.isEmpty()||text.replaceAll(" ","").equals(" ")) {
            return;
        }
        FirebaseTools.firestore.collection("users").document(text.replaceAll(" ","")).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if (task.getResult().exists()) {
                    User user = new User(task.getResult());
                    ArrayList<String> projectIds = (ArrayList<String>) task.getResult().get("projects");
                    for (String projId : projectIds) {
                        if (projId.equals(currentProject.getId())) {
                            queryCloudFunction(text);
                            return;
                        }
                    }
                    SearchUser searchUser = new SearchUser(task.getResult().getString("name"), task.getResult().getString("email"), task.getResult().getId());
                    applyUserDetails(searchUser);
                } else {
                    queryCloudFunction(text);
                }
            }
            else {
                queryCloudFunction(text);
            }
        });

    }

    public void displaySearchedUsers(ArrayList<HashMap<String,String>> users){
        selectedCollabuddy.setVisibility(View.GONE);
        collabuddyList.removeAllViews();
        for(HashMap<String,String> user : users){
            String name = user.get("name");
            String email = user.get("email");
            String userId = user.get("id");
            SearchUser searchUser = new SearchUser(name,email,userId);
            View userView = getLayoutInflater().inflate(R.layout.small_user_card,null);
            collabuddyList.addView(userView);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userView.getLayoutParams();
            params.setMargins(10,10,10,10);
            TextView userInfoTV = userView.findViewById(R.id.usernameTV);
            userInfoTV.setText(name + " \n(" + email + ")");
            userView.setOnClickListener(v -> {
                applyUserDetails(searchUser);
            });

        }
    }

    public void applyUserDetails(SearchUser user){
        selectedUser = new User(user.getUserId());
        selectedCollabuddy.setVisibility(View.VISIBLE);
        usernameTV.setText(user.getName());
        emailTV.setText(user.getEmail());
        userIdTV.setText(user.getUserId());
        inviteBtn.setOnClickListener(v -> {
            HashMap<String,Object> data = new HashMap<>();
            data.put("projectId",currentProject.getId());
            data.put("userId",user.getUserId());
            FirebaseTools.functions.getHttpsCallable("inviteUser").call(data);
            Toast.makeText(getContext(),"Invite has been sent",Toast.LENGTH_SHORT).show();
            parent.goBack();
        });
    }

    public void queryCloudFunction(String text){
        HashMap<String,String> textMap = new HashMap<>();
        textMap.put("text",text.toLowerCase());
        textMap.put("projectId",currentProject.getId());
        FirebaseTools.functions.getHttpsCallable("searchUser").call(textMap).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                HashMap<String,Object> result = (HashMap<String, Object>) task1.getResult().getData();
                ArrayList<HashMap<String,String>> users = (ArrayList<HashMap<String, String>>) result.get("users");
                displaySearchedUsers(users);
            }
        });
    }
}