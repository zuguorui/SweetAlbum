package com.zu.sweetalbum.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;

import java.util.LinkedList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by zu on 17-9-22.
 */

public class UnSplashCollectionFragment extends Fragment{
    private RecyclerView recyclerView;
    private ZoomLayoutManager zoomLayoutManager;

    private LinkedList<PhotoBean> data = new LinkedList<>();
    MyAdapter adapter;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    private Consumer messageConsumer = new Consumer<Event>(){
        @Override
        public void accept(@io.reactivex.annotations.NonNull Event event) throws Exception {

        }
    };

    private Consumer errorConsumer = new Consumer() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull Object o) throws Exception {

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        mDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_unsplash_image, null);
        initViews(root);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initViews(View root)
    {
        recyclerView = (RecyclerView)root.findViewById(R.id.UnSplashCollectionFragment_recyclerView);
        adapter = new MyAdapter(getActivity(), data);
        zoomLayoutManager = new ZoomLayoutManager(getActivity(), 2, 4, 2, adapter);
        recyclerView.setOnTouchListener(zoomLayoutManager.getZoomOnTouchListener());
        recyclerView.setLayoutManager(zoomLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private class MyAdapter extends ImageNoGroupedAdapter
    {
        public MyAdapter(@NonNull Context context, LinkedList<? extends Object> mInnerData) {
            super(context, mInnerData);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            CheckableView checkableView = new CheckableView(getContext());
            checkableView.setContentView(imageView);
            ImageViewHolder holder = new MyAdapter.ImageViewHolder(checkableView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(position < 0 || position >= data.size())
            {
                return;
            }

            Glide.with(UnSplashCollectionFragment.this)
                    .load(data.get(position).urls.small)
                    .into(((ImageViewHolder)holder).imageView);
        }

        private class ImageViewHolder extends RecyclerView.ViewHolder
        {
            public ImageView imageView;
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView)(((CheckableView)itemView).getContentView());
            }

        }
    }
}
