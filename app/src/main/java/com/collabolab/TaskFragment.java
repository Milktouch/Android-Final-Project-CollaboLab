package com.collabolab;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.collabolab.Fragments.CustomProjectFragment;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.model.PermissionSet;
import com.collabolab.model.Project;
import com.collabolab.model.TaskStatus;
import com.collabolab.model.User;
import com.collabolab.model.WorkTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class TaskFragment extends CustomProjectFragment {
    LinearLayout todoFilter;
    LinearLayout inProgressFilter;
    LinearLayout onHoldFilter;
    LinearLayout pendingReviewFilter;
    LinearLayout completedFilter;
    ArrayList<WorkTask> tasks;
    ArrayList<View> taskViews = new ArrayList<>();

    LinearLayout currentFilterLL=null;
    String currentFilter = "";
    int marginUnselected = 0;
    int marginSelected = 0;

    LinearLayout taskContainer;

    LinearLayout AddTaskBtn;

    LinearLayout filterName;
    TextView filterNameTV;

    int margins;
    public TaskFragment() {
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
        return inflater.inflate(R.layout.fragment_task, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        margins=GeneralUtilities.dpToPixelsInt(10, getResources());
        todoFilter = view.findViewById(R.id.taskFilterTodo);
        inProgressFilter = view.findViewById(R.id.taskFilterInProgress);
        onHoldFilter = view.findViewById(R.id.taskFilterOnHold);
        pendingReviewFilter = view.findViewById(R.id.taskFilterPendingReview);
        completedFilter = view.findViewById(R.id.taskFilterComplete);
        taskContainer = view.findViewById(R.id.taskList);
        filterName = view.findViewById(R.id.taskFilterNameLL);
        filterNameTV = view.findViewById(R.id.taskFilterNameTV);
        todoFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(todoFilter);
            }
        });
        inProgressFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(inProgressFilter);
            }
        });
        onHoldFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(onHoldFilter);
            }
        });
        pendingReviewFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(pendingReviewFilter);
            }
        });
        completedFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(completedFilter);
            }
        });
        marginSelected= GeneralUtilities.dpToPixelsInt(-20, getResources());
        marginUnselected= GeneralUtilities.dpToPixelsInt(-40, getResources());
        currentProject.loadTasks(()->{
            initTasks();
        });

        AddTaskBtn = view.findViewById(R.id.addTaskBtn);
        if (PermissionSet.userPermissions.hasPermission(PermissionSet.PERMISSION_CREATE_TASK)){
            AddTaskBtn.setVisibility(View.VISIBLE);
            AddTaskBtn.setOnClickListener(v -> {
                parent.setFragment(new TaskViewFragment(true));
            });
        }
        else {
            AddTaskBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        if (currentProject!=null)
            return currentProject.getName();
        return Project.currentProject.getName();
    }


    public void setFilter(LinearLayout newFilter){
        if(currentFilterLL!=null){
            unselectImageFilter(currentFilterLL);
        }
        if(currentFilterLL==newFilter) {
            currentFilterLL = null;
            currentFilter = "";
            filterName.setVisibility(View.GONE);
        }
        else{
            selectImageFilter(newFilter);
            currentFilterLL = newFilter;
            if(newFilter==todoFilter){
                currentFilter = TaskStatus.TO_DO;
                filterNameTV.setTextColor(getResources().getColor(R.color.status_todo,null));
            }
            else if(newFilter==inProgressFilter){
                currentFilter = TaskStatus.IN_PROGRESS;
                filterNameTV.setTextColor(getResources().getColor(R.color.status_in_progress,null));
            }
            else if(newFilter==onHoldFilter){
                currentFilter = TaskStatus.ON_HOLD;
                filterNameTV.setTextColor(getResources().getColor(R.color.status_on_hold,null));
            }
            else if(newFilter==pendingReviewFilter){
                currentFilter = TaskStatus.PENDING_REVIEW;
                filterNameTV.setTextColor(getResources().getColor(R.color.status_review,null));
            }
            else if(newFilter==completedFilter){
                currentFilter = TaskStatus.COMPLETE;
                filterNameTV.setTextColor(getResources().getColor(R.color.status_complete,null));
            }
            filterName.setVisibility(View.VISIBLE);
            filterNameTV.setText(currentFilter);
        }
        filterTasks(currentFilter);


    }
    public void unselectImageFilter(LinearLayout filter){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) filter.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(marginSelected, marginUnselected);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
                filter.setLayoutParams(params);
            }
        });
        animator.setDuration(300);
        animator.start();
    }
    public void selectImageFilter(LinearLayout filter){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) filter.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(marginUnselected, marginSelected);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
                filter.setLayoutParams(params);
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    public void filterTasks(String filter){
        for (int i = 0; i < tasks.size(); i++) {
            WorkTask task = tasks.get(i);
            if(task.status.equals(filter) || filter.equals("")){
                taskViews.get(i).setVisibility(View.VISIBLE);
            }else {
                taskViews.get(i).setVisibility(View.GONE);
            }
        }
    }
    public void initTasks()
    {
        tasks = currentProject.tasks;
        for (int i = 0; i < tasks.size(); i++) {
            WorkTask task = tasks.get(i);
            createTaskView(task);
        }
    }

    public View createTaskView(WorkTask task){
        View taskView = getLayoutInflater().inflate(R.layout.task_view, null);
        taskContainer.addView(taskView);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) taskView.getLayoutParams();
        params.setMargins(0, margins, 0, margins);
        TextView taskName = taskView.findViewById(R.id.taskNameTV);
        taskName.setText(task.name);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        TextView dueDate = taskView.findViewById(R.id.dueDateTv);
        dueDate.setText(formatter.format(task.deadline.toDate()));
        TextView assignedTo = taskView.findViewById(R.id.assigneeTv);
        if(task.assignedTo.isLoaded())
            assignedTo.setText(task.assignedTo.name);
        else
            task.assignedTo.load((data,success) -> {
                if (success)
                    assignedTo.setText(task.assignedTo.name);
            });

        TextView action1 = taskView.findViewById(R.id.actionBtn1);
        TextView action2 = taskView.findViewById(R.id.actionBtn2);

        ImageView statusIcon = taskView.findViewById(R.id.taskStatusColor);
        boolean isCurrentUser = task.assignedTo.getId().equals(User.currentUser.getId());
        if(task.status.equals(TaskStatus.TO_DO)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_todo,null));
            if (isCurrentUser){
                action1.setText("Start Working");
                action1.getBackground().setTint(getResources().getColor(R.color.status_in_progress,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.IN_PROGRESS;
                    task.save();
                    updateTaskView(task,taskView);
                    String log = "Task has been started.";
                    task.logChanges(log,User.currentUser);
                });
                action2.setVisibility(View.INVISIBLE);
                action2.setOnClickListener(null);
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.IN_PROGRESS)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_in_progress,null));
            if (isCurrentUser){
                action1.setText("Put on Hold");
                action1.getBackground().setTint(getResources().getColor(R.color.status_on_hold,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.ON_HOLD;
                    task.save();
                    updateTaskView(task,taskView);
                    String log = "Task has been put on hold.";
                    task.logChanges(log,User.currentUser);
                });
                action2.setText("Send for Review");
                action2.getBackground().setTint(getResources().getColor(R.color.status_review,null));
                action2.setOnClickListener(v -> {
                    task.status = TaskStatus.PENDING_REVIEW;
                    task.save();
                    updateTaskView(task,taskView);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("projectId",currentProject.getId());
                    FirebaseTools.functions.getHttpsCallable("sendTaskToReview").call(data);
                    String log = "Task has been sent for review.";
                    task.logChanges(log,User.currentUser);
                });
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.ON_HOLD)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_on_hold,null));
            if (isCurrentUser){
                action1.setText("Resume Work");
                action1.getBackground().setTint(getResources().getColor(R.color.status_in_progress,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.IN_PROGRESS;
                    task.save();
                    updateTaskView(task,taskView);
                    String log = "Task has been resumed.";
                    task.logChanges(log,User.currentUser);
                });
                action2.setVisibility(View.INVISIBLE);
                action2.setOnClickListener(null);
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.PENDING_REVIEW)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_review,null));
            if (PermissionSet.userPermissions.hasPermission(PermissionSet.PERMISSION_REVIEW_TASK)){
                action1.setText("Approve");
                action1.getBackground().setTint(getResources().getColor(R.color.status_complete,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.COMPLETE;
                    task.save();
                    updateTaskView(task,taskView);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("taskId",task.getId());
                    data.put("projectId",currentProject.getId());
                    Log.d("TaskFragment", "Approve Task: "+task.getId()+" Project: "+currentProject.getId());
                    FirebaseTools.functions.getHttpsCallable("approveTask").call(data);
                });
                action2.setText("Send Back");
                action2.getBackground().setTint(getResources().getColor(R.color.status_on_hold,null));
                action2.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Send Task Back");
                    View dialogView = getLayoutInflater().inflate(R.layout.comment_dialog,null);
                    EditText commentET = dialogView.findViewById(R.id.notesET);
                    builder.setView(dialogView);
                    builder.setPositiveButton("Send Back", (dialog, which) -> {
                        task.status = TaskStatus.ON_HOLD;
                        task.save();
                        updateTaskView(task,taskView);
                        String comment = "Task has been sent back. \n Reviewers Notes: \n \n "+commentET.getText().toString();
                        task.logChanges(comment,User.currentUser);
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("userId",task.assignedTo.getId());
                        data.put("title","Task Update");
                        data.put("description","\""+task.name+"\" task has been sent back.");
                        FirebaseTools.functions.getHttpsCallable("notifyUser").call(data);
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.show();

                });
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.COMPLETE)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_complete,null));
            action1.setVisibility(View.GONE);
            action2.setVisibility(View.GONE);
        }
        taskViews.add(taskView);
        taskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkTask.currentTask = task;
                parent.setFragment(new TaskViewFragment());
            }
        });
        ImageView deleteBtn = taskView.findViewById(R.id.deleteTaskBtn);
        if (PermissionSet.userPermissions.hasPermission(PermissionSet.PERMISSION_DELETE_TASK)){
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Task");
                builder.setMessage("Are you sure you want to delete this task? \n This action cannot be undone.");
                builder.setPositiveButton("Delete", (dialog, which) -> {
                    task.getDocRef().delete();
                    taskContainer.removeView(taskView);
                    currentProject.tasks.remove(task);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();

            });
        }
        else {
            deleteBtn.setVisibility(View.GONE);
        }
        return taskView;
    }

    public void updateTaskView(WorkTask task,View taskView){
        TextView action1 = taskView.findViewById(R.id.actionBtn1);
        TextView action2 = taskView.findViewById(R.id.actionBtn2);
        action1.setVisibility(View.VISIBLE);
        action2.setVisibility(View.VISIBLE);
        ImageView statusIcon = taskView.findViewById(R.id.taskStatusColor);
        boolean isCurrentUser = task.assignedTo.getId().equals(User.currentUser.getId());
        if(task.status.equals(TaskStatus.TO_DO)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_todo,null));
            if (isCurrentUser){
                action1.setText("Start Working");
                action1.getBackground().setTint(getResources().getColor(R.color.status_in_progress,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.IN_PROGRESS;
                    task.save();
                    updateTaskView(task,taskView);
                    String log = "Task has been started.";
                    task.logChanges(log,User.currentUser);
                });
                action2.setVisibility(View.INVISIBLE);
                action2.setOnClickListener(null);
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.IN_PROGRESS)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_in_progress,null));
            if (isCurrentUser){
                action1.setText("Put on Hold");
                action1.getBackground().setTint(getResources().getColor(R.color.status_on_hold,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.ON_HOLD;
                    task.save();
                    updateTaskView(task,taskView);
                    String log = "Task has been put on hold.";
                    task.logChanges(log,User.currentUser);
                });
                action2.setText("Send for Review");
                action2.getBackground().setTint(getResources().getColor(R.color.status_review,null));
                action2.setOnClickListener(v -> {
                    task.status = TaskStatus.PENDING_REVIEW;
                    task.save();
                    updateTaskView(task,taskView);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("projectId",currentProject.getId());
                    FirebaseTools.functions.getHttpsCallable("sendTaskToReview").call(data);
                    String log = "Task has been sent for review.";
                    task.logChanges(log,User.currentUser);
                });
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.ON_HOLD)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_on_hold,null));
            if (isCurrentUser){
                action1.setText("Resume Work");
                action1.getBackground().setTint(getResources().getColor(R.color.status_in_progress,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.IN_PROGRESS;
                    task.save();
                    updateTaskView(task,taskView);
                    String log = "Task has been resumed.";
                    task.logChanges(log,User.currentUser);
                });
                action2.setVisibility(View.INVISIBLE);
                action2.setOnClickListener(null);
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.PENDING_REVIEW)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_review,null));
            if (PermissionSet.userPermissions.hasPermission(PermissionSet.PERMISSION_REVIEW_TASK)){
                action1.setText("Approve");
                action1.getBackground().setTint(getResources().getColor(R.color.status_complete,null));
                action1.setOnClickListener(v -> {
                    task.status = TaskStatus.COMPLETE;
                    task.save();
                    task.logChanges("Task has been approved.",User.currentUser);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("taskId",task.getId());
                    data.put("projectId",currentProject.getId());
                    FirebaseTools.functions.getHttpsCallable("approveTask").call(data);
                    updateTaskView(task,taskView);
                });
                action2.setText("Send Back");
                action2.getBackground().setTint(getResources().getColor(R.color.status_on_hold,null));
                action2.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Send Task Back");
                    View dialogView = getLayoutInflater().inflate(R.layout.comment_dialog,null);
                    EditText commentET = dialogView.findViewById(R.id.notesET);
                    builder.setView(dialogView);
                    builder.setPositiveButton("Send Back", (dialog, which) -> {
                        task.status = TaskStatus.ON_HOLD;
                        task.save();
                        updateTaskView(task,taskView);
                        String comment = "Task has been sent back. \n Reviewers Notes: \n \n "+commentET.getText().toString();
                        task.logChanges(comment,User.currentUser);
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("userId",task.assignedTo.getId());
                        data.put("title","Task Update");
                        data.put("description","\""+task.name+"\" task has been sent back.");
                        FirebaseTools.functions.getHttpsCallable("notifyUser").call(data);
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.show();

                });
            }
            else {
                action1.setVisibility(View.GONE);
                action2.setVisibility(View.GONE);
            }
        }
        else if(task.status.equals(TaskStatus.COMPLETE)){
            statusIcon.setColorFilter(getResources().getColor(R.color.status_complete,null));
            action1.setVisibility(View.GONE);
            action2.setVisibility(View.GONE);
        }
    }





}