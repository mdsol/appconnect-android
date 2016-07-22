package com.sample.appconnectsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * The activity where the user can log in with a username and password.
 */
public class RegistrationSecurityQuestionActivity extends RegistrationActivity {

    private ListView securityQuestionListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_security_question_activity);

        securityQuestionListView = (ListView)findViewById(R.id.registration_security_question_list_view);

        String[] questions = getResources().getStringArray(R.array.security_questions);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, questions );
        securityQuestionListView.setAdapter(arrayAdapter);

        securityQuestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSecurityQuestionSelected(position);
            }
        });
    }

    public void onSecurityQuestionSelected(int questionId) {
        Intent intent = new Intent(RegistrationSecurityQuestionActivity.this, RegistrationSecurityAnswerActivity.class);
        intent.putExtra("email", getIntent().getStringExtra("email"));
        intent.putExtra("password", getIntent().getStringExtra("password"));
        intent.putExtra("securityQuestionId", questionId);
        startActivityForResult(intent, REGISTRATION_REQUEST);
    }
}
