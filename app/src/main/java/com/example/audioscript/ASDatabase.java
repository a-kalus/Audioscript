package com.example.audioscript;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ASDatabase {

    private static final String DATABASE_NAME = "audioscript.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "lectures";

    public static final String KEY_ID = "_id";
    public static final String KEY_LECTURE_NAME = "lectureName";
    public static final String KEY_DATE = "date";
    public static final String KEY_CONTENT = "content";

    public static final int COLUMN_LECTURE_NAME_INDEX = 1;
    public static final int COLUMN_DATE_INDEX = 2;
    public static final int COLUMN_CONTENT_INDEX = 3;

    private ASDBOpenHelper dbHelper;

    private SQLiteDatabase db;

    public ASDatabase(Context context) {
        dbHelper = new ASDBOpenHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
    }

    public void open() throws SQLException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    public long insertLecture(LectureItem item) {
        ContentValues newToDoValues = new ContentValues();

        newToDoValues.put(KEY_LECTURE_NAME, item.getName());
        newToDoValues.put(KEY_CONTENT, item.getContent());
        newToDoValues.put(KEY_DATE, item.getFormattedDate());

        return db.insert(DATABASE_TABLE, null, newToDoValues);
    }

    public void removeLecture(LectureItem item) {
        String whereClause = KEY_LECTURE_NAME + " = '" + item.getName() + "' AND "
                + KEY_DATE + " = '" + item.getFormattedDate() + "'";

        db.delete(DATABASE_TABLE, whereClause, null);
    }

    public ArrayList<LectureItem> getAllLectures() {
        ArrayList<LectureItem> items = new ArrayList<LectureItem>();
        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID,
                KEY_LECTURE_NAME, KEY_DATE, KEY_CONTENT }, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(COLUMN_LECTURE_NAME_INDEX);
                String date = cursor.getString(COLUMN_DATE_INDEX);
                String content = cursor.getString(COLUMN_CONTENT_INDEX);

                Date formatedDate = null;
                try {
                    formatedDate = new SimpleDateFormat("dd.MM.yyyy",
                            Locale.GERMAN).parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar cal = Calendar.getInstance(Locale.GERMAN);
                cal.setTime(formatedDate);

                items.add(new LectureItem(name, content,cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)));

            } while (cursor.moveToNext());
        }
        return items;
    }

    private class ASDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE = "create table "
                + DATABASE_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + KEY_LECTURE_NAME
                + " text not null, " + KEY_CONTENT
                + " text not null, " + KEY_DATE + " text);";

        public ASDBOpenHelper(Context c, String dbname,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(c, dbname, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
