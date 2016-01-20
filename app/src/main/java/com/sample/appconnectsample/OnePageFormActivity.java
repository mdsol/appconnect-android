package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.model.Field;
import com.mdsol.babbage.model.Form;
import com.mdsol.babbage.model.NumericField;
import com.mdsol.babbage.model.StepSequencer;
import com.mdsol.babbage.model.TextField;
import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.RequestException;
import java.text.DecimalFormatSymbols;

/**
 * An activity to fill out and submit a form with all of its fields on one page.
 * This is only appropriate for cases where the form doesn't have any branching
 * rules.
 */
public class OnePageFormActivity extends AppCompatActivity {

    public static final String FORM_ID_EXTRA = "appconnectsample.onepageformactivity.intent.extra.FORM_ID";

    private static final String TAG = "OnePageFormActivity";

    private long formID;

    private EditText field1Response;
    private EditText field2Response;
    private EditText field3Response;
    private char decimalCharacter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_page_form_activity);

        field1Response = (EditText)findViewById(R.id.one_page_form_field1_response);
        field2Response = (EditText)findViewById(R.id.one_page_form_field2_response);
        field3Response = (EditText)findViewById(R.id.one_page_form_field3_response);

        TextView field1Label = (TextView)findViewById(R.id.one_page_form_field1_label);
        TextView field2Label = (TextView)findViewById(R.id.one_page_form_field2_label);
        TextView field3Label = (TextView)findViewById(R.id.one_page_form_field3_label);

        decimalCharacter = DecimalFormatSymbols.getInstance().getDecimalSeparator();

        // Get the ID of the form to display
        Intent intent = getIntent();
        formID = intent.getLongExtra(FORM_ID_EXTRA, 0);

        // *** AppConnect ***
        // Get the corresponding form from the datastore
        Form form = App.getUIDatastore(this).getForm(formID);
        setTitle(form.getName());

        // *** AppConnect ***
        // Find the fields we know exist in the form and populate the view with
        // their properties. This is hardcoded for the specific case where we
        // know in advance that FIELD1 is a TextField and the other two are
        // NumericFields. If you don't know in advance what the fields are going
        // to be, look at MultiPageFormActivity instead.
        for (Field field : form.getFields()) {
            String fieldOID = field.getFieldOID();
            if (fieldOID.equals("TEXTFIELD1")) {
                TextField tf = (TextField)field;
                field1Label.setText(tf.getLabel());
                field1Response.setHint(getString(R.string.field_text_format, tf.getMaximumResponseLength()));
            }
            else if (fieldOID.equals("NUMBERS")) {
                NumericField nf = (NumericField)field;
                field2Label.setText(nf.getLabel());
                field2Response.setHint(getFormatForNumericField(nf));
            }
            else if (fieldOID.equals("NUMERICVALUE")) {
                NumericField nf = (NumericField)field;
                field3Label.setText(nf.getLabel());
                field3Response.setHint(getFormatForNumericField(nf));
            }
        }
    }

    public void doSubmitButton(View source) {
        new CollectAndSubmitTask().execute(formID);
    }

    private String getFormatForNumericField(NumericField field) {
        // *** AppConnect ***
        // This shows how to inspect a NumericField to discover the format of
        // the response it expects. Each field type has specific methods to
        // discover such properties. See the documentation.
        StringBuilder b = new StringBuilder();
        b.append(field.getMaximumResponseIntegerCount());
        if (field.isResponseIntegerCountRequired())
            b.append('+');
        b.append(decimalCharacter);
        b.append(field.getMaximumResponseDecimalCount());
        if (field.isResponseDecimalCountRequired())
            b.append('+');
        return b.toString();
    }

    /**
     * An asynchronous task that collects the responses and submits the form.
     */
    private class CollectAndSubmitTask extends AsyncTask<Long,Void,Void> {

        private ProgressDialog progressDialog;
        private String response1;
        private String response2;
        private String response3;
        private String message;
        private RequestException exception;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(OnePageFormActivity.this, ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.one_page_form_submitting_message));
            progressDialog.setCancelable(false);
            progressDialog.show();

            response1 = field1Response.getText().toString();
            response2 = field2Response.getText().toString();
            response3 = field3Response.getText().toString();
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

                // You must use a StepSequencer to fill out the form. This is
                // hardcoded for the specific case where we know in advance that
                // FIELD1 is a TextField and the other two are NumericFields. If
                // you don't know in advance what the fields are going to be,
                // look at MultiPageFormActivity.
                StepSequencer sequencer = new StepSequencer(form);
                sequencer.start();

                // Fill out the response for FIELD1, which we know is a TextField
                TextField field1 = (TextField)sequencer.getCurrentField();
                field1.setSubjectResponse(response1);
                if (field1.getResponseProblem() != Field.Problem.NONE) {
                    message = getString(R.string.one_page_form_error_invalid_response_message, "1");
                    return null;
                }

                sequencer.moveToNext();

                // Fill out the response for FIELD2, which we know is a NumericField
                NumericField field2 = (NumericField)sequencer.getCurrentField();
                field2.setSubjectResponse(field2.stringToResponse(response2, decimalCharacter));
                if (field2.getResponseProblem() != Field.Problem.NONE) {
                    message = getString(R.string.one_page_form_error_invalid_response_message, "2");
                    return null;
                }

                sequencer.moveToNext();

                // Fill out the response for FIELD3, which we know is a NumericField
                NumericField field3 = (NumericField)sequencer.getCurrentField();
                field3.setSubjectResponse(field3.stringToResponse(response3, decimalCharacter));
                if (field3.getResponseProblem() != Field.Problem.NONE) {
                    message = getString(R.string.one_page_form_error_invalid_response_message, "3");
                    return null;
                }

                // The sequencer must be in the reviewing state to be able to finish the form
                sequencer.moveToNext();
                if (sequencer.getState() != StepSequencer.State.REVIEWING) {
                    message = getString(R.string.one_page_form_error_more_fields_message);
                    return null;
                }

                // Once the form is completely filled out, you must call finish() to
                // stamp the form with the completion date and time. Attempting to
                // submit will fail if finish() hasn't been called. If the form requires
                // a signature, form.sign() should also be called before calling finish().
                if (!sequencer.finish()) {
                    message = getString(R.string.one_page_form_error_finish_failed_message);
                    return null;
                }

                // Tell the client to send the form responses
                Client client = App.getClient(OnePageFormActivity.this);
                client.sendResponses(form);
            }
            catch (RequestException ex) {
                exception = ex;
            }
            catch (ClassCastException ex) {
                message = getString(R.string.one_page_form_error_unexpected_field_type_message);
            }
            finally {
                if (datastore != null)
                    datastore.dispose();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.cancel();

            if (message != null) {
                Log.e(TAG, message);
                new AlertDialog.Builder(OnePageFormActivity.this).
                    setTitle(R.string.one_page_form_error_title).
                    setMessage(message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else if (exception != null) {
                Log.e(TAG, "The submit task failed", exception);
                new AlertDialog.Builder(OnePageFormActivity.this).
                    setTitle(R.string.submit_failed_title).
                    setMessage(R.string.submit_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else {
                new AlertDialog.Builder(OnePageFormActivity.this).
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
}
