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
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText usernameField;
    private EditText passwordField;
    private Button logInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameField = (EditText)findViewById(R.id.login_username_field);
        passwordField = (EditText)findViewById(R.id.login_password_field);
        logInButton = (Button)findViewById(R.id.login_log_in_button);

        usernameField.setText(BuildConfig.DEFAULT_USERNAME);
        passwordField.setText(BuildConfig.DEFAULT_PASSWORD);
    }

    public void doLogInButton(View source) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        new LogInTask().execute(username, password);
    }

    public void doRegisterButton(View source) {
        Intent intent = new Intent(LoginActivity.this, RegistrationEmailActivity.class);
        startActivityForResult(intent, RegistrationEmailActivity.REGISTRATION_REQUEST, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RegistrationEmailActivity.REGISTRATION_REQUEST) {
            // The user registered successfully, set the email field, and clear password
            usernameField.setText(data.getStringExtra("email"));
            passwordField.setText("");

            passwordField.requestFocus();

            new AlertDialog.Builder(LoginActivity.this).
                    setTitle(R.string.registration_succeeded_title).
                    setMessage(R.string.registration_succeeded_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
        }
    }

    /**
     * An asynchronous task that logs in the user.
     */
    private class LogInTask extends AsyncTask<String,Void,Void> {

        private long userID;
        private RequestException exception;

        @Override
        protected void onPreExecute() {
            logInButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            // *** AppConnect ***
            // Each secondary thread must create its own datastore instance and
            // dispose of it when done
            Datastore datastore = null;
            try {
                datastore = DatastoreFactory.create();
                Client client = App.getClient(LoginActivity.this);
                User user = client.logIn(datastore, username, password);

                // Babbage objects can't be shared between threads so you must pass
                // them around by ID instead and the receiving code can get its own
                // copy from its own datastore
                userID = user.getID();
            }
            catch (RequestException ex) {
                exception = ex;
            }
            finally {
                if (datastore != null)
                    datastore.dispose();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            logInButton.setEnabled(true);

            if (exception != null) {
                Log.e(TAG, "The log in task failed", exception);
                int message = R.string.login_failed_message;

                if (exception.getErrorCause() == RequestException.ErrorCause.USER_NOT_ASSOCIATED_WITH_TOKEN)
                    message = R.string.login_user_not_associated_message;

                new AlertDialog.Builder(LoginActivity.this).
                    setTitle(R.string.login_failed_title).
                    setMessage(message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else {
                // Start the ListActivity to show the forms available for the
                // user who just logged in
                Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                intent.putExtra(ListActivity.USER_ID_EXTRA, userID);
                startActivity(intent);
            }
        }
    }
}
