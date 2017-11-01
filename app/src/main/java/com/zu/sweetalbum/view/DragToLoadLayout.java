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
    private boolean layouted = false;

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
        /*此处由ZoomLayoutManager负责通知滑动。如果RecyclerView已经不能消耗滑动事件，那么就会由此处接受，然后视情况对View进行布局。此处触发时
        * UpView和DownView都应该没有显示*/
        @Override
        public boolean onScrollState(int originMoveDis, int movedDis) {
            if(originMoveDis == 0)
            {
                return false;
            }
            boolean moved = false;
            int realMoveDis = originMoveDis - movedDis;
            if(realMoveDis == 0)
            {
                return false;
            }


//            int offset = computeScrollOffset(realMoveDis);
//            if(offset != 0)
//            {
//                offsetChildrenVertical(offset);
//                notifyDragStat(false);
//                moved = true;
//            }

            Rect visibleRect = getVisibleRect();
            int offset = computeScrollOffset(originMoveDis);
            if(offset > 0)
            {

                if(upDragLoadView.getTop() + offset > visibleRect.top)
                {
                    offset = visibleRect.top - upDragLoadView.getTop();
                }
                if(offset != 0)
                {
                    offsetChildrenVertical(offset);
                    notifyDragStat(false);
                    moved = true;
                    log.d("onScrollState, move UpView, offset = " + offset);
                }
            }else
            {
                if(downDragLoadView.getBottom() + offset < visibleRect.bottom)
                {
                    offset = visibleRect.bottom - downDragLoadView.getBottom();
                }
                if(offset != 0)
                {
                    offsetChildrenVertical(offset);
                    notifyDragStat(false);
                    moved = true;
                    log.d("onScrollState, move DownView, offset = " + offset);
                }
            }
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

    }

    /**
     * 仅负责根据UpView以及DownView的位置进行弹性滑动。
     * */
    private int computeScrollOffset(int originDis)
    {
        Rect visibleRect = getVisibleRect();
        int mOffset = originDis;
        if(originDis == 0)
        {
            return 0;
        }



        if(upDragLoadView.getBottom() > visibleRect.top)
        {
            int a = Math.abs(visibleRect.top - upDragLoadView.getTop());
            int height = upDragLoadView.getMeasuredHeight();
            float process = a * 1.0f / height + 0.2f;
            if(process > 1.0f)
            {
                process = 1.0f;
            }
            int offset = (int)(process * mOffset);
            if(upDragLoadView.getBottom() + offset < visibleRect.top)
            {
                offset = visibleRect.top - upDragLoadView.getBottom();
            }
            if(upDragLoadView.getTop() + offset > visibleRect.top)
            {
                offset = visibleRect.top - upDragLoadView.getTop();
            }
            return offset;

        }else if(downDragLoadView.getTop() < visibleRect.bottom)
        {
            int a = Math.abs(visibleRect.bottom - downDragLoadView.getBottom());
            int height = downDragLoadView.getMeasuredHeight();
            float process = a * 1.0f / height + 0.2f;
            if(process > 1.0f)
            {
                process = 1.0f;
            }
            int offset = (int)(process * mOffset);
            if(downDragLoadView.getTop() + offset > visibleRect.bottom)
            {
                offset = visibleRect.bottom - downDragLoadView.getTop();
            }
            if(downDragLoadView.getBottom() + offset < visibleRect.bottom)
            {
                offset = visibleRect.bottom - downDragLoadView.getBottom();
            }
            return offset;
        }

        return originDis;
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

    /*此处需要注意，由于HideHeadLayout存在，因此该函数会经常执行，故布局函数还需重写*/
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed)
        {

            int childCount = getChildCount();
            if(childCount != 3)
            {
                throw new IllegalStateException("you must add 3 views to this DragLayout. The first view will be header and the last will be footer");
            }

            if(!(getChildAt(0) instanceof DragLoadView) || !(getChildAt(2) instanceof DragLoadView))
            {
                throw new IllegalStateException("the first and last child view should be DragLoadView");
            }
            Rect rect = getVisibleRect();

            if(upDragLoadView == null)
            {
                upDragLoadView = (DragLoadView)getChildAt(0);
                upDragLoadView.setOnLoadListener(upOnLoadListsner);
                upDragLoadView.layout(rect.left, rect.top - upDragLoadView.getMeasuredHeight(), rect.right, rect.top);
                getChildAt(1).layout(rect.left, rect.top, rect.right, rect.bottom);
            }
            if(downDragLoadView == null)
            {
                downDragLoadView = (DragLoadView)getChildAt(2);
                downDragLoadView.setOnLoadListener(downOnLoadListener);
                downDragLoadView.layout(rect.left, rect.bottom, rect.right, rect.bottom + downDragLoadView.getMeasuredHeight());
            }


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

    /**
     * 决定是否拦截此事件并且对view进行滚动。当UpView或DownView有一个是可见的时候，根据滚动距离滚动并返回true，代表拦截此事件。
     * 当UpView或DownView都不可见时返回false，代表不拦截此事件。此时会将事件交给RecyclerView。
     * */
    private boolean detectScrollWhenDrag(int dis)
    {
        int mDis = dis;
        Rect visibleRect = getVisibleRect();
        if(dis == 0)
        {
            return false;
        }

        if(upDragLoadView.getBottom() > visibleRect.top)
        {
            if(mDis > 0)
            {
                int offset = computeScrollOffset(mDis);
                if(offset != 0)
                {
                    offsetChildrenVertical(offset);
                    notifyDragStat(false);

                }

            }else
            {
                if(upDragLoadView.getBottom() + mDis < visibleRect.top)
                {
                    mDis = visibleRect.top - upDragLoadView.getBottom();
                }
                if(mDis != 0)
                {
                    offsetChildrenVertical(mDis);
                    notifyDragStat(false);
                }

            }
            return true;
        }else if(downDragLoadView.getTop() < visibleRect.bottom)
        {
            if(mDis < 0)
            {
                int offset = computeScrollOffset(mDis);
                if(offset != 0)
                {
                    offsetChildrenVertical(offset);
                    notifyDragStat(false);

                }
            }else
            {
                if(downDragLoadView.getTop() + mDis > visibleRect.bottom)
                {
                    mDis = visibleRect.bottom - downDragLoadView.getTop();
                }
                if(mDis != 0)
                {
                    offsetChildrenVertical(mDis);
                    notifyDragStat(false);
                }

            }
            return true;
        }else
        {
            return false;
        }

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
                /*当UpView或者DownView已经在显示时，由此处接管事件。当滑动至UpView以及DownView都消失时，应将事件派发给RecyclerView查看是否会消耗*/
                oldX = newX;
                oldY = newY;
                newX = (int)ev.getX();
                newY = (int)ev.getY();
                int dy = newY - oldY;
                if(Math.abs(dy) >= touchSlop)
                {
                    consumed = detectScrollWhenDrag(dy);
                    log.d("detectScrollWhenDrag returns " + consumed);
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
