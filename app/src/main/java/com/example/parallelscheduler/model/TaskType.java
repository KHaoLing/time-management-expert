package com.example.parallelscheduler.model;

public enum TaskType {
    PREDECESSOR("前置任务"),
    CONTINUOUS("持续任务"),
    WAITING("等待任务");

    public final String label;

    TaskType(String label) {
        this.label = label;
    }
}
