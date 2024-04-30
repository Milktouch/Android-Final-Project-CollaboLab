package com.collabolab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.collabolab.Fragments.CustomTaskFragment;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class TaskLogsFragment extends CustomTaskFragment {

    LinearLayout logList;
    TextView taskName;
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    int marginVertical = 0;

    public TaskLogsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_logs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logList = view.findViewById(R.id.logList);
        taskName = view.findViewById(R.id.taskNameTV);
        taskName.setText(currentTask.name);
        marginVertical = GeneralUtilities.dpToPixelsInt(5, getResources());
        initLogs();


    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        return "Task Logs";
    }

    private void initLogs(){
        logList.removeAllViews();
        currentTask.getDocRef().collection("logs").orderBy("timestamp").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                    addLog(task.getResult().getDocuments().get(i));
                }
            }
        });

    }

    private void addLog(DocumentSnapshot doc){
        String log = doc.getString("changes");
        String userId = doc.getString("changer");
        String timestamp = formatter.format(doc.getTimestamp("timestamp").toDate());
        View logView = getLayoutInflater().inflate(R.layout.log_card, null);
        logList.addView(logView);
        TextView logTV = logView.findViewById(R.id.logDescription);
        TextView userTV = logView.findViewById(R.id.loggerName);
        TextView timeTV = logView.findViewById(R.id.logDate);
        logTV.setText(log);
        String username = "";
        for (int i = 0; i < currentProject.members.size(); i++) {
            if (currentProject.members.get(i).getUserInfo().getId().equals(userId)) {
                username=currentProject.members.get(i).getUserInfo().name;
                break;
            }
        }
        if (username.equals("")) {
            User u = new User(userId);
            u.load((data,success) -> {
                if(success){
                    userTV.setText(u.name);
                }
            });
        }else {
            userTV.setText(username);
        }
        timeTV.setText(timestamp);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) logView.getLayoutParams();

        params.setMargins(0, marginVertical, 0, marginVertical);

    }
}