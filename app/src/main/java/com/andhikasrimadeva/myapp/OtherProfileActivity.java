package com.andhikasrimadeva.myapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OtherProfileActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        textView = (TextView) findViewById(R.id.other_profile_textView);

        textView.setText(getIntent().getStringExtra("user_id"));
    }
}
