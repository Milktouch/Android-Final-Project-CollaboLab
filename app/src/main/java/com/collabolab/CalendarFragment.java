package com.collabolab;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.collabolab.CustomDialogs.LoadDialog;
import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Receivers.AlarmNotificationReceiver;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.Utilities.GeneralUtilities;
import com.collabolab.Utilities.TaskCombiner;
import com.collabolab.model.TaskStatus;
import com.collabolab.model.WorkTask;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.type.DateTime;

import org.w3c.dom.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;


public class CalendarFragment extends CustomFragment {


    AlarmManager alarmManager;
    LinearLayout taskList;

    TextView noTasks;

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    ArrayList<WorkTask> workTaskList = new ArrayList<>();

    int margin = 10;

    public CalendarFragment() {
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
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        margin= GeneralUtilities.dpToPixelsInt(margin,getResources());
        LoadDialog loadDialog = new LoadDialog(getContext());
        loadDialog.setMessage("Loading Tasks...");
        loadDialog.show();
        taskList = view.findViewById(R.id.taskList);
        taskList.removeAllViews();
        noTasks = container.findViewById(R.id.noTasksText);
        alarmManager= (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        FirebaseTools.functions.getHttpsCallable("getUserTasks").call().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                loadDialog.dismiss();
                Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
                return;
            }
            HashMap<String, Object> data = (HashMap<String, Object>) task.getResult().getData();
            ArrayList<String> taskPaths = (ArrayList<String>) data.get("tasks");

            ArrayList<Task<DocumentSnapshot>> tasklist = new ArrayList<>();
            for (int i = 0; i < taskPaths.size(); i++) {
                tasklist.add(FirebaseTools.firestore.document(taskPaths.get(i)).get());
            }
            Tasks.whenAllComplete(tasklist).addOnCompleteListener(t -> {
                for (Task t2 : tasklist) {
                    if (!t2.isSuccessful()) continue;
                    DocumentSnapshot doc = (DocumentSnapshot) t2.getResult();
                    WorkTask workTask = new WorkTask(doc);
                    workTaskList.add(workTask);
                }
                sortTasks();
                for (WorkTask wt : workTaskList) {
                    createSmallTaskCard(wt);
                }
                loadDialog.dismiss();
            });
        });
    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.calendarBtn;
    }

    @Override
    public String getTitle() {
        return "Calendar";
    }

    private void setAlarm(String taskId,String task, long time){
        Intent intent = new Intent(getContext(), AlarmNotificationReceiver.class);
        intent.setAction("taskReminderAction-"+taskId);
        intent.putExtra("taskName", task);
        requestNotificationPermission();
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(getContext(), 1, intent, PendingIntent.FLAG_MUTABLE));

    }
    private void createSmallTaskCard(WorkTask task){
        View taskCard = getLayoutInflater().inflate(R.layout.small_task_card, null);
        TextView taskName = taskCard.findViewById(R.id.taskNameTV);
        TextView taskDate = taskCard.findViewById(R.id.dueDateTv);
        ImageView taskStatus = taskCard.findViewById(R.id.taskStatusColor);
        taskName.setText(task.name);
        taskDate.setText(formatter.format(task.deadline.toDate()));
        if (task.status.equals(TaskStatus.TO_DO)){
            taskStatus.setColorFilter(getResources().getColor(R.color.status_todo,null));
        }
        if (task.status.equals(TaskStatus.IN_PROGRESS)){
            taskStatus.setColorFilter(getResources().getColor(R.color.status_in_progress,null));
        }
        if (task.status.equals(TaskStatus.ON_HOLD)){
            taskStatus.setColorFilter(getResources().getColor(R.color.status_on_hold,null));
        }
        if (task.status.equals(TaskStatus.PENDING_REVIEW)){
            taskStatus.setColorFilter(getResources().getColor(R.color.status_review,null));
        }
        if (task.status.equals(TaskStatus.COMPLETE)) {
            taskStatus.setColorFilter(getResources().getColor(R.color.status_complete, null));
        }
        taskCard.setOnClickListener(v -> {
            pickReminderTime(task);
        });
        taskList.addView(taskCard);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) taskCard.getLayoutParams();
        params.setMargins(margin, margin, margin, margin);
        taskCard.setLayoutParams(params);
    }

    private void pickReminderTime(WorkTask task){
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
        datePickerDialog.setOnDateSetListener((view1, year, month, dayOfMonth) -> {
            GregorianCalendar calendar = new GregorianCalendar(year, month, dayOfMonth);
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view2, hourOfDay, minute) -> {
                calendar.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(GregorianCalendar.MINUTE, minute);
                setAlarm(task.getId(),task.name, calendar.getTimeInMillis());
            }, 0, 0, true);
            timePickerDialog.show();
        });
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }


    private void sortTasks(){
        workTaskList.sort(Comparator.comparing(o -> o.deadline));
    }

    private void requestNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

    }
}