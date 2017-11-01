package com.zu.sweetalbum.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zu.sweetalbum.util.MyLog;

/**
 * Created by zu on 17-9-8.
 */

public class HideHeadLayout extends ViewGroup {
    private int touchSlop = 2;
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
            return super.dispatchTouchEvent(ev);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed)
        {
            int top = getPaddingTop();
            int left = getPaddingLeft();
            int right = getMeasuredWidth() - getPaddingRight();
            for(int i = 0; i < getChildCount(); i++)
            {
                View view = getChildAt(i);
                view.layout(left, top, right, top + view.getMeasuredHeight());
                top += view.getMeasuredHeight();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getChildCount() != 2)
        {
            throw new IllegalArgumentException("HideHeadLayout must have 2 views, the first is head, and the second as content");
        }
        View head = getChildAt(0);
        int heightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);
        int widthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        head.measure(widthSpec, heightSpec);

        View content = getChildAt(1);
        heightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
        content.measure(widthSpec, heightSpec);

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));


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
            return false;
        }
        offsetChildrenVertical(realDy);

        scrolled = true;
        return scrolled;
    }

    private void offsetChildrenVertical(int dy)
    {
        for(int i = 0; i < getChildCount(); i++)
        {
            getChildAt(i).offsetTopAndBottom(dy);
        }
    }


}
