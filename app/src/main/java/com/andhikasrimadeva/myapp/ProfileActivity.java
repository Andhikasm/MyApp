package com.andhikasrimadeva.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private CircleImageView profileImage;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private StorageReference mStorageRef;

    private ProgressDialog progressDialog;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        displayName = (TextView) findViewById(R.id.display_name);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uID = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
        databaseReference.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
//                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                displayName.setText(name);

                if (!image.equals("default")) {
                    //Picasso.with(ProfileActivity.this).load(image).into(profileImage);

                    Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.ic_avatar).into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileActivity.this).load(image).into(profileImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            progressDialog = new ProgressDialog(ProfileActivity.this);
            progressDialog.setTitle("Uploading image...");
            progressDialog.setMessage("Please wait until the image has finished uploading");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final File thumb_file = new File(resultUri.getPath());

                File imageFile = null;
                try {
                    imageFile = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToFile(thumb_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                StorageReference filePath = mStorageRef.child("profile_images").child(currentUID + ".jpg");
                final StorageReference thumb_filePath = mStorageRef.child("profile_images").child("thumbs").child(currentUID + ".jpg");

                final File finalImageFile = imageFile;
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()) {
                            final String download_url = task.getResult().getDownloadUrl().toString();
                            thumb_filePath.putFile(Uri.fromFile(finalImageFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();

                                    Map update = new HashMap<String, String>();
                                    update.put("image", download_url);
                                    update.put("thumb_image", thumb_download_url);
                                    databaseReference.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Picasso.with(ProfileActivity.this).load(download_url).into(profileImage);
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Successfully uploaded", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });

                        }
                        else{
                            Toast.makeText(ProfileActivity.this, "Error in uploading", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
