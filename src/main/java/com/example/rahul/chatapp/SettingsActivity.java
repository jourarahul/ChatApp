package com.example.rahul.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private TextView setting_name,setting_status;
    private CircleImageView setting_img;
    private Button setting_changestatus,setting_changeimage;
    private static final int REQUEST_CODE=2;

    //storage Reference
    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mImageStorage= FirebaseStorage.getInstance().getReference();

        setting_name= (TextView) findViewById(R.id.setting_name);
        setting_status= (TextView) findViewById(R.id.setting_status);
        setting_img= (CircleImageView) findViewById(R.id.setting_img);
        setting_changestatus= (Button) findViewById(R.id.setting_changestatus);
        setting_changeimage= (Button) findViewById(R.id.setting_changeimage);
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();



        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mUserDatabase.keepSynced(true);//offline feature to load data fast
        //fetch data
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("Image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                setting_name.setText(name);
                setting_status.setText(status);
                if(!image.equals("default"))
                {
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.ic_launcher).into(setting_img, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.mipmap.ic_launcher).into(setting_img);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this, databaseError.getMessage()+"", Toast.LENGTH_SHORT).show();
            }
        });


        setting_changestatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SettingsActivity.this,StatusActivity.class);
                intent.putExtra("status",setting_status.getText().toString());
                startActivity(intent);
            }
        });

        setting_changeimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GalleryIntent=new Intent();
                GalleryIntent.setType("image/*");
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(GalleryIntent,"Choose Images"),REQUEST_CODE);

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1).start(SettingsActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog=new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("uploading Image");
                mProgressDialog.setMessage("please wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();

                File file=new File(resultUri.getPath());

                String uid = mCurrentUser.getUid();

                //compressing image and upload to firebase

                byte[] thumb_bytes=null;
                try {
                     Bitmap compressToBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(200).
                    setQuality(75).compressToBitmap(file);
                    //for uploading bitmap to firebase
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    compressToBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    thumb_bytes = stream.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                StorageReference filepath=mImageStorage.child("profile_images").
                        child(uid+"profile_image.jpeg"); //name of image
                 final StorageReference thumb_filepath=mImageStorage.child("profile_images").
                        child("thumbs").child(uid+"thumb.jpeg"); //name of image

                final byte[] finalThumb_bytes = thumb_bytes;
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            String downloadUrl=task.getResult().getDownloadUrl().toString();
                            //uploading thumb
                            UploadTask uploadTask = thumb_filepath.putBytes(finalThumb_bytes);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful())
                                    {

                                    }
                                }
                            });

                            //
                            mUserDatabase.child("Image").setValue(downloadUrl).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {

                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Success Upload", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );

                            Toast.makeText(SettingsActivity.this, "image saved", Toast.LENGTH_SHORT).show();

                        }
                        else {
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    // to get result from image cropper lib
    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                Uri resultUri = result.getUri();
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }
//    }

}
