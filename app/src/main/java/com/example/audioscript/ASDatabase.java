package com.example.audioscript;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ASDatabase {

    private static final String DATABASE_NAME = "audioscript.db";
    private static final int DATABASE_VERSION = 1;

    private static final String LECTURES_TABLE = "lectures";
    private static final String COURSES_TABLE = "courses";
    private static final String LECTURE_COURSES_TABLE = "lecture_courses";

    private static final String KEY_ID = "_id";
    private static final String KEY_LECTURE_NAME = "lectureName";
    private static final String KEY_DATE = "date";
    private static final String KEY_CONTENT = "content";

    private static final int COLUMN_LECTURE_NAME_INDEX = 1;
    private static final int COLUMN_DATE_INDEX = 3;
    private static final int COLUMN_CONTENT_INDEX = 2;

    private static final String KEY_COURSE_NAME = "course_name";

    private static final String KEY_LECTURE_ID = "lecture_id";
    private static final String KEY_COURSE_ID = "course_id";

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


    //create an new entry in the lecture table
    public long insertLecture(LectureItem item, long course_id) {
        ContentValues newLectureValues = new ContentValues();

        newLectureValues.put(KEY_LECTURE_NAME, item.getName());
        newLectureValues.put(KEY_CONTENT, item.getContent());
        newLectureValues.put(KEY_DATE, item.getFormattedDate());


        long lecture_id = db.insert(LECTURES_TABLE, null, newLectureValues);

        insertLectureCourse(lecture_id, course_id, item.getFormattedDate());

        return lecture_id;

    }
    //store the assignment of a lecture to its course
    public long insertLectureCourse(long lecture_id, long course_id, String date) {
        ContentValues values = new ContentValues();
        values.put(KEY_LECTURE_ID, lecture_id);
        values.put(KEY_COURSE_ID, course_id);
        values.put(KEY_DATE, date);

        return db.insert(LECTURE_COURSES_TABLE, null, values);
    }

    //create a new entry in the Course table
    public long insertCourse(Course course) {

        ContentValues values = new ContentValues();
        values.put(KEY_COURSE_NAME, course.getCourseName());
        values.put(KEY_DATE, String.valueOf(new Date()));

        return db.insert(COURSES_TABLE, null, values);
    }


    public void removeLecture(LectureItem item) {
        String whereClause = KEY_LECTURE_NAME + " = '" + item.getName() + "' AND "
                + KEY_DATE + " = '" + item.getFormattedDate() + "'";

        db.delete(LECTURES_TABLE, whereClause, null);

    }

    public ArrayList<LectureItem> getAllLectures() {
        ArrayList<LectureItem> items = new ArrayList<LectureItem>();
        Cursor cursor = db.query(LECTURES_TABLE, new String[]{KEY_ID,
                KEY_LECTURE_NAME, KEY_CONTENT, KEY_DATE}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(COLUMN_LECTURE_NAME_INDEX);
                String date = cursor.getString(COLUMN_DATE_INDEX);
                String content = cursor.getString(COLUMN_CONTENT_INDEX);
                Log.d("GetAllLectures", "lecture：" + name);
                Log.d("GetAllLectures", "date：" + date);
                Log.d("GetAllLectures", "content：" + content);


                Date formatedDate = null;
                try {
                    formatedDate = new SimpleDateFormat("dd.MM.yyyy",
                            Locale.GERMAN).parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar cal = Calendar.getInstance(Locale.GERMAN);
                cal.setTime(formatedDate);

                items.add(new LectureItem(name, content, cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)));

            } while (cursor.moveToNext());
        }
        return items;
    }

    public ArrayList<LectureItem> getAllLecturesOfCourse(String courseName, int courseID) {
        ArrayList<LectureItem> items = new ArrayList<LectureItem>();

        String query = "SELECT * FROM " + LECTURES_TABLE + " l, "
                + COURSES_TABLE + " c, " + LECTURE_COURSES_TABLE + " lc WHERE c."
                + KEY_COURSE_NAME + " = '" + courseName + "'" + " AND c." + KEY_ID
                + " = " + "lc." + KEY_COURSE_ID + " AND c." + KEY_ID + " = '"
                + courseID + "'" + " AND l." + KEY_ID + " = "
                + "lc." + KEY_LECTURE_ID;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {

                String name = cursor.getString(COLUMN_LECTURE_NAME_INDEX);
                String date = cursor.getString(COLUMN_DATE_INDEX);
                String content = cursor.getString(COLUMN_CONTENT_INDEX);
                Log.d("GetAllLecturesOfCourse", "lecture：" + name);
                Log.d("GetAllLecturesOfCourse", "date：" + date);
                Log.d("GetAllLecturesOfCourse", "content：" + content);
                Date formatedDate = null;
                try {
                    formatedDate = new SimpleDateFormat("dd.MM.yyyy",
                            Locale.GERMAN).parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar cal = Calendar.getInstance(Locale.GERMAN);
                cal.setTime(formatedDate);

                items.add(new LectureItem(name, content, cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)));

            } while (cursor.moveToNext());
        }
        return items;
    }

    public ArrayList<Course> getAllCourses() {
        ArrayList<Course> courses = new ArrayList<Course>();

        Cursor cursor = db.rawQuery("SELECT  * FROM " + COURSES_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course(cursor.getInt((cursor.getColumnIndex(KEY_ID))), cursor.getString(cursor.getColumnIndex(KEY_COURSE_NAME)));
                courses.add(course);
            } while (cursor.moveToNext());
        }
        return courses;
    }

    public void removeCourse(Course course) {
        ArrayList<LectureItem> lecturesToRemove = getAllLecturesOfCourse(course.getCourseName(), course.getId());
        for (int i =0; i<lecturesToRemove.size();i++){
            removeLecture(lecturesToRemove.get(i));
        }
        db.delete(COURSES_TABLE, KEY_ID + " = ?",
                new String[] { String.valueOf(course.getId()) });
    }

    private class ASDBOpenHelper extends SQLiteOpenHelper {
        private static final String CREATE_LECTURES = "create table "
                + LECTURES_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + KEY_LECTURE_NAME
                + " text not null, " + KEY_CONTENT
                + " text not null, " + KEY_DATE + " text);";

        private static final String CREATE_COURSES_TABLE = "create table " + COURSES_TABLE
                + " (" + KEY_ID + " integer primary key," + KEY_COURSE_NAME + " text,"
                + KEY_DATE + " text);";

        private static final String CREATE_LECTURE_COURSES_TABLE = "create table "
                + LECTURE_COURSES_TABLE + " (" + KEY_ID + " integer primary key,"
                + KEY_LECTURE_ID + " integer," + KEY_COURSE_ID + " integer,"
                + KEY_DATE + " text" + ");";

        public ASDBOpenHelper(Context c, String dbname,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(c, dbname, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_LECTURES);
            db.execSQL(CREATE_COURSES_TABLE);
            db.execSQL(CREATE_LECTURE_COURSES_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
