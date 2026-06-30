package com.example.parallelscheduler.engine;

import com.example.parallelscheduler.model.SchedulerTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchedulerEngine {
    private static class BusyRange {
        int start;
        int end;
        BusyRange(int start, int end) { this.start = start; this.end = end; }
    }

    public void autoSchedule(List<SchedulerTask> tasks) {
        Map<String, SchedulerTask> byId = new HashMap<>();
        for (SchedulerTask task : tasks) byId.put(task.id, task);

        List<SchedulerTask> ordered = topoSort(tasks, byId);
        List<BusyRange> occupied = new ArrayList<>();

        for (SchedulerTask task : ordered) {
            int earliest = 0;
            for (String depId : task.dependencyIds) {
                SchedulerTask dep = byId.get(depId);
                if (dep != null) earliest = Math.max(earliest, dep.endMinute());
            }

            int candidate = earliest;
            while (true) {
                int occStart = candidate;
                int occEnd = candidate + task.workMinutes;
                BusyRange conflict = firstConflict(occupied, occStart, occEnd);
                if (conflict == null) break;
                candidate = conflict.end;
            }
            task.startMinute = candidate;
            occupied.add(new BusyRange(task.occupiedStartMinute(), task.occupiedEndMinute()));
        }
    }

    private BusyRange firstConflict(List<BusyRange> ranges, int start, int end) {
        for (BusyRange range : ranges) {
            if (start < range.end && end > range.start) return range;
        }
        return null;
    }

    private List<SchedulerTask> topoSort(List<SchedulerTask> tasks, Map<String, SchedulerTask> byId) {
        List<SchedulerTask> result = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (SchedulerTask task : tasks) visit(task, byId, visiting, visited, result);
        return result;
    }

    private void visit(SchedulerTask task, Map<String, SchedulerTask> byId, Set<String> visiting, Set<String> visited, List<SchedulerTask> result) {
        if (visited.contains(task.id)) return;
        if (visiting.contains(task.id)) return; // 避免循环依赖导致卡死；正式版本可在这里给用户报错。
        visiting.add(task.id);
        for (String depId : task.dependencyIds) {
            SchedulerTask dep = byId.get(depId);
            if (dep != null) visit(dep, byId, visiting, visited, result);
        }
        visiting.remove(task.id);
        visited.add(task.id);
        result.add(task);
    }
}
