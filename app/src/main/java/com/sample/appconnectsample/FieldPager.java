package com.sample.appconnectsample;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A simple subclass of ViewPager that disables the ability for the user to
 * swipe between pages using touch. Used by {@link MultiPageFormActivity}.
 */
public class FieldPager extends ViewPager {

    public FieldPager(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
