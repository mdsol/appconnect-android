package com.sample.appconnectsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kbohlmann on 7/21/16.
 * Simple base class for the dialogue sequence of registration activities.
 */
public class RegistrationActivity extends AppCompatActivity {

    public static final int REGISTRATION_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTRATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }
}
