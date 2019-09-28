package com.example.audioscript;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import net.gotev.speech.Speech;
import net.gotev.speech.TextToSpeechCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LecturesActivity extends AppCompatActivity {

    private ArrayList<LectureItem> lectures = new ArrayList<LectureItem>();
    private LectureItem activeLecture;
    private LectureAdapter lectureAdapter;

    private Course activeCourse;


    private ASDatabase db;

    private Uri selectedImageUri;
    private Bitmap imageBitmap;
    private static final String TAG = "12345";
    private ImageView selectedImageDisplay;
    private String resultText;
    private TextView translatedTextView;
    private TextView fileInfoText;
    private RecyclerView lectureListView;
    private FloatingActionButton playButton;
    private boolean speaking =false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_list_activity);

        playButton = (FloatingActionButton) findViewById(R.id.playSpeechButton);
        final FloatingActionButton openImage = (FloatingActionButton) findViewById(R.id.openImageButton);
        activeCourse = (Course) getIntent().getSerializableExtra("course");

        setupView();
        Speech.init(this, getPackageName());
        initDB();
        initLectureAdapter();
        updateList();
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!speaking) {
                    playSpeech();
                } else {
                    Speech.getInstance().stopTextToSpeech();
                    onSpeechStop();
                }
            }
        });

        openImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //debug

                Log.d(TAG, "onClick: selecting image");

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });


    }

    private void playSpeech() {
        if (lectures.size()>0) {
            if (activeLecture == null) {
                activateLectureAtPos(0);
            }
            if (activeLecture != null) {
                Speech.getInstance().say(activeLecture.getContent(),new TextToSpeechCallback() {
                    @Override
                    public void onStart() {
                        playButton.setImageResource(android.R.drawable.ic_media_pause);
                        speaking =true;
                        Log.i("speech", "speech started");
                    }

                    @Override
                    public void onCompleted() {
                        onSpeechStop();
                        Log.i("speech", "speech completed");
                    }

                    @Override
                    public void onError() {
                        Log.i("speech", "speech error");
                    }
                });

            }
        }
    }

    private void onSpeechStop() {
        playButton.setImageResource(android.R.drawable.ic_media_play);
        speaking = false;
    }

    private void setupView() {
        selectedImageDisplay = (ImageView) findViewById(R.id.logoImageDisplay);
        translatedTextView = (TextView) findViewById(R.id.translatedTextView);
        fileInfoText = (TextView) findViewById(R.id.fileSelectedInfoText);
        lectureListView = (RecyclerView) findViewById(R.id.lecture_list);
        lectureListView.setHasFixedSize(true);

    }

    private void activateLectureAtPos(int position) {
        if (lectures.get(position) != null) {
            activeLecture = lectures.get(position);
            fileInfoText.setText(activeLecture.getName());
        }
    }

    private void convert() {
        if (imageBitmap != null) {

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            final Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    Log.d(TAG, "onSuccess: Image read sucessfully");
                                    Log.d(TAG, "onSuccess: " + firebaseVisionText.getText());
                                    //translatedTextView.setText("");
                                    //translatedTextView.setText(firebaseVisionText.getText());
                                    //Speech.getInstance().say(firebaseVisionText.getText());
                                    //addNewLecture(firebaseVisionText.getText(), "27.11.1994");
                                    showNameDialog(LecturesActivity.this, firebaseVisionText.getText());
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: Error while reading image");
                                        }
                                    });

        } else {
            Log.d(TAG, "onClick: no image selected");
        }
    }

    private void initLectureAdapter() {

        lectureAdapter = new LectureAdapter(this, lectures);
        lectureListView.setLayoutManager(new LinearLayoutManager(this));
        lectureListView.setAdapter(lectureAdapter);

        lectureAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d("RecyclerView", "onClick：" + lectures.get(position));
                // Speech.getInstance().say(lectures.get(position).getContent());
                activateLectureAtPos(position);
                playSpeech();
            }

            @Override
            public void onItemOptionsClick(final int pos, View v) {
                PopupMenu popup = new PopupMenu(LecturesActivity.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.options_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId()==R.id.menu1){
                            db.removeLecture(lectures.get(pos));
                            updateList();
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

    private void updateList() {
        Log.d("updatelist", "Course：" + activeCourse);
        lectures.clear();
        if (activeCourse.getCourseName() == null) {
            lectures.addAll(db.getAllLectures());
        } else {
            lectures.addAll(db.getAllLecturesOfCourse(activeCourse.getCourseName(), activeCourse.getId()));
            Log.d("updatelist", "size of course named" + activeCourse + ": " + db.getAllLecturesOfCourse(activeCourse.getCourseName(), activeCourse.getId()).size());
        }
        lectureAdapter.notifyDataSetChanged();
    }



    private void addNewLecture(String name, String content) {

        Date dueDate = new Date();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(dueDate);

        LectureItem newLecture = new LectureItem(name, content, cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));

        if (activeCourse == null) {
            Toast.makeText(this, "No course selected ",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        db.insertLecture(newLecture, activeCourse.getId());
        updateList();
        Toast.makeText(this, "File added: " + name,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {
                    Log.d(TAG, "onActivityResult: Image selected");
                    selectedImageUri = data.getData();
                    Log.d(TAG, "onActivityResult: " + selectedImageUri.toString());
                    //selectedImageDisplay.setImageURI(null);
                    //selectedImageDisplay.setImageURI(selectedImageUri);
                    Log.d(TAG, "onClick: image set!");
                    //fileInfoText.setText("File selected: " + selectedImageUri.getPath());
                    Toast.makeText(this, "File added: " + selectedImageUri.getPath(),
                            Toast.LENGTH_SHORT).show();

                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    convert();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "File select error", e);
        }
    }

    private void showNameDialog(Context c, final String content) {
        final EditText nameLecture = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Name Document")
                .setMessage("What is it called?")
                .setView(nameLecture)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = String.valueOf(nameLecture.getText());
                        addNewLecture(name, content);
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
