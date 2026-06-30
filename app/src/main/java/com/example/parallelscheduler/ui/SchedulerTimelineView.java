package com.example.parallelscheduler.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.parallelscheduler.model.SchedulerTask;
import com.example.parallelscheduler.model.TaskType;

import java.util.ArrayList;
import java.util.List;

public class SchedulerTimelineView extends View {
    public interface OnTaskChangedListener {
        void onTaskChanged();
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<SchedulerTask> tasks = new ArrayList<>();
    private OnTaskChangedListener listener;

    private float minuteWidth;
    private float rowHeight;
    private float leftPadding;
    private float topPadding;
    private float blockHeight;
    private SchedulerTask draggingTask;
    private float lastX;

    public SchedulerTimelineView(Context context) {
        super(context);
        init();
    }

    public SchedulerTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);
        textPaint.setColor(Color.rgb(30, 34, 45));
        textPaint.setTextSize(sp(13));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));
        borderPaint.setColor(Color.rgb(96, 108, 132));
    }

    public void setTasks(List<SchedulerTask> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        requestLayout();
        invalidate();
    }

    public void setOnTaskChangedListener(OnTaskChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = (int) (dp(82) + tasks.size() * dp(64) + dp(24));
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        minuteWidth = Math.max(dp(7), (getWidth() - dp(96)) / 45f);
        rowHeight = dp(64);
        leftPadding = dp(72);
        topPadding = dp(66);
        blockHeight = dp(36);

        drawHeader(canvas);
        for (int i = 0; i < tasks.size(); i++) drawTask(canvas, tasks.get(i), i);
        drawLegend(canvas);
    }

    private void drawHeader(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(247, 248, 252));
        canvas.drawRect(0, 0, getWidth(), dp(50), paint);

        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(sp(17));
        canvas.drawText("并行排程 · 原生 Android 版", dp(16), dp(30), textPaint);
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(sp(12));
        textPaint.setColor(Color.rgb(95, 102, 118));
        canvas.drawText("拖动任务块调整时间；等待段可并行做别的事", dp(16), dp(47), textPaint);
        textPaint.setColor(Color.rgb(30, 34, 45));

        paint.setStrokeWidth(dp(1));
        for (int minute = 0; minute <= 45; minute += 5) {
            float x = leftPadding + minute * minuteWidth;
            paint.setColor(Color.rgb(225, 229, 238));
            canvas.drawLine(x, dp(58), x, getHeight() - dp(12), paint);
            textPaint.setTextSize(sp(11));
            textPaint.setColor(Color.rgb(90, 96, 110));
            canvas.drawText(minute + "m", x - dp(8), dp(62), textPaint);
        }
        textPaint.setColor(Color.rgb(30, 34, 45));
    }

    private void drawTask(Canvas canvas, SchedulerTask task, int index) {
        float y = topPadding + index * rowHeight;
        textPaint.setTextSize(sp(13));
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.rgb(35, 40, 55));
        canvas.drawText((index + 1) + ". " + task.title, dp(10), y + dp(22), textPaint);
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(sp(11));
        textPaint.setColor(Color.rgb(105, 112, 128));
        canvas.drawText(task.type.label, dp(10), y + dp(40), textPaint);

        float startX = leftPadding + task.startMinute * minuteWidth;
        float endX = leftPadding + task.endMinute() * minuteWidth;
        float rectTop = y + dp(12);
        RectF rect = new RectF(startX, rectTop, Math.max(startX + dp(18), endX), rectTop + blockHeight);

        if (task.type == TaskType.WAITING) {
            float workEndX = leftPadding + (task.startMinute + task.workMinutes) * minuteWidth;
            RectF workRect = new RectF(startX, rectTop, Math.max(startX + dp(16), workEndX), rectTop + blockHeight);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(null);
            paint.setColor(Color.rgb(67, 176, 112));
            canvas.drawRoundRect(workRect, dp(9), dp(9), paint);

            RectF waitRect = new RectF(workEndX, rectTop, rect.right, rectTop + blockHeight);
            if (waitRect.width() > dp(2)) {
                paint.setShader(new LinearGradient(waitRect.left, 0, waitRect.right, 0,
                        Color.rgb(138, 214, 166), Color.WHITE, Shader.TileMode.CLAMP));
                canvas.drawRoundRect(waitRect, dp(9), dp(9), paint);
                paint.setShader(null);
            }
            canvas.drawRoundRect(rect, dp(9), dp(9), borderPaint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(null);
            paint.setColor(task.type == TaskType.PREDECESSOR ? Color.rgb(246, 194, 80) : Color.rgb(89, 145, 235));
            canvas.drawRoundRect(rect, dp(9), dp(9), paint);
            canvas.drawRoundRect(rect, dp(9), dp(9), borderPaint);
        }

        textPaint.setTextSize(sp(12));
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(task.title + "  " + task.startMinute + "-" + task.endMinute() + "m", rect.left + dp(8), rect.centerY() + dp(4), textPaint);
        textPaint.setFakeBoldText(false);

        if (!task.dependencyIds.isEmpty()) {
            textPaint.setTextSize(sp(10));
            textPaint.setColor(Color.rgb(95, 102, 118));
            canvas.drawText("前置: " + task.dependencyIds.size() + " 个", rect.right + dp(6), rectTop + dp(22), textPaint);
        }
    }

    private void drawLegend(Canvas canvas) {
        float y = getHeight() - dp(14);
        textPaint.setTextSize(sp(11));
        textPaint.setColor(Color.rgb(90, 96, 110));
        canvas.drawText("颜色：黄=前置，蓝=持续占用，绿+渐白=等待任务（白色部分人可以离开）", dp(12), y, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                draggingTask = hitTest(event.getX(), event.getY());
                lastX = event.getX();
                return draggingTask != null || super.onTouchEvent(event);
            case MotionEvent.ACTION_MOVE:
                if (draggingTask != null) {
                    float dx = event.getX() - lastX;
                    int deltaMinutes = Math.round(dx / minuteWidth);
                    if (deltaMinutes != 0) {
                        draggingTask.startMinute = Math.max(0, draggingTask.startMinute + deltaMinutes);
                        lastX = event.getX();
                        invalidate();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (draggingTask != null) {
                    draggingTask = null;
                    if (listener != null) listener.onTaskChanged();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private SchedulerTask hitTest(float x, float y) {
        if (minuteWidth <= 0) return null;
        for (int i = 0; i < tasks.size(); i++) {
            SchedulerTask task = tasks.get(i);
            float rowY = topPadding + i * rowHeight;
            float left = leftPadding + task.startMinute * minuteWidth;
            float right = leftPadding + task.endMinute() * minuteWidth;
            RectF rect = new RectF(left, rowY + dp(12), Math.max(left + dp(18), right), rowY + dp(12) + blockHeight);
            if (rect.contains(x, y)) return task;
        }
        return null;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
