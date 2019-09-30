package com.example.audioscript;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;

/*
 * Start Activity where the user can select an existing course or add a new one by hitting the action button
 */
public class CourseOverviewActivity extends AppCompatActivity {

    private ArrayList<Course> courses = new ArrayList();
    private CourseAdapter courseAdapter;
    private RecyclerView courseListView;

    private ASDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_list_activity);
        checkForCameraPermission();
        setupView();
        initDB();
        initCourseAdapter();
        updateCourseList();

    }

    //checks if the app is allowed to access the camera and asks the user for permission if not
    private void checkForCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }
    }

    private void setupView() {
        courseListView = (RecyclerView) findViewById(R.id.lecture_list);
        courseListView.setHasFixedSize(true);
        final FloatingActionButton addCourseButton = (FloatingActionButton) findViewById(R.id.openImageButton);
        addCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCourseDialog(CourseOverviewActivity.this);
            }
        });
    }

    /*
     *Sets the course adapter up and defines clicklisteners inside the RecyclerView:
     * A Click on a Course Item starts the Lectures Activity
     * and a click on the options button inside the item enables the deletion of that entry
     */
    private void initCourseAdapter() {

        courseAdapter = new CourseAdapter(this, courses);
        courseListView.setLayoutManager(new LinearLayoutManager(this));
        courseListView.setAdapter(courseAdapter);

        courseAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Course c = courses.get(position);
                Intent intent = new Intent(CourseOverviewActivity.this, LecturesActivity.class);
                intent.putExtra("course", c);
                startActivity(intent);
            }

            @Override
            public void onItemOptionsClick(final int pos, View v) {
                PopupMenu popup = new PopupMenu(CourseOverviewActivity.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.options_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete) {
                            db.removeCourse(courses.get(pos));
                            updateCourseList();
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    private void initDB() {
        db = new ASDatabase(this);
        db.open();
    }


    private void updateCourseList() {
        courses.clear();
        courses.addAll(db.getAllCourses());
        courseAdapter.notifyDataSetChanged();
    }

    //calls database to store course and updates the Recyclerview
    private void addNewCourse(String name) {
        Course newCourse = new Course(name);

        db.insertCourse(newCourse);
        updateCourseList();
        Toast.makeText(this, "Course added: " + name,
                Toast.LENGTH_SHORT).show();
    }

    //asks the user to name the course and calls addNewCourse() to store the answer
    private void showAddCourseDialog(Context c) {
        final EditText nameLecture = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Name Course")
                .setMessage("What is it called?")
                .setView(nameLecture)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = String.valueOf(nameLecture.getText());
                        addNewCourse(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
