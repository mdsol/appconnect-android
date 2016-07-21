package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.RequestException;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationSecurityAnswerActivity extends RegistrationActivity {

    private static final String TAG = "RegSecAnsActivity";

    private Button submitButton;
    private EditText securityAnswerField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_security_answer_activity);

        //securityAnswer = (EditText)findViewById(R.id.);
        //submitButton =
    }

    public void onSubmitButton(View source) {

        if (!securityAnswerField.getText().toString().isEmpty()) {
            new AlertDialog.Builder(RegistrationSecurityAnswerActivity.this).
                    setTitle(R.string.security_answer_failed_title).
                    setMessage(R.string.security_answer_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            return;
        }

        // intent should have all the stuff.
        new RegisterTask().execute(getIntent().getStringExtra("email"),
                getIntent().getStringExtra("password"),
                getIntent().getStringExtra("securityQuestion"),
                getIntent().getStringExtra("securityAnswer"));
    }

    /**
     * An asynchronous task that registers a new user with the email.
     */
    private class RegisterTask extends AsyncTask<String,Void,Void> {

        private RequestException exception;

        @Override
        protected void onPreExecute() {
            submitButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];
            String password = params[1];
            String securityQuestion = params[2];
            String securityAnswer = params[3];

            // *** AppConnect ***
            // Each secondary thread must create its own datastore instance and
            // dispose of it when done
            Datastore datastore = null;
            try {
                datastore = DatastoreFactory.create();
                Client client = App.getClient(RegistrationSecurityAnswerActivity.this);
                //User user = client.logIn(datastore, username, password);

                // Babbage objects can't be shared between threads so you must pass
                // them around by ID instead and the receiving code can get its own
                // copy from its own datastore
                // userID = user.getID();
            }
            /*
            catch (RequestException ex) {
                exception = ex;
            }
            */

            finally {
                if (datastore != null)
                    datastore.dispose();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (exception != null) {
                submitButton.setEnabled(true);
                Log.e(TAG, "The registration task failed", exception);
                new AlertDialog.Builder(RegistrationSecurityAnswerActivity.this).
                        setTitle(R.string.registration_failed_title).
                        setMessage(exception.getMessage()).
                        setPositiveButton(R.string.ok_button, null).
                        show();
                return;
            }

            // unwind the whole stack of registration activities back to the login screen,
            // and populate the login email field.
            Intent returnIntent = new Intent();
            returnIntent.putExtra("email", getIntent().getStringExtra("email"));

            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}
