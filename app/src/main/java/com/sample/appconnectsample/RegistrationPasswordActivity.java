package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationPasswordActivity extends RegistrationActivity {

    private EditText passwordField;
    private EditText passwordConfirmationField;
    private Button submitButton;

    final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_password_activity);

        passwordField = (EditText)findViewById(R.id.registration_password_field);
        passwordConfirmationField = (EditText)findViewById(R.id.registration_password_confirmation_field);
        submitButton = (Button)findViewById(R.id.registration_submit_password_button);

        validate();

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        });

        passwordConfirmationField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        });
    }

    public void onSubmitButton(View source) {
        Intent intent = new Intent(RegistrationPasswordActivity.this, RegistrationSecurityQuestionActivity.class);
        intent.putExtra("email", getIntent().getStringExtra("email"));
        intent.putExtra("password", passwordField.getText().toString());
        startActivityForResult(intent, REGISTRATION_REQUEST);
    }

    private void validate() {

        submitButton.setEnabled(false);

        if (Pattern.compile(PASSWORD_PATTERN).matcher(passwordField.getText().toString()).matches() &&
                passwordField.getText().toString().equals(passwordConfirmationField.getText().toString()))
            submitButton.setEnabled(true);
    }

}
