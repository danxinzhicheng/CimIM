package com.cooyet.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

/**
 * Created by user on 2018/6/13.
 */

public class FloatingWindowView extends LinearLayout {
    public FloatingWindowView(Context context) {
        super(context);
    }

    public FloatingWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
            if (mOnKeyListener != null) {
                mOnKeyListener.onKey(this, KeyEvent.KEYCODE_BACK, event);
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    OnKeyListener mOnKeyListener = null;

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        mOnKeyListener = l;

        super.setOnKeyListener(l);
    }
}
