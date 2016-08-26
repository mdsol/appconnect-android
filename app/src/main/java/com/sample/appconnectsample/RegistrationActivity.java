package com.sample.appconnectsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import com.mdsol.babbage.net.RequestException;

import java.util.HashMap;

/**
 * Created by kbohlmann on 7/21/16.
 * Simple base class for the dialogue sequence of registration activities.
 */
public abstract class RegistrationActivity extends AppCompatActivity {

    public static final int REGISTRATION_REQUEST = 1;

    public static final HashMap<RequestException.ErrorCause, String> errorMap = createErrorMap();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTRATION_REQUEST) {
            setResult(resultCode, data);
            finish();
        }
    }

    private static HashMap<RequestException.ErrorCause, String> createErrorMap() {
        HashMap<RequestException.ErrorCause, String> errorsMessages = new HashMap<RequestException.ErrorCause, String>();
        errorsMessages.put(RequestException.ErrorCause.EMAIL_ALREADY_EXISTS, "The user already exists in the study");
        errorsMessages.put(RequestException.ErrorCause.INVALID_PASSWORD, "Your password does not meet our requirements");
        return errorsMessages;
    }

    public String getErrorMessageFromException(RequestException exception) {

        String errorMessage = exception.getMessage();
        String userMessage = errorMap.get(exception.getErrorCause());

        if (userMessage != null)
        {
            errorMessage = userMessage;
        }

        return errorMessage;
    }

}
