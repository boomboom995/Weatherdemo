package com.example.weatherforecast2.Entity;

public class DiaryEntry {
    private int id;
    private String date;
    private String mood;
    private String content;
    private long reminderTime;
    private boolean isPlan;
    private String planContent;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }
    public boolean isPlan() { return isPlan; }
    public void setPlan(boolean plan) { isPlan = plan; }
    public String getPlanContent() { return planContent; }
    public void setPlanContent(String planContent) { this.planContent = planContent; }
}