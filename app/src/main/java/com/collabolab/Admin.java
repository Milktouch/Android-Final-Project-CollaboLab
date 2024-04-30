package com.collabolab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.CustomDialogs.LoadDialog;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class Admin extends AppCompatActivity {

    private User selectedUser;

     FirebaseFirestore db = FirebaseFirestore.getInstance();
     ArrayList<View> views = new ArrayList<View>();
     ArrayList<User> viewsData = new ArrayList<User>();
     LinearLayout layout ;
     EditText search ;

     private int margin = 10;

     private ArrayList<Pair<String,String>> userEmailPass = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        margin = GeneralUtilities.dpToPixelsInt(margin,getResources());
        search = findViewById(R.id.adminUserSearchET);
        layout = findViewById(R.id.adminUserContainer);
        initViews();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (int i = 0; i < viewsData.size(); i++){
                    if (viewsData.get(i).name.toLowerCase().contains(s) || viewsData.get(i).email.toLowerCase().contains(s)){
                        views.get(i).setVisibility(View.VISIBLE);
                    }else{
                        views.get(i).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void initViews(){
        db.collection("users").get().addOnCompleteListener(t->{
            if (t.isSuccessful()){
                for (DocumentSnapshot doc : t.getResult()){
                    createUserCard(doc);
                }
            }
        });
    }
    private void createUserCard(DocumentSnapshot doc){
        if (doc.getId().equals(User.NULL_USER_ID))
            return;
        View view = getLayoutInflater().inflate(R.layout.admin_user_view, null);
        views.add(view);
        User user = new User(doc);
        viewsData.add(user);
        userEmailPass.add(new Pair<>(doc.getString("email"), doc.getString("password")));
        layout.addView(view);
        ((TextView)view.findViewById(R.id.usernameAdminTV)).setText(doc.getString("name"));
        ((TextView)view.findViewById(R.id.emailAdminTV)).setText(doc.getString("email"));
        ImageView menu = view.findViewById(R.id.adminUserMenu);
        menu.setTag(viewsData.size()-1);
        registerForContextMenu(menu);
        view.setOnClickListener(v->{
            User u = viewsData.get((int)v.getTag());
            selectedUser = u;
            openContextMenu(v);
        });
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = margin;
        params.bottomMargin = margin;
        view.setLayoutParams(params);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_user_menu,menu);
        User u = viewsData.get((int)v.getTag());
        selectedUser = u;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.adminDeleteUserMI){
            LoadDialog dialog = new LoadDialog(this);
            dialog.setMessage("Deleting User...");
            dialog.show();
            HashMap<String, Object> data = new HashMap<>();
            data.put("userId", selectedUser.getId());
            FirebaseTools.functions.getHttpsCallable("deleteUser").call(data).addOnCompleteListener(t->{
                dialog.dismiss();
                if (t.isSuccessful()){
                    Toast.makeText(this, "User Deleted", Toast.LENGTH_SHORT).show();
                    layout.removeAllViews();
                    views.clear();
                    viewsData.clear();
                    initViews();
                }else{
                    Toast.makeText(this, "Error Deleting User" + t.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (item.getItemId() == R.id.adminLoginAsMI){
            SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
            Pair<String, String> emailPass = userEmailPass.get(viewsData.indexOf(selectedUser));
            editor.putString("email", emailPass.first);
            editor.putString("password", emailPass.second);
            editor.putBoolean("isSingular", true);
            editor.commit();
            startActivity(new Intent(this,SplashScreen.class));
        }
        return super.onContextItemSelected(item);
    }
}