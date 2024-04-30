package com.collabolab;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.User;

public class ViewProfileFragment extends CustomFragment {
    private User user;

    TextView usernameTV, emailTV, phoneTV, userIdTV;
    ImageView qrBtn,copyBtn;


    public ViewProfileFragment() {
        // Required empty public constructor
    }
    public ViewProfileFragment(User user) {
        this.user = user;
        user.load();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usernameTV = view.findViewById(R.id.usernameTV);
        emailTV = view.findViewById(R.id.emailTV);
        phoneTV = view.findViewById(R.id.phoneNumberTV);
        userIdTV = view.findViewById(R.id.userIdTV);
        qrBtn = view.findViewById(R.id.qrCodeIV);
        copyBtn = view.findViewById(R.id.copyIdBtn);

        applyUserDetails();

        qrBtn.setOnClickListener(v -> {
            try {
                Bitmap qrCode = GeneralUtilities.generateQrCode(user.getId());
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
        copyBtn.setOnClickListener(v -> {
            GeneralUtilities.copyToClipboard(getContext(),user.getId(),"User ID");
            Toast.makeText(getContext(), "User ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.profileBtn;
    }

    @Override
    public String getTitle() {
        return user.name.split(" ")[0]+ "'s Profile";
    }

    private void applyUserDetails(){
        usernameTV.setText(user.name);
        emailTV.setText(user.email);
        phoneTV.setText(user.phone);
        userIdTV.setText(user.getId());
    }
}