package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationPasswordActivity extends RegistrationActivity {

    private EditText passwordField;
    private EditText passwordConfirmationField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_password_activity);

        passwordField = (EditText)findViewById(R.id.registration_password_field);
        passwordConfirmationField = (EditText)findViewById(R.id.registration_password_confirmation_field);
    }

    public void onSubmitButton(View source) {

        if (!passwordField.getText().toString().equals(passwordConfirmationField.getText().toString())) {
            new AlertDialog.Builder(RegistrationPasswordActivity.this).
                    setTitle(R.string.registration_password_failed_title).
                    setMessage(R.string.registration_password_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            return;
        }

        // activity with result into security question.
        Intent intent = new Intent(RegistrationPasswordActivity.this, RegistrationEmailActivity.class);
        intent.putExtra("email", getIntent().getStringExtra("email"));
        intent.putExtra("password", passwordField.getText().toString());
        startActivityForResult(intent, RegistrationEmailActivity.REGISTRATION_REQUEST);
    }
}
