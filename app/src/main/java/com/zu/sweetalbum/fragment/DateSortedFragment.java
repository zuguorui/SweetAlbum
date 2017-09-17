package com.zu.sweetalbum.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.util.LruCache;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.activity.ImageCheckActivity;
import com.zu.sweetalbum.activity.MainActivity;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.ImageGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.zu.sweetalbum.service.AlbumService.*;

/**
 * Created by zu on 17-6-13.
 */

public class DateSortedFragment extends Fragment {

    private String TAG = "DateSortedFragment";
    private MyLog log = new MyLog(TAG, true);
    private RecyclerView recyclerView;
    private HashMap<String, LinkedList<ImageModule>> data = new HashMap<>();
    private MyAdapter adapter;
    private ZoomLayoutManager zoomLayoutManager;
    private int[] checkedItems = null;
    private View noImageTextView;

    private ZoomLayoutManager.OnItemClickListener onItemClickListener = new ZoomLayoutManager.OnItemClickListener() {


        @Override
        public void onItemClicked(int position) {
            Object target = adapter.getItem(position);
            if(target == null)
            {
                return;
            }
            if(target.getClass().equals(String.class))
            {
                return;
            }
            if(target.getClass().equals(ImageModule.class))
            {
                Intent intent = new Intent(getContext(), ImageCheckActivity.class);
                intent.putExtra("album", "date");
                intent.putExtra("img_name", ((ImageModule)target).path);
                startActivity(intent);
            }
        }
    };

    private ZoomLayoutManager.OnItemLongClickListener onItemLongClickListener = new ZoomLayoutManager.OnItemLongClickListener() {
        @Override
        public void onItemLongClicked(int position) {
            zoomLayoutManager.setMultiCheckMode(true);
            zoomLayoutManager.addCheckedItems(new int[]{position});
        }
    };


    private ZoomLayoutManager.OnItemCheckedListener onMultiCheckListener = new ZoomLayoutManager.OnItemCheckedListener() {


        @Override
        public void onItemChecked(int[] checkedItemPosition) {
            checkedItems = checkedItemPosition;
            notifyImageSelected(checkedItemPosition.length);
            if(checkedItems.length >= adapter.getItemCount() - data.size())
            {
                ((MainActivity)getActivity()).notifyImageSelectAll(true);
            }else
            {
                ((MainActivity)getActivity()).notifyImageSelectAll(false);
            }
        }
    };


    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull final Event event) throws Exception {
            switch (event.action)
            {
                case ACTION_SORT_BY_DATE_SUCCESS:
                    synchronized (this)
                    {
                        if(event.content == null)
                        {
                            data.clear();
                        }else
                        {
                            data = (HashMap<String, LinkedList<ImageModule>>) event.content;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(data == null || data.size() == 0)
                                {
                                    noImageTextView.setVisibility(View.VISIBLE);
                                }else
                                {
                                    noImageTextView.setVisibility(View.GONE);
                                }
                                adapter.setData(data);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    break;
                case ACTION_DATA_UPDATE:
                    RxBus.getInstance().post(new Event(ACTION_GET_DATE_SORTED_LIST, null));
                    break;
                case ACTION_NO_IMAGE:
                case ACTION_NO_CAMERA_IMAGE:
                    data.clear();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setData(data);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };



    private Consumer errorConsumer = new Consumer<Throwable>(){
        @Override
        public void accept(@NonNull Throwable throwable) throws Exception {
            Log.e(TAG, throwable.getMessage());

        }
    };




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposables.add(disposable);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposables.clear();
    }

    private void notifyImageSelected(int selectedCount)
    {
        if(selectedCount == 0)
        {
            zoomLayoutManager.setMultiCheckMode(false);
        }
        ((MainActivity)getActivity()).notifyImageSelected(selectedCount);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_sorted, container, false);
        initViews(view);
        RxBus.getInstance().post(new Event(ACTION_GET_DATE_SORTED_LIST, null));
        return view;
    }

    private void initViews(View root)
    {
        noImageTextView = root.findViewById(R.id.DateSortedFragment_textView_noImage);
        noImageTextView.setVisibility(View.GONE);
        recyclerView = (RecyclerView)root.findViewById(R.id.DateSortedFragment_recyclerView);
        adapter = new MyAdapter(getContext(), data);
        zoomLayoutManager = new ZoomLayoutManager(getContext(), 4, 4, 1, adapter);
        recyclerView.setLayoutManager(zoomLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener(zoomLayoutManager.getZoomOnTouchListener());
        zoomLayoutManager.setOnItemCheckedListener(onMultiCheckListener);
        zoomLayoutManager.setOnItemLongClickListener(onItemLongClickListener);
        zoomLayoutManager.setOnItemClickListener(onItemClickListener);

    }

    public void cancelMultiCheckMode()
    {
        zoomLayoutManager.emptyCheckedItems();
        zoomLayoutManager.setMultiCheckMode(false);

    }

    public ArrayList<String> getCheckedItems()
    {
        if(checkedItems == null)
        {
            return null;
        }
        ArrayList<String> result = new ArrayList<>(checkedItems.length);
        for(int i = 0; i < checkedItems.length; i++)
        {
            int k = checkedItems[i];
            Object item = adapter.getItem(k);
            if(item.getClass().equals(ImageModule.class))
            {
                ImageModule imageModule = (ImageModule)item;
                result.add(imageModule.path);
            }
        }
        return result;
    }

    public void selectAll()
    {
        int[] checkedPositions = adapter.getAllCheckablePosition();
        zoomLayoutManager.setCheckedItems(checkedPositions);
    }

    public void selectNone()
    {
        zoomLayoutManager.emptyCheckedItems();
    }








    private class MyAdapter extends ImageGroupedAdapter
    {


        public MyAdapter(@android.support.annotation.NonNull Context context, HashMap<String, LinkedList<ImageModule>> data) {
            super(context, data);
        }



        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            log.d("onCreateViewHolder, viewType = " + viewType);
            RecyclerView.ViewHolder result = null;
            if(viewType == VIEW_TYPE_UNZOOM)
            {
                View view = LayoutInflater.from(context).inflate(R.layout.title_item, null);
                result = new TitleHolder(view);
            }else if(viewType == VIEW_TYPE_ZOOM)
            {
                CheckableView checkableView = new CheckableView(context);
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                checkableView.setContentView(imageView);

//            View view = LayoutInflater.from(context).inflate(R.layout.image_item, null);
                result = new ImageHolder(checkableView);
            }
            return result;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            log.d("onBindViewHolder, position = " + position);
//            if(position < 0 || position >= getItemCount())
//            {
//                return;
//            }
            long id = getItemId(position);
            int groupIndex = getGroupIndex(id);
            int childIndex = getChildIndex(id);
            if(childIndex == 0)
            {
                TitleHolder titleHolder = (TitleHolder)holder;
                String text = sortedKeys.get(groupIndex);
                titleHolder.textView.setText(text);
            }else
            {
                final ImageHolder imageHolder = (ImageHolder)holder;
                LinkedList<ImageModule> list = data.get(sortedKeys.get(groupIndex));
                ImageModule imageModule = list.get(childIndex - 1);


                imageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int mPosition = position;
                        layoutManager.onItemClicked(mPosition);
                    }
                });

                imageHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final int mPosition = position;
                        layoutManager.onItemLongClicked(mPosition);
                        return true;
                    }
                });

                Glide.clear(imageHolder.imageView);
                Glide.with(context)
                        .load(imageModule.path)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .override(200, 200)
//                    .thumbnail(0.1f)
                        .into(imageHolder.imageView);

                if(imageHolder.itemView instanceof CheckableView)
                {
                    if(imageModule.path.toLowerCase().endsWith(".gif"))
                    {
                        ((CheckableView) imageHolder.itemView).setIsGif(true);
                    }else
                    {
                        ((CheckableView) imageHolder.itemView).setIsGif(false);
                    }

                }

            }
//        checkAndPreload(position);
        }

        private class TitleHolder extends RecyclerView.ViewHolder
        {
            public TextView textView;
            public TitleHolder(View itemView) {
                super(itemView);
                textView = (TextView)itemView.findViewById(R.id.TitleItem_textView);
            }
        }

        public int[] getAllCheckablePosition()
        {
            int count = 0;
            for(String s : mInnerData.keySet())
            {
                count += mInnerData.get(s).size();
            }
            int[] checkedPositions = new int[count];
            int intPosition = 0;
            int k = 1;
            for(String s : sortedKeys)
            {
                LinkedList<ImageModule> temp = mInnerData.get(s);
                for(int i = 0; i < temp.size(); i++)
                {
                    checkedPositions[intPosition] = intPosition + k;
                    intPosition++;
                }
                k++;
            }
            return checkedPositions;
        }

        private class ImageHolder extends RecyclerView.ViewHolder
        {
            public ImageView imageView;


            public ImageHolder(CheckableView itemView) {
                super(itemView);
                imageView = (ImageView) itemView.getContentView();
            }
        }
    }
}
