package com.example.audioscript;


import android.Manifest;
import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;


import net.gotev.speech.Speech;

import java.util.ArrayList;

import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {


    private ArrayList<Course> courses = new ArrayList(); //move to new class
    private Course activeCourse;
    private CourseAdapter courseAdapter;


    private ASDatabase db;


    //move to new class
    private RecyclerView courseListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
           
        }else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }


        final FloatingActionButton playButton = (FloatingActionButton) findViewById(R.id.playSpeechButton);
        final FloatingActionButton openImage = (FloatingActionButton) findViewById(R.id.openImageButton);

        setupView();

        initDB();
        initCourseAdapter();
        updateCourseList();


        openImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //debug
                showAddCourseDialog(MainActivity.this);

            }
        });

    }

    private void setupView() {
        ImageView selectedImageDisplay = (ImageView) findViewById(R.id.logoImageDisplay);
        TextView translatedTextView = (TextView) findViewById(R.id.translatedTextView);
        TextView fileInfoText = (TextView) findViewById(R.id.fileSelectedInfoText);
        courseListView = (RecyclerView) findViewById(R.id.lecture_list);
        courseListView.setHasFixedSize(true);

    }


    private void initCourseAdapter() {

        courseAdapter = new CourseAdapter(this, courses);
        courseListView.setLayoutManager(new LinearLayoutManager(this));
        courseListView.setAdapter(courseAdapter);

        courseAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d("RecyclerView", "onClick：" + courses.get(position));
                // Speech.getInstance().say(lectures.get(position).getContent());

                activeCourse = courses.get(position);
                Course c = courses.get(position);
                Intent intent = new Intent(MainActivity.this, LecturesActivity.class);
                //intent.putExtra("itemName", model.getName());
                intent.putExtra("course", c);
                startActivity(intent);
                //updateList(courses.get(position).getCourseName());
            }

            @Override
            public void onItemOptionsClick(final int pos, View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.options_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId()==R.id.menu1){
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



    private void addNewCourse(String name) {

        Course newCourse = new Course(name);

        db.insertCourse(newCourse);
        updateCourseList();
        Toast.makeText(this, "File added: " + name,
                Toast.LENGTH_SHORT).show();
    }



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
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
        Speech.getInstance().shutdown();
    }
}
