package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.model.Field;
import com.mdsol.babbage.model.Form;
import com.mdsol.babbage.model.StepSequencer;
import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.RequestException;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity to fill out and submit a form with fields shown sequentially.
 * This is appropriate for all forms and will honor any branching rules the form
 * might have.
 */
public class MultiPageFormActivity extends AppCompatActivity {

    public static final String FORM_ID_EXTRA = "appconnectsample.multipageformactivity.intent.extra.FORM_ID";

    private static final String TAG = "MultiPageFormActivity";

    private Button previousButton;
    private Button nextButton;
    private Button submitButton;
    private ViewPager fieldPager;
    private FieldAdapter fieldAdapter;
    private ProgressBar progressBar;
    private long formID;
    private StepSequencer sequencer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_page_form_activity);

        previousButton = (Button)findViewById(R.id.form_previous_button);
        nextButton = (Button)findViewById(R.id.form_next_button);
        submitButton = (Button)findViewById(R.id.form_submit_button);

        fieldPager = (ViewPager)findViewById(R.id.form_pager);
        fieldPager.setAdapter(fieldAdapter = new FieldAdapter());

        progressBar = (ProgressBar)findViewById(R.id.form_progress_bar);
        progressBar.setVisibility(View.GONE);

        // Get the ID of the form to display
        Intent intent = getIntent();
        formID = intent.getLongExtra(FORM_ID_EXTRA, 0);

        // *** AppConnect ***
        // Get the corresponding form from the datastore
        Datastore datastore = App.getUIDatastore(this);
        Form form = datastore.getForm(formID);
        setTitle(form.getName());

        // *** AppConnect ***
        // You must use a StepSequencer to fill out the form. Calling start()
        // will clear out all the field responses and begin on the first field.
        // You could also verify whether form.canResume() returns true and call
        // resume() instead, which would preserve any previously answered field
        // and begin where the user was last.
        sequencer = new StepSequencer(form);
        sequencer.start();

        // *** AppConnect ***
        // Show the first field of the form as indicated by the sequencer
        fieldAdapter.addField(sequencer.getCurrentField());

        updateButtonsVisible();
    }

    public void doPreviousButton(View source) {
        // *** AppConnect ***
        // Tell the sequencer to move back to the previous field. This can fail
        // if there are no previous fields.
        if (sequencer.moveToPrevious(false)) {
            // Move to the previous page
            int position = fieldPager.getCurrentItem();
            fieldPager.setCurrentItem(position - 1);

            updateButtonsVisible();
        }
        else {
            new AlertDialog.Builder(this).
                setTitle(R.string.multi_page_form_previous_failed_title).
                setMessage(R.string.multi_page_form_previous_failed_message).
                setPositiveButton(R.string.ok_button, null).
                show();
        }
    }

    public void doNextButton(View source) {
        // *** AppConnect ***
        // Tell the sequencer to move forward to the next field. This can fail
        // if the response for the current field is invalid (i.e. its problem
        // property has a fatal value, see the documentation), or if there are
        // no more steps in the form.
        if (sequencer.moveToNext()) {
            // Create the next page if needed
            int position = fieldPager.getCurrentItem();
            if (position == fieldAdapter.getCount() - 1) {
                // The last step of the form is the REVIEWING state, in which
                // case getCurrentField() will return null
                Field field = sequencer.getCurrentField();
                if (field != null)
                    fieldAdapter.addField(field);
                else
                    fieldAdapter.addReview();
            }

            // Move to the next page
            fieldPager.setCurrentItem(position + 1);

            updateButtonsVisible();
        }
        else {
            new AlertDialog.Builder(this).
                setTitle(R.string.multi_page_form_next_failed_title).
                setMessage(R.string.multi_page_form_next_failed_message).
                setPositiveButton(R.string.ok_button, null).
                show();
        }
    }

    public void doSubmitButton(View source) {
        // *** AppConnect ***
        // Once the form is completely filled out, you must call finish() to
        // stamp the form with the completion date and time. Attempting to
        // submit will fail if finish() hasn't been called. If the form requires
        // a signature, form.sign() should also be called before calling finish().
        if (!sequencer.finish()) {
            new AlertDialog.Builder(this).
                setTitle(R.string.multi_page_form_error_finish_failed_title).
                setMessage(R.string.multi_page_form_error_finish_failed_message).
                setPositiveButton(R.string.ok_button, null).
                show();
            return;
        }

        new SubmitTask().execute(formID);
    }

    private void updateButtonsVisible() {
        boolean reviewing = (sequencer.getState() == StepSequencer.State.REVIEWING);
        previousButton.setVisibility(fieldPager.getCurrentItem() == 0 ? View.GONE : View.VISIBLE);
        nextButton.setVisibility(reviewing ? View.GONE : View.VISIBLE);
        submitButton.setVisibility(reviewing ? View.VISIBLE : View.GONE);
    }

    /**
     * An asynchronous task that submits a form.
     */
    private class SubmitTask extends AsyncTask<Long,Void,Void> {

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
            long formID = params[0];

            // *** AppConnect ***
            // Each secondary thread must create its own datastore instance and
            // dispose of it when done
            Datastore datastore = null;
            try {
                datastore = DatastoreFactory.create();
                Form form = datastore.getForm(formID);

                // The StepSequencer has already validated and timestamped the
                // form so all we need to do here is tell the client to send the
                // form responses
                Client client = App.getClient(MultiPageFormActivity.this);
                client.sendResponses(form);
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
                Log.e(TAG, "The submit task failed", exception);
                new AlertDialog.Builder(MultiPageFormActivity.this).
                    setTitle(R.string.submit_failed_title).
                    setMessage(R.string.submit_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else {
                new AlertDialog.Builder(MultiPageFormActivity.this).
                    setCancelable(false).
                    setTitle(R.string.submit_succeeded_title).
                    setMessage(R.string.submit_succeeded_message).
                    setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).
                    show();
            }
        }
    }

    private class FieldAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public FieldAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        public void addField(Field field) {
            // You may have different kinds of fragment for different types of
            // field. Here we use one fragment that can handle all field types.
            FieldFragment f = new FieldFragment();

            // Give the fragment the ID of the field to display
            Bundle args = new Bundle();
            args.putLong(FieldFragment.FIELD_ID_ARG, field.getID());
            f.setArguments(args);

            fragments.add(f);
            notifyDataSetChanged();
        }

        public void addReview() {
            ReviewFragment f = new ReviewFragment();

            fragments.add(f);
            notifyDataSetChanged();
        }
    }
}
