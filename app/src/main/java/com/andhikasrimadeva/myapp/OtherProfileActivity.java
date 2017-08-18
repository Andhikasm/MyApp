package com.andhikasrimadeva.myapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class OtherProfileActivity extends AppCompatActivity {

    private ImageView other_profile_Image;
    private TextView other_profile_name;
    private TextView other_profile_totalFriends;
    private Button other_profile_sendRequest;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        String user_id = getIntent().getStringExtra("user_id");

        other_profile_Image = (ImageView) findViewById(R.id.other_profile_image);
        other_profile_name = (TextView) findViewById(R.id.other_profile_name);
        other_profile_totalFriends = (TextView) findViewById(R.id.other_profile_totalFriends);
        other_profile_sendRequest = (Button) findViewById(R.id.other_profile_sendReq);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                other_profile_name.setText(display_name);
                Picasso.with(OtherProfileActivity.this).load(image).placeholder(R.mipmap.ic_avatar).into(other_profile_Image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
