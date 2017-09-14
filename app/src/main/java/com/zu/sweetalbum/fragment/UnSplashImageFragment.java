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
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by zu on 17-9-8.
 */

public class UnSplashImageFragment extends Fragment {

    private RecyclerView recyclerView;
    private ZoomLayoutManager zoomLayoutManager;

    private LinkedList<String> data = new LinkedList<>();
    MyAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        recyclerView = (RecyclerView)root.findViewById(R.id.UnSplashImageFragment_recyclerView);
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
            ImageViewHolder holder = new ImageViewHolder(checkableView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(position < 0 || position >= data.size())
            {
                return;
            }
            Glide.with(UnSplashImageFragment.this)
                    .load(new File(data.get(position)))
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
