package com.zu.sweetalbum.view.AlbumListView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.MyLog;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zu on 17-7-17.
 */

public abstract class ImageNoGroupedAdapter extends ImageAdapter{
    private MyLog log = new MyLog("ImageNoGroupedAdapter", true);
    protected LinkedList<? extends Object> mInnerData = new LinkedList<>();
    protected Context context;

    public ImageNoGroupedAdapter(@NonNull Context context, LinkedList<? extends Object> mInnerData) {
        this.context = context;
        if(mInnerData != null)
        {
            this.mInnerData = mInnerData;
        }
        setHasStableIds(true);

    }

    public void setData(LinkedList<? extends Object> data)
    {
        mInnerData = data;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ZOOM;
    }

    @Override
    public boolean isGrouped() {
        return false;
    }

    @Override
    public int getGroupIndex(long id) {
        return (int)((id & 0xffffffff00000000l) >> 32);
    }

    @Override
    public int getChildIndex(long id) {
        return (int)(id & 0x00000000ffffffffl);
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getChildCount(int groupIndex) {
        return mInnerData.size();
    }

    @Override
    public Object getItem(int position) {
        if(position < 0 || position >= getItemCount())
        {
            return null;
        }
        return mInnerData.get(position);
    }



    @Override
    public int getItemCount() {
        return mInnerData.size();
    }


}
