package com.zu.sweetalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
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
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;

/**
 * Created by zu on 2017/10/17.
 */

public class DragToLoadLayout extends FrameLayout{

    MyLog log = new MyLog("DragToLoadLayout", true);

    private DragLoadView upDragLoadView;
    private DragLoadView downDragLoadView;

    private boolean isOnTouch = false;

    private DragLoadView.OnLoadListener upOnLoadListsner = new DragLoadView.OnLoadListener() {
        @Override
        public void onLoadComplete(boolean success) {

        }

        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadCancel() {

        }
    };

    private DragLoadView.OnLoadListener downOnLoadListener = new DragLoadView.OnLoadListener() {
        @Override
        public void onLoadComplete(boolean success) {

        }

        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadCancel() {

        }
    };

    private ValueAnimator animator;

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int dis = (int)animation.getAnimatedValue();
            offsetChildrenVertical(dis);
        }
    };


    private int oldX, oldY, newX, newY;
    private int touchSlop = 3;

    private ZoomLayoutManager.OnScrollStateListener onScrollStateListener = new ZoomLayoutManager.OnScrollStateListener() {
        @Override
        public boolean onScrollState(int originMoveDis, int movedDis) {
            if(originMoveDis == 0)
            {
                return false;
            }
            boolean moved = false;
            Rect visibleRect = getVisibleRect();
            int realMoveDis = originMoveDis - movedDis;
            if(realMoveDis == 0)
            {
                return false;
            }
            int offset = computeScrollOffset(realMoveDis);
            if(offset != 0)
            {
                offsetChildrenVertical(offset);
                notifyDragStat(false);
                moved = true;
            }
//            if(originMoveDis > 0)
//            {
//
//                if(getChildAt(0).getTop() + realMoveDis > visibleRect.top)
//                {
//                    realMoveDis = visibleRect.top - getChildAt(0).getTop();
//                }
//                if(realMoveDis != 0)
//                {
//                    offsetChildrenVertical(realMoveDis);
//                    notifyDragStat(false);
//                    moved = true;
//                }
//            }else
//            {
//                if(getChildAt(getChildCount() - 1).getBottom() + realMoveDis < visibleRect.bottom)
//                {
//                    realMoveDis = visibleRect.bottom - getChildAt(getChildCount() - 1).getBottom();
//                }
//                if(realMoveDis != 0)
//                {
//                    offsetChildrenVertical(realMoveDis);
//                    notifyDragStat(false);
//                    moved = true;
//                }
//            }
            return moved;
        }
    };

    public ZoomLayoutManager.OnScrollStateListener getOnScrollStateListener()
    {
        return onScrollStateListener;
    }


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
        upDragLoadView.setOnLoadListener(upOnLoadListsner);
        downDragLoadView.setOnLoadListener(downOnLoadListener);

    }

    private int computeScrollOffset(int originDis)
    {
        Rect visibleRect = getVisibleRect();
        int mOffset = originDis;
        if(upDragLoadView.getBottom() > visibleRect.top)
        {
            int a = Math.abs(upDragLoadView.getBottom() - visibleRect.top);
            int height = upDragLoadView.getMeasuredHeight();
            float process = a * 1.0f / height;
            if(process > 1.0f)
            {
                process = 1.0f;
            }
            int offset = (int)(process * mOffset);
            if(upDragLoadView.getBottom() + offset < visibleRect.top)
            {
                offset = visibleRect.top - upDragLoadView.getBottom();
            }
            return offset;
        }else if(downDragLoadView.getTop() < visibleRect.bottom)
        {
            int a = Math.abs(downDragLoadView.getTop() - visibleRect.bottom);
            int height = downDragLoadView.getMeasuredHeight();
            float process = a * 1.0f / height;
            if(process > 1.0f)
            {
                process = 1.0f;
            }
            int offset = (int)(process * mOffset);
            if(downDragLoadView.getTop() + offset > visibleRect.bottom)
            {
                offset = visibleRect.bottom - downDragLoadView.getTop();
            }
            return offset;
        }

        return 0;
    }

    private void offsetChildrenVertical(int offset)
    {

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            child.offsetTopAndBottom(offset);
        }
    }

    private void notifyDragStat(boolean release)
    {
        Rect visibleRect = getVisibleRect();
        if(upDragLoadView.getBottom() > visibleRect.top)
        {
            int offset = Math.abs(upDragLoadView.getBottom() - visibleRect.top);
            int height = upDragLoadView.getMeasuredHeight();
            float process = offset * 1.0f / height;
            if(release)
            {
                upDragLoadView.onDragRelease(process);
            }else
            {
                upDragLoadView.onDrag(process);
            }

        }else if(downDragLoadView.getTop() < visibleRect.bottom)
        {
            int offset = Math.abs(downDragLoadView.getTop() - visibleRect.bottom);
            int height = downDragLoadView.getMeasuredHeight();
            float process = offset * 1.0f / height;
            if(release)
            {
                downDragLoadView.onDragRelease(process);
            }else
            {
                downDragLoadView.onDrag(process);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed)
        {
            Rect rect = getVisibleRect();
            upDragLoadView.layout(rect.left, rect.top - upDragLoadView.getMeasuredHeight(), rect.right, rect.top);
            downDragLoadView.layout(rect.left, rect.bottom, rect.right, rect.bottom + downDragLoadView.getMeasuredHeight());
            getChildAt(1).layout(rect.left, rect.top, rect.right, rect.bottom);
        }

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

    private boolean detectScroll(int dis)
    {
        int mDis = dis;
        Rect visibleRect = getVisibleRect();
        if(dis == 0)
        {
            return false;
        }else if(dis > 0)
        {
            View foot = getChildAt(2);
            if(foot.getTop() < visibleRect.bottom)
            {
                if(foot.getTop() + mDis > visibleRect.bottom)
                {
                    mDis = visibleRect.bottom - foot.getTop();
                }
                if(mDis != 0)
                {
                    offsetChildrenVertical(mDis);
                    notifyDragStat(isOnTouch);
                    return true;
                }
            }
        }else
        {
            View head = getChildAt(0);
            if(head.getBottom() > visibleRect.top)
            {
                if(head.getBottom() + mDis < visibleRect.top)
                {
                    mDis = visibleRect.top - head.getBottom();
                }
                if(mDis != 0)
                {
                    offsetChildrenVertical(mDis);
                    notifyDragStat(isOnTouch);
                    return true;
                }
            }
        }

        return false;
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        boolean consumed = false;
        if(ev.getPointerCount() != 1)
        {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                newX = (int)ev.getX();
                newY = (int)ev.getY();
                oldX = newX;
                oldY = newY;
                isOnTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                oldX = newX;
                oldY = newY;
                newX = (int)ev.getX();
                newY = (int)ev.getY();
                int dy = newY - oldY;
                if(Math.abs(dy) >= touchSlop)
                {
                    consumed = onScrollStateListener.onScrollState(dy, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                isOnTouch = false;
                notifyDragStat(true);
                break;
            default:
                break;

        }
        if(!consumed)
        {
            consumed = super.dispatchTouchEvent(ev);
        }
        log.d("dispatchTouchEvent returns " + consumed);
        return consumed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }


}
