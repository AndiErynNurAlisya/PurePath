package com.example.purepath.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "purepath.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_DIARY = "diary";
    public static final String COL_ID = "id";
    public static final String COL_DATE = "date";
    public static final String COL_AQI = "aqi";
    public static final String COL_AQI_LABEL = "aqi_label";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TEMP = "temperature";
    public static final String COL_UV = "uv_index";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_DIARY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT UNIQUE, " +
                COL_AQI + " INTEGER, " +
                COL_AQI_LABEL + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_TEMP + " REAL, " +
                COL_UV + " REAL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
        onCreate(db);
    }
}