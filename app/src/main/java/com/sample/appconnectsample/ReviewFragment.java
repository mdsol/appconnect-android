package com.sample.appconnectsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment used by {@link MultiPageFormActivity} to show the user that the
 * form is all filled out. For this sample we don't do anything but you could
 * have, for example, a summary of the user's answers that they can review.
 */
public class ReviewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.review_fragment, container, false);
    }
}
