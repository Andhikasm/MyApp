package com.andhikasrimadeva.myapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.single_user_layout,
                UsersViewHolder.class,
                mDatabaseReference
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setUserNameView(model.getName());
                viewHolder.setUserImageView(model.getThumb_image(), getApplicationContext());
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        TextView userNameView;
        CircleImageView userImageView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            userNameView = (TextView) itemView.findViewById(R.id.user_single_name);
            userImageView = (CircleImageView) itemView.findViewById(R.id.user_single_image);
        }

        public void setUserNameView(String name) {
            userNameView.setText(name);
        }

        public void setUserImageView(String thumb_image, Context context) {
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.ic_avatar).into(userImageView);
        }
    }
}
