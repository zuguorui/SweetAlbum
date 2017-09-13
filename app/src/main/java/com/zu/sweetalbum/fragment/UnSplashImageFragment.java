package com.zu.sweetalbum.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;

/**
 * Created by zu on 17-9-8.
 */

public class UnSplashImageFragment extends Fragment {

    private RecyclerView recyclerView;
    private ZoomLayoutManager zoomLayoutManager;
    private ImageNoGroupedAdapter adapter;
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

    }
}
