package com.sample.appconnectsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kbohlmann on 7/21/16.
 * Simple base class for the dialogue sequence of registration activities.
 */
public abstract class RegistrationActivity extends AppCompatActivity {

    public static final int REGISTRATION_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REGISTRATION_REQUEST) {
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
