package com.zu.sweetalbum.view.AlbumListView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by zu on 2017/9/18.
 */

public abstract class DragLoadView extends FrameLayout {

    private OnLoadListener onLoadListener = null;
    private OnDragListener onDragListener = null;

    public DragLoadView(@NonNull Context context) {
        super(context);

    }

    public void setOnLoadListener(OnLoadListener loadListener)
    {
        this.onLoadListener = loadListener;
    }

    public void removeOnLoadListener()
    {
        this.onLoadListener = null;
    }

    public void setOnDragListener(OnDragListener listener)
    {
        this.onDragListener = listener;
    }

    public void removeDragListener()
    {
        this.onDragListener = null;
    }

    public void onDrag(float process){
        if(onDragListener != null)
        {
            onDragListener.onDrag(process);
        }
    }

    public void onDragRelease(float process)
    {
        if(onDragListener != null)
        {
            onDragListener.onDragRelease(process);
        }
    }

    public void onDragStart()
    {
        if(onDragListener != null)
        {
            onDragListener.onDragStart();
        }
    }

    public void onLoadComplete(boolean success)
    {
        if(onLoadListener != null)
        {
            onLoadListener.onLoadComplete(success);
        }
    }

    public void onLoadStart()
    {
        if(onLoadListener != null)
        {
            onLoadListener.onLoadStart();
        }
    }

    public void onLoadCancel()
    {
        if(onLoadListener != null)
        {
            onLoadListener.onLoadCancel();
        }
    }



    public interface OnLoadListener{
        void onLoadComplete(boolean success);
        void onLoadStart();
        void onLoadCancel();
    }

    public interface OnDragListener{
        void onDrag(float process);
        void onDragRelease(float process);
        void onDragStart();
    }
}
