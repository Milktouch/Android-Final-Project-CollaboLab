package com.collabolab;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Fragments.CustomTaskFragment;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.PermissionSet;
import com.collabolab.model.Project;
import com.collabolab.model.TaskStatus;
import com.collabolab.model.User;
import com.collabolab.model.WorkTask;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;


public class TaskViewFragment extends CustomTaskFragment {
    boolean newTask=false;
    ImageView editBtn;
    EditText taskTitle;
    EditText taskNotes;
    TextView taskDueDate;
    AutoCompleteTextView taskAssignee;
    TextView discardChangesBtn, saveChangesBtn;
    TextView logsBtn;
    ArrayList<String> members = new ArrayList<>();
    ArrayList<String> memberId = new ArrayList<>();
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Date dueDate;

    public TaskViewFragment() {
        super();
        currentTask = WorkTask.currentTask;
    }
    public TaskViewFragment(boolean newTask) {
        super();
        this.newTask = newTask;
        currentTask = new WorkTask(FirebaseTools.firestore.collection("projects").document(currentProject.getId()).collection("tasks").document());
        WorkTask.currentTask = currentTask;
        currentTask.assignedTo = new User(User.NULL_USER_ID);
        currentTask.deadline = Timestamp.now();
        currentTask.status = TaskStatus.TO_DO;
        currentTask.name = "New Task";
        currentTask.description = "Task Description";
        currentTask.timestamp = Timestamp.now();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parent.titleText.setText("Task");
        editBtn = view.findViewById(R.id.editTaskBtn);
        taskTitle = view.findViewById(R.id.taskNameET);
        taskNotes = view.findViewById(R.id.taskNotesET);
        taskDueDate = view.findViewById(R.id.taskDueDateET);
        taskAssignee = view.findViewById(R.id.taskAssigneeET);
        members.add(currentProject.owner.name);
        memberId.add(currentProject.owner.getId());
        currentProject.members.forEach(member -> {
            if (member.getUserInfo().isLoaded()) {
                members.add(member.getUserInfo().name);
                memberId.add(member.getUserInfo().getId());
            }
            else {
                member.getUserInfo().load(((data, success) -> {
                    members.add(member.getUserInfo().name);
                    memberId.add(member.getUserInfo().getId());
                }));
            }
        });
        discardChangesBtn = view.findViewById(R.id.discardChangesBtn);
        saveChangesBtn = view.findViewById(R.id.saveChangesBtn);
        logsBtn = view.findViewById(R.id.viewLogsBtn);
        setTaskInfo(currentTask);

        editBtn.setOnClickListener(v -> {
            if (currentTask.status.equals(TaskStatus.COMPLETE))
                Toast.makeText(getContext(), "You cannot edit a completed task", Toast.LENGTH_LONG).show();
            else if (PermissionSet.userPermissions.hasPermission(PermissionSet.PERMISSION_EDIT_TASK)||newTask)
                startEditing(); //enable editing
            else
                Toast.makeText(getContext(), "You do not have permission to edit this task", Toast.LENGTH_LONG).show();
        });

        saveChangesBtn.setOnClickListener(v -> {
            saveTaskChanges();
            // Disable editing
            stopEditing();
        });

        discardChangesBtn.setOnClickListener(v -> {
            // Disable editing
            stopEditing();
            setTaskInfo(currentTask);
        });

        logsBtn.setOnClickListener(v -> {
            parent.setFragment(new TaskLogsFragment());
        });

        taskDueDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((view1, year, month, dayOfMonth) -> {
                String date = "";
                if (dayOfMonth < 10) {
                    date += "0";
                }
                date += dayOfMonth + "/";
                if (month < 9) {
                    date += "0";
                }
                date += (month + 1) + "/" + year;
                taskDueDate.setText(date);
                dueDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
            });
            int year = Integer.parseInt(taskDueDate.getText().toString().split("/")[2]);
            int month = Integer.parseInt(taskDueDate.getText().toString().split("/")[1]);
            int day = Integer.parseInt(taskDueDate.getText().toString().split("/")[0]);
            datePickerDialog.updateDate(year, month - 1, day);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });


    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        return "Task";
    }

    public void setTaskInfo(WorkTask task) {
        taskTitle.setText(task.name);
        taskNotes.setText(task.description);

        taskDueDate.setText(formatter.format(task.deadline.toDate()));
        taskAssignee.setText(task.assignedTo.name);
        dueDate = task.deadline.toDate();
    }
    public void saveTaskChanges() {
        HashMap<String,Pair<String,String>> changes = new HashMap<>();
        String newTitle = taskTitle.getText().toString();
        String newNotes = taskNotes.getText().toString();
        String newAssignee = taskAssignee.getText().toString();
        int index = members.indexOf(newAssignee);
        if (index == -1) {
            Toast.makeText(getContext(), "Invalid assignee", Toast.LENGTH_LONG).show();
            return;
        }
        String assigneeId = memberId.get(index);
        if(newTask){
            currentTask.assignedTo = new User(assigneeId);
            currentTask.deadline = new Timestamp(dueDate);
            currentTask.name = newTitle;
            currentTask.description = newNotes;
            currentTask.timestamp = Timestamp.now();
            currentTask.save();
            currentTask.logChanges("Task created", User.currentUser);
            newTask = false;
            HashMap<String, Object> data = new HashMap<>();
            data.put("userId", assigneeId);
            data.put("title", "New Task");
            data.put("description", "\""+newTitle+"\" task has been assigned to you in \""+currentProject.getName()+"\" project");
            FirebaseTools.functions.getHttpsCallable("notifyUser").call(data);
            Toast.makeText(getContext(), "Task created", Toast.LENGTH_LONG).show();
            return;
        }
        StringBuilder log = new StringBuilder();
        if (!newTitle.equals(currentTask.name)) {
            log.append("Task has been renamed from ").append(currentTask.name).append(" to ").append(newTitle).append("\n");
            currentTask.name = newTitle;
        }
        if (!newNotes.equals(currentTask.description)) {
            log.append("Task notes have been updated").append(" from: ").append(currentTask.description).append(" to: ").append(newNotes).append("\n");
            currentTask.description = newNotes;
        }
        if (dueDate.getTime()!=currentTask.deadline.toDate().getTime()) {
            log.append("Task due date has been updated").append(" from: ").append(formatter.format(currentTask.deadline.toDate())).append(" to: ").append(dueDate).append("\n");
            currentTask.deadline = new Timestamp(dueDate);
        }
        if (!assigneeId.equals(currentTask.assignedTo.getId())) {
            log.append("Task has been re-assigned").append(" from: ").append(currentTask.assignedTo.name).append(" to: ").append(newAssignee).append("\n");
            currentTask.assignedTo = new User(assigneeId);
            currentTask.assignedTo.load();
            HashMap<String, Object> data = new HashMap<>();
            data.put("userId", assigneeId);
            data.put("title", "New Task Assigned");
            data.put("description", "You have been assigned a new task: " + newTitle);
            FirebaseTools.functions.getHttpsCallable("notifyUser").call(data);
        }
        // Save changes to database
        currentTask.save();
        // Log changes
        currentTask.logChanges(log.toString(), User.currentUser);
        HashMap<String, Object> data = new HashMap<>();
        data.put("taskId", currentTask.getId());
        data.put("projectId", log.toString());
        FirebaseTools.functions.getHttpsCallable("updateTask").call(data);
        Toast.makeText(getContext(), "Task information updated", Toast.LENGTH_LONG).show();


    }
    public void startEditing() {
        taskTitle.setEnabled(true);
        taskNotes.setEnabled(true);
        taskDueDate.setEnabled(true);
        taskAssignee.setEnabled(true);
        taskAssignee.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, members));
        discardChangesBtn.setVisibility(View.VISIBLE);
        saveChangesBtn.setVisibility(View.VISIBLE);
    }
    public void stopEditing() {
        taskTitle.setEnabled(false);
        taskNotes.setEnabled(false);
        taskDueDate.setEnabled(false);
        taskAssignee.setEnabled(false);
        discardChangesBtn.setVisibility(View.GONE);
        saveChangesBtn.setVisibility(View.GONE);
    }

}