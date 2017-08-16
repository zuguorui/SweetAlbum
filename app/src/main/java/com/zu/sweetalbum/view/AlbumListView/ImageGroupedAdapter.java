package com.zu.sweetalbum.view.AlbumListView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.view.CheckableView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * Created by zu on 17-7-6.
 */

public abstract class ImageGroupedAdapter extends ImageAdapter {


    private MyLog log = new MyLog("ImageGroupedAdapter", true);
    protected HashMap<String, LinkedList<ImageModule>> mInnerData = new HashMap<>();
    protected ArrayList<String> sortedKeys = new ArrayList<>();
    protected Context context;
    protected int itemCount = 0;



    public ImageGroupedAdapter(@NonNull Context context, HashMap<String, LinkedList<ImageModule>> mInnerData) {

        this.context = context;
        if(mInnerData != null)
        {
            this.mInnerData = mInnerData;
            init();
        }
        setHasStableIds(true);
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                init();
            }
        });

    }

    public void setData(HashMap<String, LinkedList<ImageModule>> data)
    {
        mInnerData = data;
    }

    private void init()
    {
        itemCount = 0;
        LinkedList<String> temp = CommonUtil.sortKey(mInnerData.keySet());
        if(temp == null || temp.size() == 0)
        {
            return;
        }
        sortedKeys = new ArrayList<>(temp);
        for(String s : sortedKeys)
        {
            itemCount += mInnerData.get(s).size();
        }
        itemCount += sortedKeys.size();
    }

    @Override
    public Object getItem(int position) {
        if(position > getItemCount() || position < 0)
        {
            return null;
        }
        long id = getItemId(position);
        int groupIndex = getGroupIndex(id);
        int childIndex = getChildIndex(id);
        if(childIndex == 0)
        {
            return sortedKeys.get(groupIndex);
        }else
        {
            List<ImageModule> list = mInnerData.get(sortedKeys.get(groupIndex));
            if(list.size() >= childIndex)
            {
                return list.get(childIndex - 1);
            }else
            {
                return null;
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        long id = getItemId(position);
        int childIndex = getChildIndex(id);
        if(childIndex == 0)
        {
            return VIEW_TYPE_UNZOOM;
        }else
        {
            return VIEW_TYPE_ZOOM;
        }
    }

    @Override
    public long getItemId(int position) {
        if(position < 0 || position >= getItemCount())
        {
            return 0;
        }
        int mPosition = position;
        long group = 0;
        for(String key : sortedKeys)
        {
            if(mPosition >= mInnerData.get(key).size() + 1)
            {
                group += 1;
                mPosition -= mInnerData.get(key).size() + 1;
            }else
            {
                break;
            }
        }
        long id = (group << 32) | mPosition;
        return id;
    }

    @Override
    public int getGroupIndex(long id)
    {
        return (int)((id & 0xffffffff00000000l) >> 32);
    }

    @Override
    public int getChildIndex(long id)
    {

        return (int)(id & 0x00000000ffffffffl);
    }

    @Override
    public boolean isGrouped() {
        return true;
    }

    @Override
    public int getChildCount(int groupIndex) {
        String key = sortedKeys.get(groupIndex);
        return mInnerData.get(key).size();
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }


}
