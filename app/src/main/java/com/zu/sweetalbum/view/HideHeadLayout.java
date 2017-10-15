package com.zu.sweetalbum.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.zu.sweetalbum.util.MyLog;

/**
 * Created by zu on 17-9-8.
 */

public class HideHeadLayout extends LinearLayout{
    private int touchSlop = 4;
    private int oldX, oldY, newX, newY, dx, dy;

    private MyLog log = new MyLog("HideHeadLayout", true);

    private boolean firstDispatch = true;

    public HideHeadLayout(Context context) {
        this(context, null);
    }

    public HideHeadLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HideHeadLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HideHeadLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setClickable(true);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        log.d("ev achieve");
//        boolean intercepted = false;
//        int count = ev.getPointerCount();
//        if(count > 1)
//        {
//            return false;
//        }
//        View header = getChildAt(0);
//        switch (ev.getActionMasked())
//        {
//            case MotionEvent.ACTION_DOWN:
//                newX = oldX = (int)ev.getX();
//                newY = oldY = (int)ev.getY();
//                intercepted = false;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                oldX = newX;
//                oldY = newY;
//                newX = (int)ev.getX();
//                newY = (int)ev.getY();
//                dx = newX - oldX;
//                dy = newY - oldY;
//                if(Math.abs(dy) >= touchSlop)
//                {
//                    intercepted = scrollViewY(dy);
//                }else
//                {
//                    intercepted = false;
//                }
//                log.d("ACTION_MOVE, intercepted = " + intercepted);
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                firstDispatch = true;
//                intercepted = false;
//                break;
//            default:
//                intercepted = false;
//                break;
//
//        }
//        return false;
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        log.d("dispatchTouchEvent");
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
                if(Math.abs(dy) >= touchSlop)
                {
                    intercepted = scrollViewY(dy);
                }else
                {
                    intercepted = false;
                }
                log.d("ACTION_MOVE, intercepted = " + intercepted);
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
        if(!intercepted)
        {
            return super.dispatchTouchEvent(ev);
        }else
        {
            return intercepted;
        }

    }

    private boolean scrollViewY(int dy)
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
