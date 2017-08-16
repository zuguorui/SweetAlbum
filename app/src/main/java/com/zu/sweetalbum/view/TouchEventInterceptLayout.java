package com.zu.sweetalbum.view;


import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by zu on 17-6-26.
 */

public class TouchEventInterceptLayout extends FrameLayout{

    public TouchEventInterceptLayout(@NonNull Context context) {
        super(context);
    }

    public TouchEventInterceptLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchEventInterceptLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchEventInterceptLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private OnTouchEventReceiveListener onTouchEventReceiveListener;

    public void setOnTouchEventReceiveListener(OnTouchEventReceiveListener listener)
    {
        onTouchEventReceiveListener = listener;
    }

    public OnTouchEventReceiveListener removeOnTouchEventReceiveListener()
    {
        OnTouchEventReceiveListener temp = onTouchEventReceiveListener;
        onTouchEventReceiveListener = null;
        return temp;
    }
    public void notifyOnTouchEventReceiveListener(MotionEvent event)
    {
        if(onTouchEventReceiveListener != null)
        {
            onTouchEventReceiveListener.onTouchEventReceive(event);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent tempEvent = MotionEvent.obtain(ev);
        notifyOnTouchEventReceiveListener(tempEvent);
        return super.onInterceptTouchEvent(ev);
    }

    public interface OnTouchEventReceiveListener
    {
        void onTouchEventReceive(MotionEvent event);
    }
}
