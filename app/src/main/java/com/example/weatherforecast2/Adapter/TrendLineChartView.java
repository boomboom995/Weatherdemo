package com.example.weatherforecast2.Adapter;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;

public class TrendLineChartView extends View {
    private JSONArray forecastArray;
    private Paint linePaint, fillPaint, pointPaint, pointStrokePaint, textPaint, gridPaint;
    private int[] highTemps, lowTemps;
    private int days = 0;
    private LinearGradient highGradient, lowGradient, highFillGradient, lowFillGradient;

    public TrendLineChartView(Context context) { super(context); init(); }
    public TrendLineChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public TrendLineChartView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        // 设置线条画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.StrokeJoin.ROUND);
        
        // 填充区域画笔
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        
        // 数据点画笔（填充）
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);
        
        // 数据点边框画笔
        pointStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointStrokePaint.setStyle(Paint.Style.STROKE);
        pointStrokePaint.setStrokeWidth(2f);
        pointStrokePaint.setColor(Color.WHITE);
        
        // 文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(2f, 1f, 1f, Color.argb(100, 0, 0, 0));
        
        // 网格画笔
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.argb(30, 255, 255, 255));
        gridPaint.setStrokeWidth(0.5f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));
    }

    public void setForecastArray(JSONArray forecastArray) {
        this.forecastArray = forecastArray;
        if (forecastArray != null) {
            days = Math.min(forecastArray.length(), 15); // 限制最多15天
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
        int padding = 60;
        int chartHeight = height - 2 * padding;
        int chartWidth = width - 2 * padding;
        
        // 计算温度范围
        int maxTemp = Integer.MIN_VALUE, minTemp = Integer.MAX_VALUE;
        for (int i = 0; i < days; i++) {
            if (highTemps[i] > maxTemp) maxTemp = highTemps[i];
            if (lowTemps[i] < minTemp) minTemp = lowTemps[i];
        }
        int tempRange = Math.max(1, maxTemp - minTemp + 10); // 增加一些缓冲
        
        float xStep = chartWidth * 1f / Math.max(1, days - 1);
        
        // 创建渐变效果
        setupGradients(height, padding, chartHeight);
        
        // 绘制网格辅助线
        drawGrid(canvas, width, height, padding, chartWidth, chartHeight);
        
        // 绘制高温曲线和填充
        drawTemperatureLine(canvas, padding, chartHeight, xStep, tempRange, maxTemp, minTemp, true);
        
        // 绘制低温曲线和填充
        drawTemperatureLine(canvas, padding, chartHeight, xStep, tempRange, maxTemp, minTemp, false);
        
        // 绘制数据点和温度标签
        drawDataPoints(canvas, padding, chartHeight, xStep, tempRange, maxTemp, minTemp);
    }
    
    private void setupGradients(int height, int padding, int chartHeight) {
        // 高温线渐变 - 黄色到橙色
        highGradient = new LinearGradient(0, 0, 0, height,
                Color.argb(200, 255, 213, 79), Color.argb(200, 255, 152, 0), Shader.TileMode.CLAMP);
        
        // 低温线渐变 - 青色到蓝色
        lowGradient = new LinearGradient(0, 0, 0, height,
                Color.argb(200, 129, 212, 250), Color.argb(200, 33, 150, 243), Shader.TileMode.CLAMP);
        
        // 高温填充渐变
        highFillGradient = new LinearGradient(0, padding, 0, padding + chartHeight,
                Color.argb(60, 255, 213, 79), Color.argb(10, 255, 152, 0), Shader.TileMode.CLAMP);
        
        // 低温填充渐变
        lowFillGradient = new LinearGradient(0, padding, 0, padding + chartHeight,
                Color.argb(10, 129, 212, 250), Color.argb(60, 33, 150, 243), Shader.TileMode.CLAMP);
    }
    
    private void drawGrid(Canvas canvas, int width, int height, int padding, int chartWidth, int chartHeight) {
        // 绘制横向网格线
        for (int i = 1; i < 4; i++) {
            float y = padding + (chartHeight * i / 4f);
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint);
        }
        
        // 绘制纵向网格线
        for (int i = 1; i < days; i++) {
            float x = padding + (chartWidth * i / (days - 1f));
            canvas.drawLine(x, padding, x, padding + chartHeight, gridPaint);
        }
    }
    
    private void drawTemperatureLine(Canvas canvas, int padding, int chartHeight, float xStep, 
                                    int tempRange, int maxTemp, int minTemp, boolean isHighTemp) {
        int[] temps = isHighTemp ? highTemps : lowTemps;
        
        // 创建路径
        Path linePath = new Path();
        Path fillPath = new Path();
        
        // 第一个点
        float firstX = padding;
        float firstY = padding + chartHeight * (1 - (temps[0] - minTemp + 5) * 1f / tempRange);
        linePath.moveTo(firstX, firstY);
        fillPath.moveTo(firstX, firstY);
        
        // 中间点 - 使用贝塞尔曲线实现平滑连接
        for (int i = 1; i < days; i++) {
            float x = padding + i * xStep;
            float y = padding + chartHeight * (1 - (temps[i] - minTemp + 5) * 1f / tempRange);
            
            if (i == 1) {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            } else {
                // 使用二次贝塞尔曲线平滑连接
                float prevX = padding + (i - 1) * xStep;
                float prevY = padding + chartHeight * (1 - (temps[i - 1] - minTemp + 5) * 1f / tempRange);
                float controlX = (prevX + x) / 2;
                float controlY = (prevY + y) / 2;
                linePath.quadTo(controlX, controlY, x, y);
                fillPath.quadTo(controlX, controlY, x, y);
            }
        }
        
        // 完成填充路径
        if (isHighTemp) {
            fillPath.lineTo(padding + (days - 1) * xStep, padding + chartHeight / 3f);
            fillPath.lineTo(firstX, padding + chartHeight / 3f);
        } else {
            fillPath.lineTo(padding + (days - 1) * xStep, padding + chartHeight * 2f / 3f);
            fillPath.lineTo(firstX, padding + chartHeight * 2f / 3f);
        }
        fillPath.close();
        
        // 绘制填充区域
        fillPaint.setShader(isHighTemp ? highFillGradient : lowFillGradient);
        canvas.drawPath(fillPath, fillPaint);
        
        // 绘制线条
        linePaint.setShader(isHighTemp ? highGradient : lowGradient);
        canvas.drawPath(linePath, linePaint);
    }
    
    private void drawDataPoints(Canvas canvas, int padding, int chartHeight, float xStep, 
                               int tempRange, int maxTemp, int minTemp) {
        float pointRadius = 6f;
        
        for (int i = 0; i < days; i++) {
            float x = padding + i * xStep;
            
            // 高温点
            float yHigh = padding + chartHeight * (1 - (highTemps[i] - minTemp + 5) * 1f / tempRange);
            
            // 绘制高温点（带渐变的半透明圆点）
            pointPaint.setShader(new RadialGradient(x, yHigh, pointRadius + 2,
                    Color.argb(180, 255, 213, 79), Color.argb(120, 255, 152, 0), Shader.TileMode.CLAMP));
            canvas.drawCircle(x, yHigh, pointRadius + 2, pointPaint);
            canvas.drawCircle(x, yHigh, pointRadius + 2, pointStrokePaint);
            
            // 高温文字
            canvas.drawText(highTemps[i] + "°", x, yHigh - 20, textPaint);
            
            // 低温点
            float yLow = padding + chartHeight * (1 - (lowTemps[i] - minTemp + 5) * 1f / tempRange);
            
            // 绘制低温点
            pointPaint.setShader(new RadialGradient(x, yLow, pointRadius + 2,
                    Color.argb(180, 129, 212, 250), Color.argb(120, 33, 150, 243), Shader.TileMode.CLAMP));
            canvas.drawCircle(x, yLow, pointRadius + 2, pointPaint);
            canvas.drawCircle(x, yLow, pointRadius + 2, pointStrokePaint);
            
            // 低温文字
            canvas.drawText(lowTemps[i] + "°", x, yLow + 35, textPaint);
        }
    }
} 