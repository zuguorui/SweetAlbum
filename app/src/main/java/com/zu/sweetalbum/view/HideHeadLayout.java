package com.zu.sweetalbum.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by zu on 17-9-8.
 */

public class HideHeadLayout extends LinearLayout{
    private int touchSlop = 4;
    private int oldX, oldY, newX, newY, dx, dy;

    public HideHeadLayout(Context context) {
        super(context);
    }

    public HideHeadLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HideHeadLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HideHeadLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int count = ev.getPointerCount();
        if(count > 1)
        {
            return false;
        }
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                newX = oldX = (int)ev.getX();
                newY = oldY = (int)ev.getY();
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                oldX = newX;
                oldY = newY;
                newX = (int)ev.getX();
                newY = (int)ev.getY();
                dx = newX - oldX;
                dy = newY - oldY;
                if(Math.abs(dx) >= touchSlop)
                {
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                intercepted = false;
                break;

        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = true;
        if (event.getPointerCount() > 1) {
            return false;
        }
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                newX = oldX = (int)event.getX();
                newY = oldY = (int)event.getY();
                consumed = false;
                break;
            case MotionEvent.ACTION_MOVE:
                oldX = newX;
                oldY = newY;
                newX = (int)event.getX();
                newY = (int)event.getY();
                dx = newX - oldX;
                dy = newY - oldY;
                if(Math.abs(dx) >= touchSlop)
                {
                    consumed = scrollViewY(dy);
                }
                break;
            case MotionEvent.ACTION_UP:
                consumed = false;
                break;
            default:
                consumed = false;
                break;
        }
        return consumed;
    }

    private boolean scrollViewY(int dy)
    {
        boolean scrolled = false;
        int realDy = 0;
        if(getChildCount() < 2)
        {
            return false;
        }
        View header = getChildAt(0);
        if(header.getTop() + dy > getPaddingTop())
        {
            realDy = getPaddingTop() - header.getTop();
        }
        if(header.getBottom() + dy < getPaddingTop())
        {
            realDy = getPaddingTop() - header.getBottom();
        }
        if(realDy == 0)
        {
            return false;
        }
        MarginLayoutParams layoutParams = (MarginLayoutParams) header.getLayoutParams();
        layoutParams.topMargin += realDy;
        header.setLayoutParams(layoutParams);
        requestLayout();
        scrolled = true;
        return scrolled;
    }
}
