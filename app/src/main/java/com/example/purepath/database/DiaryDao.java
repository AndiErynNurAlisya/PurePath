package com.example.purepath.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.purepath.model.DiaryEntry;
import java.util.ArrayList;
import java.util.List;

public class DiaryDao {

    private DatabaseHelper dbHelper;

    public DiaryDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Simpan atau update entry hari ini
    public void insertOrUpdate(String date, int aqi, String aqiLabel,
                               String description, double temp, double uv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_DATE, date);
        values.put(DatabaseHelper.COL_AQI, aqi);
        values.put(DatabaseHelper.COL_AQI_LABEL, aqiLabel);
        values.put(DatabaseHelper.COL_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_TEMP, temp);
        values.put(DatabaseHelper.COL_UV, uv);

        // Insert atau replace kalau tanggal sama
        db.insertWithOnConflict(DatabaseHelper.TABLE_DIARY, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Ambil semua entry, terbaru dulu
    public List<DiaryEntry> getAllEntries() {
        List<DiaryEntry> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_DIARY, null,
                null, null, null, null,
                DatabaseHelper.COL_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
                int aqi = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AQI));
                String label = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AQI_LABEL));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
                list.add(new DiaryEntry(date, desc, aqi, label));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Ambil 7 entry terakhir untuk chart
    public List<DiaryEntry> getLast7Entries() {
        List<DiaryEntry> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_DIARY, null,
                null, null, null, null,
                DatabaseHelper.COL_DATE + " DESC", "7");

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
                int aqi = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AQI));
                String label = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AQI_LABEL));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
                list.add(new DiaryEntry(date, desc, aqi, label));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Cek apakah data hari ini sudah ada
    public boolean isTodayExists(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DIARY,
                new String[]{DatabaseHelper.COL_ID},
                DatabaseHelper.COL_DATE + "=?",
                new String[]{date}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}