package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.model.User;
import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.RequestException;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    private EditText emailField;
    private EditText emailConfirmationField;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);

        emailField = (EditText)findViewById(R.id.registration_email_field);
        emailConfirmationField = (EditText)findViewById(R.id.registration_email_confirmation_field);
        registerButton = (Button)findViewById(R.id.registration_register_button);
    }

    public void onRegisterButton(View source) {
        
        String email = emailField.getText().toString();
        String emailConfirmation = emailConfirmationField.getText().toString();

        if (!email.equals(emailConfirmation)) {
            //show error.   
            return;
        }
        
        new RegisterTask().execute(email);
    }

    /**
     * An asynchronous task that registers a new user with the email.
     */
    private class RegisterTask extends AsyncTask<String,Void,Void> {

        private long userID;
        private RequestException exception;

        @Override
        protected void onPreExecute() {
            registerButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];

            // *** AppConnect ***
            // Each secondary thread must create its own datastore instance and
            // dispose of it when done
            Datastore datastore = null;
            try {
                datastore = DatastoreFactory.create();
                Client client = App.getClient(RegistrationActivity.this);
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
            registerButton.setEnabled(true);

            if (exception != null) {
                Log.e(TAG, "The registration task failed", exception);
                new AlertDialog.Builder(RegistrationActivity.this).
                    setTitle(R.string.registration_failed_title).
                    setMessage(R.string.registration_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else {
                //segue to login
                // Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                // intent.putExtra(ListActivity.USER_ID_EXTRA, userID);
                // startActivity(intent);
            }
        }
    }
}
