package com.sample.appconnectsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DateTimeField;
import com.mdsol.babbage.model.DictionaryField;
import com.mdsol.babbage.model.DictionaryResponse;
import com.mdsol.babbage.model.DiscreteField;
import com.mdsol.babbage.model.Field;
import com.mdsol.babbage.model.NumericField;
import com.mdsol.babbage.model.RaveDateFormat;
import com.mdsol.babbage.model.ScaleField;
import com.mdsol.babbage.model.TextField;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;

/**
 * A fragment used by {@link MultiPageFormActivity} to display an individual
 * field and let the user enter a response. This fragment can handle all field
 * types, but in a more complete application you would typically have different
 * kinds of fragment for different types of field.
 */
public class FieldFragment extends Fragment {

    public static final String FIELD_ID_ARG = "fieldID";

    private static final String TAG = "FieldFragment";

    private TextView oidField;
    private TextView typeField;
    private TextView headerField;
    private TextView numberField;
    private TextView labelField;
    private TextView formatField;
    private TextView problemField;
    private EditText responseField;
    private View responseScale;
    private TextView responseScaleLabel;
    private SeekBar responseScaleSlider;
    private Field field;
    private char decimalCharacter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decimalCharacter = DecimalFormatSymbols.getInstance().getDecimalSeparator();

        // Get the ID of the field to display
        Bundle args = getArguments();
        long fieldID = args.getLong(FIELD_ID_ARG);

        // *** AppConnect ***
        // Get the corresponding field from the datastore
        Datastore datastore = App.getUIDatastore(getActivity());
        field = datastore.getField(fieldID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.field_fragment, container, false);

        oidField = (TextView)v.findViewById(R.id.field_oid_field);
        typeField = (TextView)v.findViewById(R.id.field_type_field);
        headerField = (TextView)v.findViewById(R.id.field_header_field);
        numberField = (TextView)v.findViewById(R.id.field_number_field);
        labelField = (TextView)v.findViewById(R.id.field_label_field);
        formatField = (TextView)v.findViewById(R.id.field_format_field);
        problemField = (TextView)v.findViewById(R.id.field_problem_field);

        responseField = (EditText)v.findViewById(R.id.field_response_field);
        responseField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateFieldResponse();
            }
        });

        responseScale = v.findViewById(R.id.field_response_scale);
        responseScaleLabel = (TextView)v.findViewById(R.id.field_response_scale_label);
        responseScaleSlider = (SeekBar)v.findViewById(R.id.field_response_scale_slider);
        responseScaleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    updateFieldResponse();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // *** AppConnect ***
        // Populate the views with the field information
        oidField.setText(field.getFieldOID());
        typeField.setText(field.getFieldType().toString());
        headerField.setText(field.getHeader());
        numberField.setText(field.getFieldNumber());
        labelField.setText(field.getLabel());
        formatField.setText(getFormatForField(field));
        problemField.setText(field.getResponseProblem().toString());

        // For scale field types we hide the EditText and show a SeekBar instead
        switch (field.getFieldType()) {
            case NRS:
            case VAS:
            case VAS_BOX:
                ScaleField sf = (ScaleField)field;
                responseScaleLabel.setText(" ");
                responseScaleSlider.setMax(sf.getMaximumResponse() - sf.getMinimumResponse());
                responseField.setVisibility(View.GONE);
                responseScale.setVisibility(View.VISIBLE);
                break;
            default:
                responseField.setVisibility(View.VISIBLE);
                responseScale.setVisibility(View.GONE);
                break;
        }

        // For field types that use the EditText, we set an appropriate keyboard
        int inputType = InputType.TYPE_NULL;
        switch (field.getFieldType()) {
            case TEXT:
                inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
                break;
            case NUMERIC:
                inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
                break;
            case DATE_TIME:
                inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
                break;
            case DICTIONARY:
            case BRISTOL:
            case WONG_BAKER:
                inputType = InputType.TYPE_CLASS_NUMBER;
                break;
        }
        responseField.setInputType(inputType);

        return v;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        Context ctx = getActivity();
        if (ctx != null) {
            // Show the keyboard when the EditText is used, hide it when the
            // SeekBar is used
            InputMethodManager mgr = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            switch (field.getFieldType()) {
                case NRS:
                case VAS:
                case VAS_BOX:
                    mgr.hideSoftInputFromWindow(responseField.getWindowToken(), 0);
                    break;
                default:
                    mgr.showSoftInput(responseField, 0);
                    break;
            }
        }
    }

    private String getFormatForField(Field field) {
        // *** AppConnect ***
        // This shows how to inspect the different types of field to discover
        // the format of the response they expect. Each field type has specific
        // methods to discover such properties. See the documentation.
        switch (field.getFieldType()) {
            case TEXT: {
                TextField tf = (TextField)field;
                return getString(R.string.field_text_format, tf.getMaximumResponseLength());
            }
            case NUMERIC: {
                NumericField nf = (NumericField)field;
                StringBuilder b = new StringBuilder();
                b.append(nf.getMaximumResponseIntegerCount());
                if (nf.isResponseIntegerCountRequired())
                    b.append('+');
                b.append(decimalCharacter);
                b.append(nf.getMaximumResponseDecimalCount());
                if (nf.isResponseDecimalCountRequired())
                    b.append('+');
                return b.toString();
            }
            case DATE_TIME: {
                DateTimeField dtf = (DateTimeField)field;
                return dtf.getDateTimeFormat();
            }
            case DICTIONARY: {
                DictionaryField df = (DictionaryField)field;
                List<DictionaryResponse> responses = df.getPossibleResponses();
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < responses.size(); i++) {
                    if (b.length() != 0)
                        b.append("\n");
                    b.append(getString(R.string.field_dictionary_format, i + 1, responses.get(i).getUserValue()));
                }
                return b.toString();
            }
            case BRISTOL:
            case WONG_BAKER: {
                DiscreteField df = (DiscreteField)field;
                List<Integer> responses = df.getPossibleResponses();
                StringBuilder b = new StringBuilder();
                for (int response : responses) {
                    if (b.length() != 0)
                        b.append(", ");
                    b.append(response);
                }
                return getString(R.string.field_discrete_format, b.toString());
            }
            case NRS:
            case VAS:
            case VAS_BOX: {
                ScaleField sf = (ScaleField)field;
                return getString(R.string.field_scale_format, sf.getMinimumResponse(), sf.getMaximumResponse());
            }
        }
        return null;
    }

    private void updateFieldResponse() {
        String text = responseField.getText().toString();

        // *** AppConnect ***
        // This shows how to fill out the response for the different types of
        // field. Each field type has a specialized version of setSubjectResponse()
        // so Field must be cast to its specific subclass. See the documentation.
        switch (field.getFieldType()) {
            case TEXT: {
                TextField tf = (TextField)field;
                tf.setSubjectResponse(text);
                break;
            }
            case NUMERIC: {
                NumericField nf = (NumericField)field;
                long[] response = nf.stringToResponse(text, decimalCharacter);
                nf.setSubjectResponse(response);
                break;
            }
            case DATE_TIME: {
                DateTimeField dtf = (DateTimeField)field;
                RaveDateFormat dateFormat = new RaveDateFormat(dtf.getDateTimeFormat());
                try {
                    Date response = dateFormat.parse(text);
                    dtf.setSubjectResponse(response);
                }
                catch (IllegalArgumentException ex) {
                    dtf.setSubjectResponse(null);
                    Log.e(TAG, ex.getMessage());
                }
                break;
            }
            case DICTIONARY: {
                DictionaryField df = (DictionaryField)field;
                try {
                    int index = Integer.parseInt(text) - 1;
                    DictionaryResponse response = df.getPossibleResponses().get(index);
                    df.setSubjectResponse(response);
                }
                catch (IndexOutOfBoundsException|NumberFormatException ex) {
                    df.setSubjectResponse(null);
                    Log.e(TAG, ex.getMessage());
                }
                break;
            }
            case BRISTOL:
            case WONG_BAKER: {
                DiscreteField df = (DiscreteField)field;
                try {
                    int response = Integer.parseInt(text);
                    df.setSubjectResponse(response);
                }
                catch (NumberFormatException ex) {
                    df.setSubjectResponse(null);
                    Log.e(TAG, ex.getMessage());
                }
                break;
            }
            case NRS:
            case VAS:
            case VAS_BOX: {
                ScaleField sf = (ScaleField)field;
                int response = responseScaleSlider.getProgress() + sf.getMinimumResponse();
                responseScaleLabel.setText(String.valueOf(response));
                sf.setSubjectResponse(response);
                break;
            }
        }

        // *** AppConnect ***
        // The response problem of a field is updated every time you call
        // setSubjectResponse(), so afterward you can check whether there are
        // any problems with the response and display them to the user. Here we
        // just display the value, but you could also disable buttons or display
        // instructive messages.
        problemField.setText(field.getResponseProblem().toString());
    }
}
