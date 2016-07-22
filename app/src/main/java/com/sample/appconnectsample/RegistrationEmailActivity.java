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

        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        });

        emailConfirmationField.addTextChangedListener(new TextWatcher() {
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

        if (emailField.getText().toString().isEmpty())
            return;

        if (!emailField.getText().toString().equals(emailConfirmationField.getText().toString())) {
            new AlertDialog.Builder(RegistrationEmailActivity.this).
                    setTitle(R.string.registration_email_failed_title).
                    setMessage(R.string.registration_email_mismatch_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            return;
        }

        // segue to password entry
        Intent intent = new Intent(RegistrationEmailActivity.this, RegistrationPasswordActivity.class);
        intent.putExtra("email", emailField.getText().toString());
        startActivityForResult(intent, RegistrationEmailActivity.REGISTRATION_REQUEST);
    }

    private void validate() {

        submitButton.setEnabled(false);

        if (emailField.getText().toString().equals(emailConfirmationField.getText().toString()) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(emailField.getText().toString()).matches())
            submitButton.setEnabled(true);
    }
}
