package com.sample.appconnectsample;

import android.content.Intent;
import android.os.Bundle;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationSecurityQuestionActivity extends RegistrationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_security_question_activity);
    }

    // selection from table.
    public void onSelection() {
        // activity with result into security question.
        String securityQuestion = "";
        Intent intent = new Intent(RegistrationSecurityQuestionActivity.this, RegistrationSecurityAnswerActivity.class);
        intent.putExtra("email", getIntent().getStringExtra("email"));
        intent.putExtra("password", getIntent().getStringExtra("password"));
        intent.putExtra("securityQuestion", securityQuestion);
        startActivityForResult(intent, RegistrationSecurityAnswerActivity.REGISTRATION_REQUEST);
    }
}
