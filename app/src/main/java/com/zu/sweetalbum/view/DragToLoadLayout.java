package com.zu.sweetalbum.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.view.AlbumListView.DragLoadView;

/**
 * Created by zu on 2017/10/17.
 */

public class DragToLoadLayout extends FrameLayout{

    MyLog log = new MyLog("DragToLoadLayout", true);

    private DragLoadView upDragLoadView;
    private DragLoadView downDragLoadView;
    public DragToLoadLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragToLoadLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragToLoadLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public DragToLoadLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if(getChildCount() != 3)
        {
            throw new IllegalStateException("you must add 3 views to this DragLayout. The first view will be header and the last will be footer");
        }

        if(!(getChildAt(0) instanceof DragLoadView) || !(getChildAt(2) instanceof DragLoadView))
        {
            throw new IllegalStateException("the first and last child view should be DragLoadView");
        }

        upDragLoadView = (DragLoadView)getChildAt(0);
        downDragLoadView = (DragLoadView)getChildAt(2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed)
        {

        }
        Rect rect = getVisibleRect();
        upDragLoadView.layout(rect.left, rect.top - upDragLoadView.getMeasuredHeight(), rect.right, rect.top);
        downDragLoadView.layout(rect.left, rect.bottom, rect.right, rect.bottom + downDragLoadView.getMeasuredHeight());
        getChildAt(1).layout(rect.left, rect.top, rect.right, rect.bottom);
    }



    private Rect getVisibleRect()
    {
        int top = getPaddingTop();
        int left = getPaddingLeft();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean consumed = super.dispatchTouchEvent(ev);
        log.d("dispatchTouchEvent returns " + consumed);
        return consumed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
