package com.example.parallelscheduler.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.parallelscheduler.model.SchedulerTask;
import com.example.parallelscheduler.model.TaskType;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static final String PREFS = "parallel_scheduler_prefs";
    private static final String KEY_TASKS = "tasks";

    private final SharedPreferences preferences;

    public TaskRepository(Context context) {
        this.preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<SchedulerTask> loadTasks() {
        String raw = preferences.getString(KEY_TASKS, null);
        if (raw == null) return defaultTasks();
        try {
            JSONArray array = new JSONArray(raw);
            List<SchedulerTask> tasks = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) tasks.add(SchedulerTask.fromJson(array.getJSONObject(i)));
            if (tasks.isEmpty()) return defaultTasks();
            return tasks;
        } catch (JSONException e) {
            return defaultTasks();
        }
    }

    public void saveTasks(List<SchedulerTask> tasks) {
        JSONArray array = new JSONArray();
        try {
            for (SchedulerTask task : tasks) array.put(task.toJson());
            preferences.edit().putString(KEY_TASKS, array.toString()).apply();
        } catch (JSONException ignored) {
        }
    }

    public void clear() {
        preferences.edit().remove(KEY_TASKS).apply();
    }

    private List<SchedulerTask> defaultTasks() {
        List<SchedulerTask> tasks = new ArrayList<>();
        SchedulerTask boil = new SchedulerTask("task_1", "烧水", TaskType.PREDECESSOR, 0, 5, 0);
        SchedulerTask wait = new SchedulerTask("task_2", "等水开", TaskType.WAITING, 5, 1, 10);
        wait.dependencyIds.add(boil.id);
        SchedulerTask trash = new SchedulerTask("task_3", "扔垃圾", TaskType.CONTINUOUS, 6, 6, 0);
        SchedulerTask noodle = new SchedulerTask("task_4", "煮面", TaskType.CONTINUOUS, 16, 4, 0);
        noodle.dependencyIds.add(wait.id);
        tasks.add(boil);
        tasks.add(wait);
        tasks.add(trash);
        tasks.add(noodle);
        return tasks;
    }
}
