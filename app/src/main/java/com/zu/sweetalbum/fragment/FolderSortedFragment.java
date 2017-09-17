package com.zu.sweetalbum.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.activity.AlbumBrowseActivity;
import com.zu.sweetalbum.activity.MainActivity;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.zu.sweetalbum.service.AlbumService.*;

/**
 * Created by zu on 17-6-16.
 */

public class FolderSortedFragment extends Fragment {

    private volatile HashMap<String, LinkedList<ImageModule>> data = new HashMap<>();
    private MyLog log = new MyLog("FolderSortFragment", true);
    private int[] checkedItems = new int[0];
    private volatile LinkedList<String> sortedKeys = new LinkedList<>();
    private ZoomLayoutManager zoomLayoutManager;
    private MyAdapter adapter;

    private RecyclerView recyclerView;

    private View noImageTextView;

    private ZoomLayoutManager.OnItemClickListener onItemClickListener = new ZoomLayoutManager.OnItemClickListener() {

        @Override
        public void onItemClicked(int position) {
            Object item = adapter.getItem(position);
            if(item != null && item.getClass().equals(String.class))
            {
                Intent intent = new Intent(getActivity(), AlbumBrowseActivity.class);
                intent.putExtra("folder_name", (String)item);
                startActivity(intent);
            }
        }
    };

    private ZoomLayoutManager.OnItemLongClickListener onItemLongClickListener = new ZoomLayoutManager.OnItemLongClickListener() {


        @Override
        public void onItemLongClicked(int position) {
            zoomLayoutManager.setMultiCheckMode(true);
            zoomLayoutManager.setCheckedItems(new int[]{position});
        }
    };

    private ZoomLayoutManager.OnItemCheckedListener onMultiCheckListener = new ZoomLayoutManager.OnItemCheckedListener() {

        @Override
        public void onItemChecked(int[] checkedItemPosition) {
            checkedItems = checkedItemPosition;
            notifyAlbumSelected(checkedItems.length);
        }
    };


    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull final Event event) throws Exception {
            switch (event.action)
            {
                case ACTION_SORT_BY_FOLDER_SUCCESS:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(event.content == null)
                            {
                                data.clear();
                            }else
                            {
                                data = (HashMap<String, LinkedList<ImageModule>>)event.content;
                            }
                            if(data == null || data.size() == 0)
                            {
                                noImageTextView.setVisibility(View.VISIBLE);
                            }else
                            {
                                noImageTextView.setVisibility(View.GONE);
                            }

                            log.d("data.size : " + data.size());
                            initData();
                            adapter.setData(sortedKeys);
                            adapter.notifyDataSetChanged();
                        }
                    });

                    break;
                case ACTION_DATA_UPDATE:
                    RxBus.getInstance().post(new Event(ACTION_GET_FOLDER_SORTED_LIST, null));
                    break;
                case ACTION_NO_IMAGE:
                    log.d("no image");
                    data.clear();
                    initData();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setData(sortedKeys);
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

        log.e(throwable.getMessage());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposables.add(disposable);
        log.d("onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_sorted, container, false);
        initViews(view);
        RxBus.getInstance().post(new Event(ACTION_GET_FOLDER_SORTED_LIST, null));
        log.d("onCreateView");
        return view;
    }

    @Override
    public void onDestroy() {
        log.d("onDestroy");
        mDisposables.clear();
        super.onDestroy();
    }

    private void initViews(View parent)
    {
        noImageTextView = parent.findViewById(R.id.FolderSortedFragment_textView_noImage);
        noImageTextView.setVisibility(View.GONE);
        recyclerView = (RecyclerView) parent.findViewById(R.id.FolderSortedFragment_recyclerView);
        adapter = new MyAdapter(getContext(), sortedKeys);
        zoomLayoutManager = new ZoomLayoutManager(getContext(), 2, 2, 2, adapter);

        recyclerView.setOnTouchListener(zoomLayoutManager.getZoomOnTouchListener());
        zoomLayoutManager.setOnItemCheckedListener(onMultiCheckListener);
        zoomLayoutManager.setOnItemLongClickListener(onItemLongClickListener);
        zoomLayoutManager.setOnItemClickListener(onItemClickListener);
        recyclerView.setLayoutManager(zoomLayoutManager);
        recyclerView.setAdapter(adapter);
        log.d("initViews");
    }

    private void notifyAlbumSelected(int selectedCount)
    {
        if(selectedCount == 0)
        {
            zoomLayoutManager.setMultiCheckMode(false);
        }
        ((MainActivity)getActivity()).notifyAlbumSelected(selectedCount);
    }

    public void cancelMultiCheckMode()
    {
        zoomLayoutManager.setMultiCheckMode(false);
        zoomLayoutManager.emptyCheckedItems();
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
            if(item.getClass().equals(String.class))
            {

                result.add((String)item);
            }
        }
        return result;
    }

    private void initData()
    {
        sortedKeys.clear();
        sortedKeys = CommonUtil.sortKey(data.keySet());
        log.d("initData, sortedKeys.size = " + sortedKeys.size());
    }

    private class MyAdapter extends ImageNoGroupedAdapter
    {
        public MyAdapter(@android.support.annotation.NonNull Context context, LinkedList<? extends Object> data) {
            super(context, data);
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            log.d("onCreateViewHolder");
            CheckableView checkableView = new CheckableView(context);
            View view = LayoutInflater.from(context).inflate(R.layout.item_album, null);
            checkableView.setContentView(view);
            AlbumHolder holder = new AlbumHolder(checkableView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            log.d("onBindViewHolder");
//            if(position < 0 || position >= sortedKeys.size())
//            {
//                return;
//            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int mPosition = position;
                    layoutManager.onItemClicked(mPosition);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int mPosition = position;
                    layoutManager.onItemLongClicked(mPosition);
                    return true;
                }
            });
            String key = sortedKeys.get(position);
            String name = key;
            if(name.charAt(name.length() - 1) == '/')
            {
                name = name.substring(0, name.length() - 1);
            }
            name = name.substring(name.lastIndexOf("/") + 1, name.length());
            AlbumHolder albumHolder = (AlbumHolder)holder;
            albumHolder.albumName.setText(name);
            albumHolder.imgCount.setText(data.get(key).size() + "张照片");
            String path = data.get(key).getFirst().path;

            Glide.with(context)
                    .load(new File(path))
                    .asBitmap()
                    .override(500, 500)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(albumHolder.itemImage);

        }

        private class AlbumHolder extends RecyclerView.ViewHolder
        {
            public ImageView itemImage;
            public TextView albumName;
            public TextView imgCount;
            public AlbumHolder(View itemView) {
                super(itemView);
                View contentView = ((CheckableView)itemView).getContentView();
                itemImage = (ImageView)contentView.findViewById(R.id.AlbumItem_imageView);
                albumName = (TextView)contentView.findViewById(R.id.AlbumItem_textView_albumName);
                imgCount = (TextView)contentView.findViewById(R.id.AlbumItem_textView_albumCondition);

            }
        }
    }


}
