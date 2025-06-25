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

public class TrendLineChartView extends View {
    private JSONArray forecastArray;
    private Paint linePaint, pointPaint, textPaint;
    private int[] highTemps, lowTemps;
    private int days = 0;

    public TrendLineChartView(Context context) { super(context); init(); }
    public TrendLineChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public TrendLineChartView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
    }

    public void setForecastArray(JSONArray forecastArray) {
        this.forecastArray = forecastArray;
        if (forecastArray != null) {
            days = forecastArray.length();
            highTemps = new int[days];
            lowTemps = new int[days];
            for (int i = 0; i < days; i++) {
                JSONObject day = forecastArray.optJSONObject(i);
                String high = day.optString("high", "0").replaceAll("[^0-9]", "");
                String low = day.optString("low", "0").replaceAll("[^0-9]", "");
                try {
                    highTemps[i] = Integer.parseInt(high);
                    lowTemps[i] = Integer.parseInt(low);
                } catch (Exception e) {
                    highTemps[i] = 0;
                    lowTemps[i] = 0;
                }
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (forecastArray == null || days == 0) return;
        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        int chartHeight = height - 2 * padding;
        int chartWidth = width - 2 * padding;
        int pointRadius = 10;
        int maxTemp = Integer.MIN_VALUE, minTemp = Integer.MAX_VALUE;
        for (int i = 0; i < days; i++) {
            if (highTemps[i] > maxTemp) maxTemp = highTemps[i];
            if (lowTemps[i] < minTemp) minTemp = lowTemps[i];
        }
        int tempRange = Math.max(1, maxTemp - minTemp);
        float xStep = chartWidth * 1f / (days - 1);
        // 画高温折线
        Path highPath = new Path();
        for (int i = 0; i < days; i++) {
            float x = padding + i * xStep;
            float y = padding + chartHeight * (1 - (highTemps[i] - minTemp) * 1f / tempRange);
            if (i == 0) highPath.moveTo(x, y);
            else highPath.lineTo(x, y);
        }
        linePaint.setColor(Color.YELLOW);
        canvas.drawPath(highPath, linePaint);
        // 画低温折线
        Path lowPath = new Path();
        for (int i = 0; i < days; i++) {
            float x = padding + i * xStep;
            float y = padding + chartHeight * (1 - (lowTemps[i] - minTemp) * 1f / tempRange);
            if (i == 0) lowPath.moveTo(x, y);
            else lowPath.lineTo(x, y);
        }
        linePaint.setColor(Color.CYAN);
        canvas.drawPath(lowPath, linePaint);
        // 画点和温度文字
        for (int i = 0; i < days; i++) {
            float x = padding + i * xStep;
            float yHigh = padding + chartHeight * (1 - (highTemps[i] - minTemp) * 1f / tempRange);
            float yLow = padding + chartHeight * (1 - (lowTemps[i] - minTemp) * 1f / tempRange);
            canvas.drawCircle(x, yHigh, pointRadius, pointPaint);
            canvas.drawCircle(x, yLow, pointRadius, pointPaint);
            canvas.drawText(highTemps[i] + "°", x - 20, yHigh - 16, textPaint);
            canvas.drawText(lowTemps[i] + "°", x - 20, yLow + 36, textPaint);
        }
    }
} 