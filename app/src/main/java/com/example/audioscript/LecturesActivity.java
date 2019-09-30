package com.example.audioscript;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class LecturesActivity extends AppCompatActivity {

    private ArrayList<LectureItem> lectures = new ArrayList<LectureItem>();
    private LectureItem activeLecture;
    private LectureAdapter lectureAdapter;

    private Course activeCourse;


    private ASDatabase db;

    private Uri selectedImageUri;
    private Bitmap imageBitmap;
    private static final String TAG = "12345";
    private TextView fileInfoText;
    private RecyclerView lectureListView;
    private FloatingActionButton playButton;
    private boolean speaking = false;
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_list_activity);


        activeCourse = (Course) getIntent().getSerializableExtra("course");

        setupView();
        Speech.init(this, getPackageName());
        initDB();
        initLectureAdapter();
        updateList();

    }

    private File generatePhotoFile() throws IOException {
        String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String name = "JPEG_" + dateTime + "_";
        File file = File.createTempFile(name, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        return file;
    }

    private void playSpeech() {
        if (lectures.size() > 0) {
            if (activeLecture == null) {
                activateLectureAtPos(0);
            }
            if (activeLecture != null) {
                Speech.getInstance().say(activeLecture.getContent(), new TextToSpeechCallback() {
                    @Override
                    public void onStart() {
                        playButton.setImageResource(android.R.drawable.ic_media_pause);
                        speaking = true;
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
        fileInfoText = (TextView) findViewById(R.id.fileSelectedInfoText);
        lectureListView = (RecyclerView) findViewById(R.id.lecture_list);
        lectureListView.setHasFixedSize(true);
        playButton = (FloatingActionButton) findViewById(R.id.playSpeechButton);
        final FloatingActionButton openImage = (FloatingActionButton) findViewById(R.id.openImageButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!speaking) {
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
                List<Intent> intentList = new ArrayList();
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photo = null;
                    try {
                        photo = generatePhotoFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (photo != null) {
                        Uri uri = FileProvider.getUriForFile(LecturesActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                photo);

                        photoFile = photo;
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        intentList.add(cameraIntent);
                    }
                }
                intentList.add(galleryIntent);
                Intent chooserIntent = Intent.createChooser(new Intent(), "Select source");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[intentList.size()]));

                startActivityForResult(chooserIntent, 1);

            }
        });

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
                                    Log.d(TAG, "onSuccess: " + firebaseVisionText.getText());
                                    showNameDialog(LecturesActivity.this, firebaseVisionText.getText());
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(LecturesActivity.this, "Error while reading image. ",
                                                    Toast.LENGTH_SHORT).show();
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
                        if (item.getItemId() == R.id.delete) {
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
        String picReference = "";
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {
                    if (data!= null&&data.getData()!=null) {
                        Log.d(TAG, "onActivityResult: Image selected "+data.getData());
                        selectedImageUri = data.getData();

                        picReference =""+selectedImageUri.getPath();

                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    } else {
                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(photoFile));
                        picReference = ""+ photoFile.getName();
                    }
                    Toast.makeText(this, "Converting " + picReference + "...",
                            Toast.LENGTH_SHORT).show();

                    convert();
                }
            } else {
                Log.d(TAG, "onActivityResult: resolutCode not ok. it is " + resultCode);
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
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
        Speech.getInstance().shutdown();
    }
}
