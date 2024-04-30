package com.collabolab.Utilities;

import com.google.android.gms.tasks.Task;

public class TaskCombiner {
    private Thread thread;
    private OnTasksComplete listener;

    private Task[] tasks;

    public TaskCombiner(Task[] tasks, OnTasksComplete listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void start() {
        thread = new Thread(() -> {
            while (true) {
                boolean allComplete = true;
                for (Task task : tasks) {
                    if (!task.isComplete()) {
                        allComplete = false;
                        break;
                    }
                }
                if (allComplete) {
                    if (listener != null)
                        listener.onTasksComplete();
                    break;
                }
            }
        });
        thread.start();
    }
    public void start(OnTasksComplete listener) {
        this.listener = listener;
        start();
    }
}
