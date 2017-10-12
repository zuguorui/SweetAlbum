package com.zu.sweetalbum.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.WorkType;
import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.DragLoadView;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;
import com.zu.sweetalbum.view.DownDragLoadView;
import com.zu.sweetalbum.view.UpDragLoadView;

import java.io.File;
import java.util.LinkedList;
import java.util.logging.LogRecord;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.zu.sweetalbum.service.UnSplashService.*;

/**
 * Created by zu on 17-9-8.
 */

public class UnSplashImageFragment extends Fragment {

    private RecyclerView recyclerView;
    private ZoomLayoutManager zoomLayoutManager;

    private LinkedList<PhotoBean> data = new LinkedList<>();
    private MyAdapter adapter;

    private int dataLength = 10;

    private int work_type = WorkType.BROWSE;

    private boolean hasMoreData = true;

    private String keyWord;

    private Handler uiHandler = new Handler();

    private UpDragLoadView upDragLoadView;
    private DownDragLoadView downDragLoadView;



    private boolean waitRefresh = false;
    private boolean waitLoadMore = false;

    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull Event event) throws Exception {
            switch (event.action)
            {
                case ACTION_UNSPLASH_GET_PHOTO_SUCCESS:
                {
                    LinkedList<PhotoBean> temp = (LinkedList<PhotoBean>) event.content;
                    data.addAll(temp);
                    adapter.notifyDataSetChanged();
                    if(waitLoadMore)
                    {
                        waitLoadMore = false;
                        downDragLoadView.onLoadComplete(true);
                    }
                    if(waitRefresh)
                    {
                        waitRefresh = false;
                        upDragLoadView.onLoadComplete(true);
                    }
                }
                    break;
                case ACTION_UNSPLASH_GET_PHOTO_FAIL:
                {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "get photo error", Toast.LENGTH_SHORT).show();
                            if(waitLoadMore)
                            {
                                waitLoadMore = false;
                                downDragLoadView.onLoadComplete(false);
                            }
                            if(waitRefresh)
                            {
                                waitRefresh = false;
                                upDragLoadView.onLoadComplete(false);
                            }
                        }
                    });
                }
                    break;
                case ACTION_UNSPLASH_GET_CURATED_PHOTO_FAIL:
                {

                }
                    break;
                case ACTION_UNSPLASH_GET_CURATED_PHOTO_SUCCESS:

                    break;
            }
        }
    };

    private DragLoadView.OnDragListener upDragListener = new DragLoadView.OnDragListener() {
        @Override
        public void onDrag(float process) {

        }

        @Override
        public void onDragRelease(float process) {
            if(process > 0.7f)
            {
                upDragLoadView.onLoadStart();
                data.clear();
                acquireData(dataLength, true);
            }else
            {
                upDragLoadView.onLoadCancel();
            }

        }

        @Override
        public void onDragStart() {

        }
    };

    private DragLoadView.OnDragListener downDragListener = new DragLoadView.OnDragListener() {
        @Override
        public void onDrag(float process) {

        }

        @Override
        public void onDragRelease(float process) {
            if(process > 0.7f)
            {
                downDragLoadView.onLoadStart();
                acquireData(dataLength, false);
            }else
            {
                downDragLoadView.onLoadCancel();
            }
        }

        @Override
        public void onDragStart() {

        }
    };

    private Consumer errorConsumer = new Consumer() {
        @Override
        public void accept(@io.reactivex.annotations.NonNull Object o) throws Exception {

        }
    };

    private CompositeDisposable mDisposable = new CompositeDisposable();

    public void setWorkType(int type)
    {
        if(work_type != type )
        {
            data.clear();
            if(adapter != null)
            {
                adapter.notifyDataSetChanged();
            }
        }
        work_type = type;
    }

    private void acquireData(int count, boolean refresh)
    {
        int from = 0, to = 0;
        if(data == null || data.size() == 0)
        {
            from = 0;
            to = from + count;
        }else{
            from = data.size();
            to = from + count;
        }

        acquireData(from, to, refresh);
    }

    private void acquireData(int from, int to, boolean refresh)
    {
        if(work_type == WorkType.BROWSE)
        {
            Event event = new Event(ACTION_UNSPLASH_GET_PHOTO, null);
            event.putExtra("from", from);
            event.putExtra("to", to);
            event.putExtra("refresh", refresh);
            RxBus.getInstance().post(event);
        }else if(work_type == WorkType.SEARCH)
        {
            Event event = new Event(ACTION_UNSPLASH_SEARCH_PHOTO, null);
            event.putExtra("from", from);
            event.putExtra("to", to);
            event.putExtra("keyword", keyWord);
            event.putExtra("refresh", refresh);
            RxBus.getInstance().post(event);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposable.add(disposable);
        setWorkType(WorkType.BROWSE);

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
        return root;
    }

    private void initViews(View root)
    {
        recyclerView = (RecyclerView)root.findViewById(R.id.UnSplashImageFragment_recyclerView);
        adapter = new MyAdapter(getActivity(), data);
        zoomLayoutManager = new ZoomLayoutManager(getActivity(), 2, 4, 2, adapter);
        recyclerView.setOnTouchListener(zoomLayoutManager.getZoomOnTouchListener());
        recyclerView.setLayoutManager(zoomLayoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = recyclerView.getMeasuredWidth();
                int height = recyclerView.getMeasuredHeight();
                int childHeight = zoomLayoutManager.getChildHeight();
                int childWidth = zoomLayoutManager.getChildWidth();
                int cols = (width / childWidth ) + 1;
                int rows = (height / childHeight) + 1;
                dataLength = 3 * cols * rows;
                acquireData(dataLength, false);
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });



        upDragLoadView = new UpDragLoadView(getContext());
        upDragLoadView.setOnDragListener(upDragListener);
        downDragLoadView = new DownDragLoadView(getContext());
        downDragLoadView.setOnDragListener(downDragListener);


    }

    private class MyAdapter extends ImageNoGroupedAdapter
    {
        public MyAdapter(@NonNull Context context, LinkedList<? extends Object> mInnerData) {
            super(context, mInnerData);
        }

        @Override
        public int getItemCount() {

            return super.getItemCount();
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
