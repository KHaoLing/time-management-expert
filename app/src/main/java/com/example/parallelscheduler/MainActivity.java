package com.example.parallelscheduler;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parallelscheduler.data.TaskRepository;
import com.example.parallelscheduler.engine.SchedulerEngine;
import com.example.parallelscheduler.model.SchedulerTask;
import com.example.parallelscheduler.model.TaskType;
import com.example.parallelscheduler.ui.SchedulerTimelineView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final SchedulerEngine engine = new SchedulerEngine();
    private TaskRepository repository;
    private final List<SchedulerTask> tasks = new ArrayList<>();
    private SchedulerTimelineView timelineView;
    private LinearLayout summaryPanel;
    private int nextTaskNumber = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TaskRepository(this);
        tasks.addAll(repository.loadTasks());
        nextTaskNumber = tasks.size() + 1;
        buildUi();
        refresh();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("并行排程");
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(24);
        title.setTextColor(Color.rgb(28, 32, 44));
        title.setPadding(dp(16), dp(14), dp(16), dp(2));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("原生 Android 实现：任务数据、排程算法、时间轴绘制分开写。拖动任务块即可改时间。");
        subtitle.setTextSize(13);
        subtitle.setTextColor(Color.rgb(89, 96, 112));
        subtitle.setPadding(dp(16), 0, dp(16), dp(10));
        root.addView(subtitle);

        HorizontalScrollView buttonScroller = new HorizontalScrollView(this);
        buttonScroller.setHorizontalScrollBarEnabled(false);
        LinearLayout buttonBar = new LinearLayout(this);
        buttonBar.setOrientation(LinearLayout.HORIZONTAL);
        buttonBar.setPadding(dp(12), dp(4), dp(12), dp(10));

        buttonBar.addView(makeButton("自动排程", v -> {
            engine.autoSchedule(tasks);
            saveAndRefresh("已按前置关系和人工占用自动排程");
        }));
        buttonBar.addView(makeButton("新增前置", v -> addTask(TaskType.PREDECESSOR)));
        buttonBar.addView(makeButton("新增持续", v -> addTask(TaskType.CONTINUOUS)));
        buttonBar.addView(makeButton("新增等待", v -> addTask(TaskType.WAITING)));
        buttonBar.addView(makeButton("删除最后", v -> removeLastTask()));
        buttonBar.addView(makeButton("重置样例", v -> resetTasks()));
        buttonScroller.addView(buttonBar);
        root.addView(buttonScroller);

        timelineView = new SchedulerTimelineView(this);
        timelineView.setOnTaskChangedListener(() -> saveAndRefresh("已保存拖动后的时间"));
        ScrollView timelineScroller = new ScrollView(this);
        timelineScroller.addView(timelineView, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));
        root.addView(timelineScroller, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        summaryPanel = new LinearLayout(this);
        summaryPanel.setOrientation(LinearLayout.VERTICAL);
        summaryPanel.setPadding(dp(16), dp(8), dp(16), dp(14));
        root.addView(summaryPanel);

        setContentView(root);
    }

    private Button makeButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(13);
        button.setTextColor(Color.rgb(38, 47, 68));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), 0, dp(12), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(242, 245, 251));
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), Color.rgb(221, 227, 238));
        button.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
        );
        lp.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(lp);
        button.setOnClickListener(listener);
        return button;
    }

    private void addTask(TaskType type) {
        String id = "task_" + System.currentTimeMillis();
        int lastEnd = 0;
        for (SchedulerTask task : tasks) lastEnd = Math.max(lastEnd, task.endMinute());
        int work = type == TaskType.WAITING ? 2 : 5;
        int wait = type == TaskType.WAITING ? 8 : 0;
        SchedulerTask task = new SchedulerTask(id, type.label + nextTaskNumber, type, lastEnd, work, wait);
        if (!tasks.isEmpty()) task.dependencyIds.add(tasks.get(tasks.size() - 1).id);
        tasks.add(task);
        nextTaskNumber++;
        saveAndRefresh("已新增" + type.label + "，默认依赖上一个任务");
    }

    private void removeLastTask() {
        if (tasks.isEmpty()) return;
        SchedulerTask removed = tasks.remove(tasks.size() - 1);
        for (SchedulerTask task : tasks) task.dependencyIds.remove(removed.id);
        saveAndRefresh("已删除最后一个任务");
    }

    private void resetTasks() {
        repository.clear();
        tasks.clear();
        tasks.addAll(repository.loadTasks());
        nextTaskNumber = tasks.size() + 1;
        saveAndRefresh("已恢复样例任务");
    }

    private void saveAndRefresh(String message) {
        repository.saveTasks(tasks);
        refresh();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void refresh() {
        timelineView.setTasks(tasks);
        summaryPanel.removeAllViews();

        int totalEnd = 0;
        int humanBusy = 0;
        for (SchedulerTask task : tasks) {
            totalEnd = Math.max(totalEnd, task.endMinute());
            humanBusy += task.workMinutes;
        }
        TextView summary = new TextView(this);
        summary.setText(String.format(Locale.CHINA, "共 %d 个任务 · 总完成时间约 %d 分钟 · 人工占用约 %d 分钟", tasks.size(), totalEnd, humanBusy));
        summary.setTextSize(13);
        summary.setTextColor(Color.rgb(68, 75, 92));
        summaryPanel.addView(summary);

        TextView hint = new TextView(this);
        hint.setText("说明：等待任务的渐白部分表示人可以离开去做其他任务；前置关系不使用占长度的箭头。正式版可继续加编辑任务、冲突提示、导入导出。 ");
        hint.setTextSize(12);
        hint.setTextColor(Color.rgb(105, 112, 128));
        hint.setPadding(0, dp(4), 0, 0);
        summaryPanel.addView(hint);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
