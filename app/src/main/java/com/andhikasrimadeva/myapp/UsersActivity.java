package com.andhikasrimadeva.myapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
