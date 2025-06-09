package com.example.weatherforecast2.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.weatherforecast2.Entity.DiaryEntry;
import java.util.ArrayList;
import java.util.List;

public class DiaryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_DIARY = "diary";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_REMINDER_TIME = "reminder_time";
    public static final String COLUMN_IS_PLAN = "is_plan";
    public static final String COLUMN_PLAN_CONTENT = "plan_content";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_DIARY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_MOOD + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_REMINDER_TIME + " INTEGER DEFAULT 0, " +
                    COLUMN_IS_PLAN + " INTEGER DEFAULT 0, " +
                    COLUMN_PLAN_CONTENT + " TEXT" +
                    ");";

    public DiaryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_REMINDER_TIME + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_IS_PLAN + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_DIARY + " ADD COLUMN " + COLUMN_PLAN_CONTENT + " TEXT");
        }
    }

    public long addOrUpdateDiary(DiaryEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, entry.getDate());
        values.put(COLUMN_MOOD, entry.getMood());
        values.put(COLUMN_CONTENT, entry.getContent());
        values.put(COLUMN_REMINDER_TIME, entry.getReminderTime());
        values.put(COLUMN_IS_PLAN, entry.isPlan() ? 1 : 0);
        values.put(COLUMN_PLAN_CONTENT, entry.getPlanContent());

        if (entry.getId() > 0) {
            return db.update(TABLE_DIARY, values, COLUMN_ID + " = ?", new String[]{String.valueOf(entry.getId())});
        } else {
            return db.insert(TABLE_DIARY, null, values);
        }
    }

    public List<DiaryEntry> getAllDiaryEntries() {
        List<DiaryEntry> entries = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_DIARY + " ORDER BY " + COLUMN_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    DiaryEntry entry = new DiaryEntry();
                    entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    entry.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    entry.setMood(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)));
                    entry.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                    entry.setReminderTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME)));
                    entry.setPlan(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PLAN)) == 1);
                    entry.setPlanContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_CONTENT)));
                    entries.add(entry);
                } while (cursor.moveToNext());
            }
        }
        return entries;
    }

    public String getMoodForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String mood = null;
        try (Cursor cursor = db.query(TABLE_DIARY, new String[]{COLUMN_MOOD}, COLUMN_DATE + " = ? AND " + COLUMN_IS_PLAN + " = 0", new String[]{date}, null, null, null)) {
            if (cursor.moveToFirst()) {
                mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD));
            }
        }
        return mood;
    }

    public boolean hasPlanForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean hasPlan = false;
        try (Cursor cursor = db.query(TABLE_DIARY, new String[]{COLUMN_ID}, COLUMN_DATE + " = ? AND " + COLUMN_IS_PLAN + " = 1", new String[]{date}, null, null, null)) {
            hasPlan = cursor.getCount() > 0;
        }
        return hasPlan;
    }

    public boolean hasEntryForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;
        try (Cursor cursor = db.query(TABLE_DIARY, new String[]{COLUMN_ID}, COLUMN_DATE + " = ?", new String[]{date}, null, null, null)) {
            exists = cursor.getCount() > 0;
        }
        return exists;
    }

    public int deleteDiary(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_DIARY, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
}