package com.example.videoapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.videoapplication.R;
import com.example.videoapplication.Adapter.VideoAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 10;
    private static final int REQUEST_CODE = 111;
    private List<String> uriList =new ArrayList<>();
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingActionButton = findViewById(R.id.floatingButton);
        recyclerView = findViewById(R.id.recVideo);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(intent, REQUEST_CODE);


                }else{
                    checkPermission();
                }

            }
        });

           getVideos();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==REQUEST_CODE && resultCode == RESULT_OK) {

            uploadToFireBaseStorage(data.getData());
        }
    }

    private void uploadToFireBaseStorage(Uri uri){
        storageReference = FirebaseStorage.getInstance().getReference("video/");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        final StorageReference videoName = storageReference.child("Video" + uri.getLastPathSegment());
        videoName.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                videoName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = String.valueOf(uri);
                        insertVideoInDB(url,progressDialog);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error uploading the video", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });


    }

    private void insertVideoInDB(String url, final ProgressDialog progressDialog){
        databaseReference = FirebaseDatabase.getInstance().getReference("videos");
        Map<String,String> map = new HashMap<>();
        map.put("videoUrl",url);
        databaseReference.push().setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error uploading the video", Toast.LENGTH_LONG).show();

            }
        });


    }



    private void getVideos(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");
        databaseReference.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (uriList.size()>0){
                    uriList.clear();
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()){

                    String url = ds.child("videoUrl").getValue(String.class);
                    uriList.add(url);
                 }
                Collections.reverse(uriList);
                VideoAdapter videoAdapter = new VideoAdapter(MainActivity.this,uriList);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,RecyclerView.VERTICAL,false));
                SnapHelper snapHelper = new PagerSnapHelper();
                recyclerView.setAdapter(videoAdapter);
                recyclerView.setOnFlingListener(null);
                snapHelper.attachToRecyclerView(recyclerView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Something went wrong, please try again", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);

    }

    private void checkPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
    }






    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    new AlertDialog.Builder(this).
                            setTitle("Camera Permission").
                            setMessage("You need to grant Camera Permission to record videos" +
                                    " Retry and grant it !").show();
                } else {
                    new AlertDialog.Builder(this).
                            setTitle("Camera Permission Denied").
                            setMessage("You denied Camera Permission ." +
                                    " So, the feature will be disabled. To enable it" +
                                    ", go on settings and grant" +
                                    "Camera Permission for the application").show();
                }

            }
        }
    }
}
