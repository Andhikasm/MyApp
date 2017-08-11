package com.andhikasrimadeva.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private EditText _nameText;
    private EditText _emailText;
    private EditText _passwordText;
    private TextView _loginLink;
    private Button _signupButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        _nameText = (EditText) findViewById(R.id.input_name);
        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginLink = (TextView) findViewById(R.id.link_login);
        _signupButton = (Button) findViewById(R.id.btn_signup);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailEntered = _emailText.getText().toString();
                String passwordEntered = _passwordText.getText().toString();
                String nameEntered = _nameText.getText().toString();
                createAccount(emailEntered, passwordEntered, nameEntered);
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    private void createAccount(String email, String password, final String name){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            String uID = mAuth.getCurrentUser().getUid();
                            DatabaseReference myRef = database.getReference().child("Users").child(uID);

                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name", name);
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            myRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        else {
                            Toast.makeText(SignUpActivity.this, "Authentication failed, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}