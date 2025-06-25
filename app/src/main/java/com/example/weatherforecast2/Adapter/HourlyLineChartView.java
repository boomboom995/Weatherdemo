package com.example.weatherforecast2.Adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;

public class HourlyLineChartView extends View {
    private JSONArray hourlyArray;
    private Paint linePaint, pointPaint, textPaint, gridPaint;
    private int[] temps;
    private String[] times;
    private int hours = 0;

    public HourlyLineChartView(Context context) { 
        super(context); 
        init(); 
    }
    
    public HourlyLineChartView(Context context, AttributeSet attrs) { 
        super(context, attrs); 
        init(); 
    }
    
    public HourlyLineChartView(Context context, AttributeSet attrs, int defStyleAttr) { 
        super(context, attrs, defStyleAttr); 
        init(); 
    }

    private void init() {
        // 折线画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.CYAN);
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);
        
        // 数据点画笔
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);
        
        // 文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // 网格线画笔
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(100);
    }

    public void setHourlyArray(JSONArray hourlyArray) {
        this.hourlyArray = hourlyArray;
        if (hourlyArray != null) {
            hours = hourlyArray.length();
            temps = new int[hours];
            times = new String[hours];
            
            for (int i = 0; i < hours; i++) {
                JSONObject hour = hourlyArray.optJSONObject(i);
                if (hour != null) {
                    String tempStr = hour.optString("temp", "0").replaceAll("[^0-9]", "");
                    String timeStr = hour.optString("time", "00");
                    
                    try {
                        temps[i] = Integer.parseInt(tempStr);
                        times[i] = timeStr.length() >= 2 ? timeStr.substring(0, 2) + ":00" : timeStr + ":00";
                    } catch (Exception e) {
                        temps[i] = 0;
                        times[i] = "00:00";
                    }
                } else {
                    temps[i] = 0;
                    times[i] = "00:00";
                }
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hourlyArray == null || hours == 0) return;

        int width = getWidth();
        int height = getHeight();
        int padding = 60;
        int chartHeight = height - 2 * padding;
        int chartWidth = width - 2 * padding;
        
        // 找出温度范围
        int maxTemp = Integer.MIN_VALUE;
        int minTemp = Integer.MAX_VALUE;
        for (int i = 0; i < hours; i++) {
            if (temps[i] > maxTemp) maxTemp = temps[i];
            if (temps[i] < minTemp) minTemp = temps[i];
        }
        
        int tempRange = Math.max(1, maxTemp - minTemp);
        if (tempRange < 10) tempRange = 10; // 最小范围10度
        
        float xStep = chartWidth * 1f / Math.max(1, hours - 1);
        
        // 绘制网格线
        drawGrid(canvas, padding, chartWidth, chartHeight);
        
        // 绘制折线
        Path path = new Path();
        for (int i = 0; i < hours; i++) {
            float x = padding + i * xStep;
            float y = padding + chartHeight * (1 - (temps[i] - minTemp) * 1f / tempRange);
            
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, linePaint);
        
        // 绘制数据点和温度标签
        for (int i = 0; i < hours; i++) {
            float x = padding + i * xStep;
            float y = padding + chartHeight * (1 - (temps[i] - minTemp) * 1f / tempRange);
            
            // 绘制数据点
            canvas.drawCircle(x, y, 6, pointPaint);
            
            // 绘制温度标签
            canvas.drawText(temps[i] + "°", x, y - 20, textPaint);
            
            // 绘制时间标签（底部，只显示部分以避免拥挤）
            if (i % 3 == 0 || i == hours - 1) {
                canvas.drawText(times[i], x, height - 20, textPaint);
            }
        }
    }
    
    private void drawGrid(Canvas canvas, int padding, int chartWidth, int chartHeight) {
        // 绘制水平网格线（温度刻度）
        for (int i = 0; i <= 4; i++) {
            float y = padding + chartHeight * i / 4f;
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint);
        }
        
        // 绘制垂直网格线（时间刻度）
        for (int i = 0; i <= 6; i++) {
            float x = padding + chartWidth * i / 6f;
            canvas.drawLine(x, padding, x, padding + chartHeight, gridPaint);
        }
    }
} 