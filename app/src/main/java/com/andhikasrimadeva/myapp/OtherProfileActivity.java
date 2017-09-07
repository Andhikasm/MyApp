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
import java.util.HashMap;
import java.util.Map;

public class OtherProfileActivity extends AppCompatActivity {

    private ImageView other_profile_Image;
    private TextView other_profile_name;
    private TextView other_profile_totalFriends;
    private Button other_profile_sendRequest;
    private Button other_profile_declineRequest;
    private ProgressDialog progressDialog;

    private String currentState;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mFirebaseUser;

    private void disableButton(Button button){
        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);
    }

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

        other_profile_declineRequest.setVisibility(View.INVISIBLE);
        other_profile_declineRequest.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load the user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                other_profile_name.setText(display_name);
                Picasso.with(OtherProfileActivity.this).load(image).placeholder(R.mipmap.ic_avatar).into(other_profile_Image);

                if(mFirebaseUser.getUid().equals(user_id)){

                    disableButton(other_profile_declineRequest);
                    disableButton(other_profile_sendRequest);
                }

                // -------------------- FRIEND LIST / REQUEST FEATURE ---------------------

                mFriendReqDatabase.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                currentState = "req_received";
                                other_profile_sendRequest.setText("Accept Friends Request");

                                other_profile_declineRequest.setVisibility(View.VISIBLE);
                                other_profile_declineRequest.setEnabled(true);
                            }
                            else if(req_type.equals("sent")) {

                                currentState = "req_sent";
                                other_profile_sendRequest.setText("Cancel Friends Request");

                                disableButton(other_profile_declineRequest);
                            }
                            progressDialog.dismiss();
                        }
                        else {

                            mFriendDatabase.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)) {
                                        currentState = "friends";
                                        other_profile_sendRequest.setText("Unfriend this Person");

                                        disableButton(other_profile_declineRequest);
                                    }

                                    progressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                progressDialog.dismiss();
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

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mFirebaseUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mFirebaseUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mFirebaseUser.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(OtherProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {

                                currentState = "req_sent";
                                other_profile_sendRequest.setText("Cancel Friends Request");

                            }

                            other_profile_sendRequest.setEnabled(true);

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
                                    other_profile_sendRequest.setText("Send Friends Request");

                                    disableButton(other_profile_declineRequest);
                                }
                            });
                        }
                    });

                }

                //-------------------- ACCEPT FRIEND REQUEST STATE ----------------

                else if (currentState.equals("req_received")) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mFirebaseUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + mFirebaseUser.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mFirebaseUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mFirebaseUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                other_profile_sendRequest.setEnabled(true);
                                currentState = "friends";
                                other_profile_sendRequest.setText("Unfriend this Person");

                                disableButton(other_profile_declineRequest);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(OtherProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                // ---------------------------- UNFRIEND FRIEND STATE -------------------------

                else if (currentState.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mFirebaseUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mFirebaseUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                currentState = "not_friends";
                                other_profile_sendRequest.setText("Send Friends Request");

                                disableButton(other_profile_declineRequest);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(OtherProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            other_profile_sendRequest.setEnabled(true);

                        }
                    });

                }


            }
        });


        // --------------- Decline Request ------------
        other_profile_declineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map declineReqMap = new HashMap();
                declineReqMap.put("Friend_req/" + user_id + "/" + mFirebaseUser.getUid(), null);
                declineReqMap.put("Friend_req/" + mFirebaseUser.getUid() + "/" + user_id, null);

                mRootRef.updateChildren(declineReqMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        currentState = "not_friends";
                        other_profile_sendRequest.setText("Send Friends Request");

                        disableButton(other_profile_declineRequest);
                    }
                });
            }
        });
    }
}
