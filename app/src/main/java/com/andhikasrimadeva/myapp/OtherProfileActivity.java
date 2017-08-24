package com.andhikasrimadeva.myapp;

import android.app.ProgressDialog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class OtherProfileActivity extends AppCompatActivity {

    private ImageView other_profile_Image;
    private TextView other_profile_name;
    private TextView other_profile_totalFriends;
    private Button other_profile_sendRequest;
    private Button other_profile_declineRequest;
    private ProgressDialog progressDialog;

    private String currentState;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        other_profile_Image = (ImageView) findViewById(R.id.other_profile_image);
        other_profile_name = (TextView) findViewById(R.id.other_profile_name);
        other_profile_totalFriends = (TextView) findViewById(R.id.other_profile_totalFriends);
        other_profile_sendRequest = (Button) findViewById(R.id.other_profile_sendReq);
        other_profile_declineRequest = (Button) findViewById(R.id.other_profile_declineReq);

        currentState = "not_friends";

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load the user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                other_profile_name.setText(display_name);
                Picasso.with(OtherProfileActivity.this).load(image).placeholder(R.mipmap.ic_avatar).into(other_profile_Image);

                mFriendReqDatabase.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                currentState = "req_received";
                                other_profile_sendRequest.setText("Accept Friend Request");
                            }
                            else if(req_type.equals("sent")) {

                                currentState = "req_sent";
                                other_profile_sendRequest.setText("Cancel Friend Request");
                            }
                        }
                        else {

                            mFriendDatabase.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)) {
                                        currentState = "friends";
                                        other_profile_sendRequest.setText("Unfriend this Person");
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        other_profile_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                other_profile_sendRequest.setEnabled(false);

                // ------------------- NOT FRIENDS STATE ---------

                if (currentState.equals("not_friends")) {

                    mFriendReqDatabase.child(mFirebaseUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {

                                mFriendReqDatabase.child(user_id).child(mFirebaseUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        other_profile_sendRequest.setEnabled(true);
                                        currentState = "req_sent";
                                        other_profile_sendRequest.setText("Cancel Friend Request");

                                        Toast.makeText(getApplicationContext(), "Request Sent Successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Failed Sending Request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //-------------------- CANCEL REQUEST STATE ---------------

                //DIFFERENT FROM TUTORIAL HERE
                else if (currentState.equals("req_sent")) {

                    mFriendReqDatabase.child(mFirebaseUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mFirebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    other_profile_sendRequest.setEnabled(true);
                                    currentState = "not_friends";
                                    other_profile_sendRequest.setText("Send Friend Request");
                                }
                            });
                        }
                    });

                }

                //-------------------- ACCEPT FRIEND REQUEST STATE ----------------

                else if (currentState.equals("req_received")) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    mFriendDatabase.child(mFirebaseUser.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mFirebaseUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(mFirebaseUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendReqDatabase.child(user_id).child(mFirebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    other_profile_sendRequest.setEnabled(true);
                                                    currentState = "friends";
                                                    other_profile_sendRequest.setText("Unfriend this Person");
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                // ---------------------------- UNFRIEND FRIEND STATE -------------------------

                else if (currentState.equals("friends")) {

                    mFriendDatabase.child(mFirebaseUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mFirebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    other_profile_sendRequest.setEnabled(true);
                                    currentState = "not_friends";
                                    other_profile_sendRequest.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }
            }
        });

    }
}
