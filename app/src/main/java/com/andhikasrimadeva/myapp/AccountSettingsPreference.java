package com.andhikasrimadeva.myapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Andhika on 13/06/2017.
 */

public class AccountSettingsPreference extends DialogPreference {

    private FirebaseAuth mAuth;

    public AccountSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAuth = FirebaseAuth.getInstance();

        setDialogLayoutResource(R.layout.account_settings_dialog);
        setPositiveButtonText("LOGOUT");
        setNegativeButtonText("CANCEL");
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(positiveResult){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getContext().startActivity(intent);
        }
    }
}
