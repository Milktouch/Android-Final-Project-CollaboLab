package com.collabolab;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Services.NotificationService;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.Project;
import com.collabolab.model.User;
import com.collabolab.model.UserUpdates;
import com.collabolab.model.WorkTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Stack;

public class SharedLayout extends AppCompatActivity {
    public static SharedLayout parentActivity;
    int fragmentId;
    CustomFragment currentFragment;

    FragmentContainerView fragmentContainer;
    TextView titleText;
    private FragmentManager fragmentManager;

    ImageButton activeButton;
    public ImageButton calendarBtn,homeBtn, profileBtn, projectsBtn;



    //custom stacks
    Stack<ImageButton> buttonStack = new Stack<>();
    public Stack<CustomFragment> fragmentStack = new Stack<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserUpdates.startListening(User.currentUser);
        startService(new Intent(this, NotificationService.class));
        subscribeToTopics();
        parentActivity = this;
        setContentView(R.layout.activity_shared_layout);
        fragmentId = R.id.fragment;
        fragmentContainer = findViewById(fragmentId);
        currentFragment = (CustomFragment) getSupportFragmentManager().findFragmentById(fragmentId);
        titleText=findViewById(R.id.titleText);
        fragmentManager = getSupportFragmentManager();

        calendarBtn = findViewById(R.id.calendarBtn);
        homeBtn = findViewById(R.id.homeBtn);
        profileBtn = findViewById(R.id.profileBtn);
        projectsBtn = findViewById(R.id.projectBtn);

        //on calendar button click
        calendarBtn.setOnClickListener(view ->{
            setFragment(new CalendarFragment());
        });
        //on home button click
        homeBtn.setOnClickListener(view ->{
            setFragment(new HomeFragment());
        });
        //on projects button click
        projectsBtn.setOnClickListener(view ->{
            setFragment(new ProjectFragment());
        });
        //on settings button click
        profileBtn.setOnClickListener(view ->{
            setFragment(new UserProfileFragment());
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        activeButton = homeBtn;
        fragmentManager.addFragmentOnAttachListener((fragmentManager, fragment) -> {
            Log.d("Fragment", "onAttachFragment: "+fragment.getClass().getName());
            if(fragment instanceof CustomFragment){
                CustomFragment customFragment = (CustomFragment) fragment;
                activeButton = customFragment.getActiveButton();
                titleText.setText(customFragment.getTitle());
                setActiveButton(activeButton);
            }
        });
    }
    public void refreshFragment(){
        fragmentManager.beginTransaction().detach(currentFragment).attach(currentFragment).commit();
    }
    public void setActiveButton(ImageButton btn){
        Resources r = getResources();
        //set all buttons to inactive
        homeBtn.setPadding(0, GeneralUtilities.dpToPixelsInt(10,r),0,0);
        homeBtn.setBackgroundColor(r.getColor(R.color.nav_menu_color,null));

        calendarBtn.setPadding(0,GeneralUtilities.dpToPixelsInt(10,r),0,0);
        calendarBtn.setBackgroundColor(r.getColor(R.color.nav_menu_color,null));

        projectsBtn.setPadding(0,GeneralUtilities.dpToPixelsInt(10,r),0,0);
        projectsBtn.setBackgroundColor(r.getColor(R.color.nav_menu_color,null));

        profileBtn.setPadding(0,GeneralUtilities.dpToPixelsInt(10,r),0,0);
        profileBtn.setBackgroundColor(r.getColor(R.color.nav_menu_color,null));

        //set active button
        btn.setPadding(0,0,0,0);
        btn.setBackgroundColor(r.getColor(R.color.app_color,null));

        activeButton = btn;
    }
    public static void setTitle(String title){
        parentActivity.titleText.setText(title);
    }

    public void sendFCMToken(String token){
        User.currentUser.getDocRef().update("fcmToken", token);
    }

    public void setFragment(CustomFragment fragment){

        if(fragment.getClass().equals(currentFragment.getClass())){
            refreshFragment();
            return;
        }
        fragmentStack.push(currentFragment);
        currentFragment = fragment;
        buttonStack.push(activeButton);
        setActiveButton(fragment.getActiveButton());
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(fragmentId,fragment)
                .commit();
        titleText.setText(fragment.getTitle());
    }
    public void setFragmentNoBackStack(CustomFragment fragment){
        if(fragment.getClass().equals(currentFragment.getClass())){
            refreshFragment();
            return;
        }
        currentFragment = fragment;
        activeButton = fragment.getActiveButton();
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(fragmentId,fragment)
                .commit();
        titleText.setText(fragment.getTitle());
    }

    public void goBack(){
        if(buttonStack.isEmpty()){
            finish();
        }else{
            ImageButton btn = buttonStack.pop();
            setActiveButton(btn);
            currentFragment = fragmentStack.pop();
            titleText.setText(currentFragment.getTitle());
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(fragmentId,currentFragment)
                    .commit();
        }
    }

    public void logout(){
        User.currentUser.projects.forEach(project -> {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(project.getId());
        });
        UserUpdates.resetInstance();
        User.currentUser.getDocRef().update("fcmToken", "");
        User.currentUser = null;
        WorkTask.currentTask = null;
        Project.currentProject = null;
        //      웃
        FirebaseAuth.getInstance().signOut();
        getSharedPreferences("login", MODE_PRIVATE).edit().clear().commit();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void subscribeToTopics(){
        User.currentUser.projects.forEach(project -> {
            FirebaseMessaging.getInstance().subscribeToTopic(project.getId());
        });

    }

}