package com.example.thedominators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST =1;

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private EditText mEditTextFileName , etLong , etLati, etAbout , etCity, etRiver;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mButtonChooseImage = findViewById(R.id.btnchs);
        mButtonUpload = findViewById(R.id.btnupload);
        mEditTextFileName = findViewById(R.id.etName);
        etLong = findViewById(R.id.etLongi);
        etLati = findViewById(R.id.etLati);
        etAbout = findViewById(R.id.etAbout);
        etCity = findViewById(R.id.etCity);
        etRiver = findViewById(R.id.etRiver);
        mImageView = findViewById(R.id.imgview);
        mStorageRef = FirebaseStorage.getInstance().getReference("Uploaded Data");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Uploaded Data");
        mProgressBar = findViewById(R.id.progressbar);
        mImageView.setVisibility(View.GONE);


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();

            }
        });
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask!= null&& mUploadTask.isInProgress()){
                    Toast.makeText(UploadActivity.this,"Upload in Progress",Toast.LENGTH_LONG).show();
                }
                else {
                    uploadFile();
                }

            }
        });
        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadActivity.this,MainActivity.class));

            }
        });

    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK
                && data!= null && data.getData()!=null){
            mImageUri=data.getData();
            mImageView.setVisibility(View.VISIBLE);
            Picasso.with(this).load(mImageUri).into(mImageView);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR=getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(mImageUri!=null){
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            mUploadTask=fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            },500);
                            Toast.makeText(UploadActivity.this,"Uplaod Successfull",Toast.LENGTH_SHORT).show();

                           /* Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                    taskSnapshot.getStorage().getDownloadUrl().toString());
                            String uploadId=mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);*/
                            Task<Uri> urlTask= taskSnapshot.getStorage().getDownloadUrl();
                            while(!urlTask.isSuccessful());
                            Uri downloadUrl=urlTask.getResult();
                            Upload upload=new Upload(mEditTextFileName.getText().toString().trim(),downloadUrl.toString(),etCity.getText().toString(),etAbout.getText().toString(),etLong.getText().toString(),etLati.getText().toString(),etRiver.getText().toString());
                            String uploadId=mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);
                            Intent intent = new Intent (UploadActivity.this , MainActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this , e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot){

                            mProgressBar.setVisibility(View.VISIBLE);
                            double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int)progress);
                            ProgressDialog progressDialog = new ProgressDialog(UploadActivity.this);
                            progressDialog.setMessage("Uploading");
                        }
                    });
        }
        else{
            Toast.makeText(this,"No File Selected",Toast.LENGTH_SHORT).show();
        }
    }
}