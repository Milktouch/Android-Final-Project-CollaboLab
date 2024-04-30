package com.collabolab;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.User;

public class UserProfileFragment extends CustomFragment {

    String phoneRegex ="^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
    ImageView qrCodeBtn,editBtn,copyBtn;
    TextView saveBtn,discardBtn,logoutBtn;
    EditText nameET,phoneET;
    TextView emailTV,userIdTV,helloTV;

    LinearLayout logoutLayout;

    public UserProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //dialogWidth = GeneralUtilities.dpToPixels(300,getResources());
        //dialogHeight = GeneralUtilities.dpToPixels(300,getResources());
        qrCodeBtn = view.findViewById(R.id.qrCodeIV);
        editBtn = view.findViewById(R.id.editAccountBtn);
        copyBtn = view.findViewById(R.id.copyIdBtn);
        saveBtn = view.findViewById(R.id.saveChangesBtn);
        discardBtn = view.findViewById(R.id.discardChangesBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        nameET = view.findViewById(R.id.usernameET);
        phoneET = view.findViewById(R.id.phoneNumberET);
        emailTV = view.findViewById(R.id.emailTV);
        userIdTV = view.findViewById(R.id.userIdTV);
        helloTV = view.findViewById(R.id.helloTV);
        logoutLayout = view.findViewById(R.id.logoutLayout);

        applyUserInfo();

        // Set the onClickListeners
        qrCodeBtn.setOnClickListener(v -> {
            // Open the QR code fragment
            try {
                Bitmap qrCode = GeneralUtilities.generateQrCode(User.currentUser.getId());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Your QR Code");
                View qrCodeView = getLayoutInflater().inflate(R.layout.qrcodedialog,null);
                ImageView qrCodeIV = qrCodeView.findViewById(R.id.qrCodeIV);
                qrCodeIV.setImageBitmap(qrCode);
                builder.setView(qrCodeView);
                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
            }
        });

        editBtn.setOnClickListener(v -> {
            enableEdit();
        });
        saveBtn.setOnClickListener(v -> {
            if(nameET.getText().toString().isEmpty() || phoneET.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Name and Phone number cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!phoneET.getText().toString().matches(phoneRegex)){
                Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            User.currentUser.name = nameET.getText().toString();
            User.currentUser.phone = phoneET.getText().toString();
            User.currentUser.save();
            disableEdit();
            applyUserInfo();
        });
        discardBtn.setOnClickListener(v -> {
            disableEdit();
            applyUserInfo();
        });
        logoutBtn.setOnClickListener(v -> {
            SharedLayout.parentActivity.logout();
        });
        copyBtn.setOnClickListener(v -> {
            GeneralUtilities.copyToClipboard(getContext(),User.currentUser.getId(),"User ID");
            Toast.makeText(getContext(), "User ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });



    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.profileBtn;
    }

    @Override
    public String getTitle() {
        return "Profile";
    }

    private void applyUserInfo(){
        emailTV.setText(User.currentUser.email);
        nameET.setText(User.currentUser.name);
        phoneET.setText(User.currentUser.phone);
        userIdTV.setText(User.currentUser.getId());
        helloTV.setText("Hello, "+User.currentUser.name.split(" ")[0]);
    }

    private void enableEdit(){
        nameET.setEnabled(true);
        phoneET.setEnabled(true);
        saveBtn.setVisibility(View.VISIBLE);
        discardBtn.setVisibility(View.VISIBLE);
        logoutLayout.setVisibility(View.GONE);
    }
    private void disableEdit(){
        nameET.setEnabled(false);
        phoneET.setEnabled(false);
        saveBtn.setVisibility(View.GONE);
        discardBtn.setVisibility(View.GONE);
        logoutLayout.setVisibility(View.VISIBLE);
    }
}