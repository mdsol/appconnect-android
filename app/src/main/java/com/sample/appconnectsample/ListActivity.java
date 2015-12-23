package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.model.Form;
import com.mdsol.babbage.model.Subject;
import com.mdsol.babbage.model.User;
import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.RequestException;
import java.util.ArrayList;
import java.util.List;

/**
 * The list activity retrieves available forms and displays them to the user.
 */
public class ListActivity extends AppCompatActivity {

    public static final String USER_ID_EXTRA = "appconnectsample.listactivity.intent.extra.USER_ID";

    private static final String TAG = "ListActivity";

    private ListView formList;
    private FormAdapter formAdapter;
    private View progressBar;
    private long userID;
    private List<Form> forms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        formList = (ListView)findViewById(R.id.list_form_list);
        formList.setAdapter(formAdapter = new FormAdapter());
        formList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doOpenForm(formAdapter.getItem(position));
            }
        });

        progressBar = findViewById(R.id.list_progress_bar);
        progressBar.setVisibility(View.GONE);

        // Get the ID of the user whose forms we want to display
        Intent intent = getIntent();
        userID = intent.getLongExtra(USER_ID_EXTRA, 0);

        // Populate the list with the forms that are already in the datastore
        populateForms();

        // Start an asynchronous task to sync the datastore
        new SyncTask().execute(userID);
    }

    private void populateForms() {
        forms.clear();

        // *** AppConnect ***
        // This is how the UI retrieves forms from the datastore for display.
        // The user could have multiple subjects if they're assigned to multiple
        // studies. Here we just gather all available forms, but you could also
        // present them organized by subject if desired.
        Datastore datastore = App.getUIDatastore(this);
        for (Subject subject : datastore.getSubjectsForUser(userID))
            forms.addAll(datastore.getAvailableFormsForSubject(subject.getID()));

        formAdapter.notifyDataSetChanged();
    }

    private void doOpenForm(Form form) {
        Intent intent;

        // *** AppConnect ***
        // Start an activity to fill out the form. If the form is from the SDK
        // sample CRF, we open FORM1 as a one-page form and FORM2 as a multi-page
        // form to demonstrate how to handle both cases.
        if (form.getParentSubject().getStudy().getName().equals("Sample_SDK")) {
            String formOID = form.getFormOID();
            if (formOID.equals("FORM1")) {
                intent = new Intent(ListActivity.this, OnePageFormActivity.class);
                intent.putExtra(OnePageFormActivity.FORM_ID_EXTRA, form.getID());
            }
            else { // formOID.equals("FORM2")
                intent = new Intent(ListActivity.this, MultiPageFormActivity.class);
                intent.putExtra(MultiPageFormActivity.FORM_ID_EXTRA, form.getID());
            }
        }
        else {
            // All other forms open as multi-page
            intent = new Intent(ListActivity.this, MultiPageFormActivity.class);
            intent.putExtra(MultiPageFormActivity.FORM_ID_EXTRA, form.getID());
        }

        startActivity(intent);
    }

    /**
     * An asynchronous task that syncs the subjects and forms available.
     */
    private class SyncTask extends AsyncTask<Long,Void,Void> {

        private RequestException exception;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Long... params) {
            // *** AppConnect ***
            // Babbage objects can't be shared between threads so you must pass
            // them around by ID instead and the receiving code can get its own
            // copy from its own datastore
            long userID = params[0];

            // *** AppConnect ***
            // Each secondary thread must create its own datastore instance and
            // dispose of it when done
            Datastore datastore = null;
            try {
                datastore = DatastoreFactory.create();
                User user = datastore.getUser(userID);

                // Get the subjects for the current user and then iterate over
                // the subjects to sync their forms. The objects returned from
                // these methods are only usable during the lifetime of this
                // temporary datastore.
                Client client = App.getClient(ListActivity.this);
                for (Subject subject : client.loadSubjects(datastore, user))
                    client.loadForms(datastore, subject);
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
            progressBar.setVisibility(View.GONE);
            if (exception != null) {
                Log.e(TAG, "The sync task failed", exception);
                new AlertDialog.Builder(ListActivity.this).
                    setTitle(R.string.sync_failed_title).
                    setMessage(R.string.sync_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else {
                populateForms();
            }
        }
    }

    private class FormAdapter extends ArrayAdapter<Form> {

        public FormAdapter() {
            super(ListActivity.this, 0, forms);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = getLayoutInflater().inflate(R.layout.form_row, parent, false);

            Form form = getItem(position);

            TextView tv = (TextView)v.findViewById(R.id.form_row_name_label);
            tv.setText(form.getName());

            return v;
        }
    }
}
