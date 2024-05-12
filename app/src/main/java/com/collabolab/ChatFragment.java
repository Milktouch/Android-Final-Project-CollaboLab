package com.collabolab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Fragments.CustomProjectFragment;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.Project;
import com.collabolab.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class ChatFragment extends CustomProjectFragment {




    LinearLayout chatLayout;

    CollectionReference chatRef;

    Button sendBtn;
    EditText messageET;



    public ChatFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatRef = currentProject.getDocRef().collection("chat");
        chatLayout = container.findViewById(R.id.chatMessagesContainer);
        sendBtn = container.findViewById(R.id.sendMessageBtn);
        messageET = container.findViewById(R.id.chatMessageET);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageET.getText().toString();
                if(!message.isEmpty()){
                    sendNewMessage(message);
                }
            }
        });

        initChatMessages();
    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        return currentProject.getName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    public void sendNewMessage(String message){
        HashMap<String, Object> newMessage = new HashMap<>();
        newMessage.put("from", User.currentUser.name);
        newMessage.put("text", message);
        newMessage.put("projectId", currentProject.getId());
        messageET.setText("");
        FirebaseTools.functions.getHttpsCallable("sendChatMessage").call(newMessage);
    }

    public void initChatMessages(){
        chatRef.orderBy("timestamp").addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            if (SharedLayout.parentActivity.currentFragment != this){
                return;
            }
            for (int i = 0; i < value.getDocumentChanges().size(); i++) {
                String author = value.getDocumentChanges().get(i).getDocument().getString("from");
                String userId = value.getDocumentChanges().get(i).getDocument().getString("userId");
                String message = value.getDocumentChanges().get(i).getDocument().getString("text");
                if (userId.equals(User.currentUser.getId())){
                    createOwnChatMessage(message);
                }
                else{
                    createChatMessage(author, message);
                }
                //scroll to bottom
                chatLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView)chatLayout.getParent()).fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });

    }

    private void createOwnChatMessage(String message) {
        LinearLayout chatMessage = (LinearLayout) getLayoutInflater().inflate(R.layout.chat_message_user, null);
        //change margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 20, 15, 20);

        params.gravity = Gravity.RIGHT;
        chatMessage.setLayoutParams(params);
        chatLayout.addView(chatMessage);
        TextView messageTV = chatMessage.findViewById(R.id.messageTextTV);
        messageTV.setText(message);

    }

    public void createChatMessage(String author, String message){
        LinearLayout chatMessage = (LinearLayout) getLayoutInflater().inflate(R.layout.chat_message_other, null);
        //change margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 30, 15, 30);
        params.gravity = Gravity.LEFT;
        chatMessage.setLayoutParams(params);
        chatLayout.addView(chatMessage);
        TextView authorTV = chatMessage.findViewById(R.id.authorTV);
        TextView messageTV = chatMessage.findViewById(R.id.messageTextTV);
        authorTV.setText(author);
        messageTV.setText(message);

    }
}