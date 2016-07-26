package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.RequestException;

import java.util.HashMap;
import java.util.Map;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationSecurityQuestionActivity extends RegistrationActivity {

    private ListView securityQuestionListView;
    private HashMap<Integer, String> securityQuestionsById;
    private View progressBar;
    private TextView questionPromptView;
    private String[] questionsArray;

    private static final String TAG = "RegSecQuestionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_security_question_activity);

        progressBar = findViewById(R.id.questions_progress_bar);
        progressBar.setVisibility(View.GONE);

        questionPromptView = (TextView)findViewById(R.id.registration_security_question_prompt_view);
        questionPromptView.setVisibility(View.INVISIBLE);

        securityQuestionListView = (ListView)findViewById(R.id.registration_security_question_list_view);

        securityQuestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // find the item's id in the hash map of security questions.
                for (Map.Entry<Integer, String> questionEntry : securityQuestionsById.entrySet()) {
                    if (questionsArray[position].equals(questionEntry.getValue())) {
                        onSecurityQuestionSelected(questionEntry.getKey(), questionEntry.getValue());
                        return;
                    }
                }
            }
        });

        new LoadSecurityQuestionsTask().execute();
    }

    public void onSecurityQuestionSelected(int questionId, String question) {
        Intent intent = new Intent(RegistrationSecurityQuestionActivity.this, RegistrationSecurityAnswerActivity.class);
        intent.putExtra("email", getIntent().getStringExtra("email"));
        intent.putExtra("password", getIntent().getStringExtra("password"));
        intent.putExtra("securityQuestionId", questionId);
        intent.putExtra("securityQuestion", question);
        startActivityForResult(intent, REGISTRATION_REQUEST);
    }

    public void populateList() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, questionsArray);
        securityQuestionListView.setAdapter(arrayAdapter);
    }

    /**
     * An asynchronous task that registers a new user.
     */
    private class LoadSecurityQuestionsTask extends AsyncTask<String,Void,Void> {

        private RequestException exception;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            // *** AppConnect ***
            // Request the subject registration security questions and their ids.
            try {
                Client client = App.getClient(RegistrationSecurityQuestionActivity.this);

                securityQuestionsById = client.loadSecurityQuestions();
                questionsArray = new String[securityQuestionsById.size()];
                questionsArray = securityQuestionsById.values().toArray(questionsArray);
            }
            catch (RequestException ex) {
                exception = ex;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);

            if (exception != null) {
                Log.e(TAG, "Failed to load security questions.", exception);
                new AlertDialog.Builder(RegistrationSecurityQuestionActivity.this).
                        setTitle(R.string.registration_failed_title).
                        setMessage(exception.getMessage()).
                        setPositiveButton(R.string.ok_button, null).
                        show();
                return;
            }

            questionPromptView.setVisibility(View.VISIBLE);

            populateList();
        }
    }
}
