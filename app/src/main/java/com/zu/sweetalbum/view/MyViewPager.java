package com.zu.sweetalbum.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zu on 17-6-22.
 */

public class MyViewPager extends ViewPager {

    private boolean scrollEnable = true;
    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollEnable(boolean enable)
    {
        scrollEnable = enable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!scrollEnable)
        {
            return false;
        }else
        {
            return super.onInterceptTouchEvent(ev);
        }
    }
}
