package com.example.parallelscheduler.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SchedulerTask {
    public final String id;
    public String title;
    public TaskType type;
    public int startMinute;
    public int workMinutes;
    public int waitMinutes;
    public final List<String> dependencyIds = new ArrayList<>();

    public SchedulerTask(String id, String title, TaskType type, int startMinute, int workMinutes, int waitMinutes) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.startMinute = startMinute;
        this.workMinutes = Math.max(1, workMinutes);
        this.waitMinutes = Math.max(0, waitMinutes);
    }

    public int totalMinutes() {
        return type == TaskType.WAITING ? workMinutes + waitMinutes : workMinutes;
    }

    public int endMinute() {
        return startMinute + totalMinutes();
    }

    public int occupiedStartMinute() {
        return startMinute;
    }

    public int occupiedEndMinute() {
        return startMinute + workMinutes;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("type", type.name());
        obj.put("startMinute", startMinute);
        obj.put("workMinutes", workMinutes);
        obj.put("waitMinutes", waitMinutes);
        JSONArray deps = new JSONArray();
        for (String dep : dependencyIds) deps.put(dep);
        obj.put("dependencyIds", deps);
        return obj;
    }

    public static SchedulerTask fromJson(JSONObject obj) throws JSONException {
        SchedulerTask task = new SchedulerTask(
                obj.getString("id"),
                obj.getString("title"),
                TaskType.valueOf(obj.getString("type")),
                obj.getInt("startMinute"),
                obj.getInt("workMinutes"),
                obj.optInt("waitMinutes", 0)
        );
        JSONArray deps = obj.optJSONArray("dependencyIds");
        if (deps != null) {
            for (int i = 0; i < deps.length(); i++) {
                task.dependencyIds.add(deps.getString(i));
            }
        }
        return task;
    }
}
