package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationEmailActivity extends RegistrationActivity {

    private EditText emailField;
    private EditText emailConfirmationField;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_email_activity);

        emailField = (EditText)findViewById(R.id.registration_email_field);
        emailConfirmationField = (EditText)findViewById(R.id.registration_email_confirmation_field);
        submitButton = (Button)findViewById(R.id.registration_email_submit_button);

        validate();

        TextWatcher validationListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        };

        emailField.addTextChangedListener(validationListener);
        emailConfirmationField.addTextChangedListener(validationListener);
    }

    public void onSubmitButton(View source) {
        Intent intent = new Intent(RegistrationEmailActivity.this, RegistrationPasswordActivity.class);
        intent.putExtra("email", emailField.getText().toString());
        startActivityForResult(intent, REGISTRATION_REQUEST);
    }

    private void validate() {

        submitButton.setEnabled(false);

        String email = emailField.getText().toString();
        String emailConfirmation = emailConfirmationField.getText().toString();

        if (email.equals(emailConfirmation) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            submitButton.setEnabled(true);
    }
}
