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

    private boolean firstDispatch = true;

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
        View header = getChildAt(0);
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
                if(Math.abs(dy) >= touchSlop)
                {
                    if((dy > 0 && header.getTop() < getPaddingTop()) ||
                            (dy < 0 && header.getBottom() > getPaddingTop()))
                        scrollViewY(dy, ev);
                    intercepted = true;
                }else
                {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                firstDispatch = true;
                intercepted = false;
                break;
            default:
                intercepted = false;
                break;

        }
        return false;
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
                if(Math.abs(dy) >= touchSlop)
                {
                    consumed = scrollViewY(dy, event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                firstDispatch = true;
                consumed = false;
                break;
            default:
                consumed = false;
                break;
        }
        return consumed;
    }

    private boolean scrollViewY(int dy, MotionEvent event)
    {
        boolean scrolled = false;
        int realDy = dy;
        if(getChildCount() < 2)
        {
            return false;
        }
        View header = getChildAt(0);
        if(header.getTop() + realDy > getPaddingTop())
        {
            realDy = getPaddingTop() - header.getTop();
        }
        if(header.getBottom() + realDy < getPaddingTop())
        {
            realDy = getPaddingTop() - header.getBottom();
        }
        if(realDy == 0)
        {
//            requestDisallowInterceptTouchEvent(true);
            return false;
        }
        MarginLayoutParams layoutParams = (MarginLayoutParams) header.getLayoutParams();
        layoutParams.topMargin += realDy;
        header.setLayoutParams(layoutParams);
//        header.offsetTopAndBottom(realDy);
//        requestLayout();
        scrolled = true;
        return scrolled;
    }


}
