package com.zu.sweetalbum.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.zu.sweetalbum.util.MyLog;

/**
 * Created by zu on 17-9-8.
 */

public class HideHeadLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild{

    private int touchSlop = 2;
    private int oldX, oldY, newX, newY, dx, dy;

    private final NestedScrollingChildHelper mChildHelper;
    private final NestedScrollingParentHelper mParentHelper;

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
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
    }



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

    /*NestedScrollingChild APIs*/

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    /*NestedScrollingParent APIs*/

    /*must override this method*/
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        mParentHelper.onStopNestedScroll(child);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }


}
