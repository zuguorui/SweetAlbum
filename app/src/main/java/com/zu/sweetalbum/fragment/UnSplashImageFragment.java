package com.zu.sweetalbum.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.WorkType;
import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.service.UnSplashService;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;
import com.zu.sweetalbum.view.DownDragLoadView;
import com.zu.sweetalbum.view.DragLoadView;
import com.zu.sweetalbum.view.DragToLoadLayout;
import com.zu.sweetalbum.view.UpDragLoadView;

import java.util.LinkedList;

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

    private DragToLoadLayout dragToLoadLayout;

    private LinkedList<PhotoBean> data = new LinkedList<>();
    private MyAdapter adapter;

    private int dataLength = 10;

    private int work_type = WorkType.BROWSE;

    private boolean hasMoreData = true;

    private String keyWord;

    private float startLoadGate = 0.7f;

    private Handler uiHandler = new Handler();

    private UpDragLoadView upDragLoadView;
    private DownDragLoadView downDragLoadView;
    private DragLoadView.OnDragListener upDragListener = new DragLoadView.OnDragListener(){

        @Override
        public void onDrag(float process) {

        }

        @Override
        public void onDragRelease(float process) {
            if(process > startLoadGate && !waitRefresh)
            {
                waitRefresh = true;
                upDragLoadView.loadStart();
                acquireData(0, dataLength, true);
                return;
            }
            if(process <= startLoadGate)
            {
                upDragLoadView.loadCancel();
            }
        }

        @Override
        public void onDragStart() {

        }
    };

    private DragLoadView.OnDragListener downDragListener = new DragLoadView.OnDragListener(){
        @Override
        public void onDrag(float process) {

        }

        @Override
        public void onDragRelease(float process) {
            if(process > startLoadGate && !waitRefresh && !waitLoadMore)
            {
                waitLoadMore = true;
                downDragLoadView.loadStart();
                acquireData(dataLength, false);
                return;
            }
            if(process <= startLoadGate)
            {
                downDragLoadView.loadCancel();
            }
        }

        @Override
        public void onDragStart() {

        }
    };



    private boolean waitRefresh = false;
    private boolean waitLoadMore = false;

    private Runnable waitRunnable = null;

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
                        downDragLoadView.loadComplete(true);
                    }
                    if(waitRefresh)
                    {
                        waitRefresh = false;
                        upDragLoadView.loadComplete(true);
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
                                downDragLoadView.loadComplete(false);
                            }
                            if(waitLoadMore)
                            {
                                waitLoadMore = false;
                                upDragLoadView.loadComplete(false);
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

                case ACTION_UNSPLASH_SERVICE_STARTED:
                    if(waitRunnable != null)
                    {
                        new Thread(waitRunnable).start();
                    }
                    break;
            }
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

        dragToLoadLayout = (DragToLoadLayout)root.findViewById(R.id.UnSplashImageFragment_DragToLoadLayout);

        upDragLoadView = (UpDragLoadView)root.findViewById(R.id.UnSplashImageFragment_UpDragLoadView);
        upDragLoadView.setOnDragListener(upDragListener);
        downDragLoadView = (DownDragLoadView)root.findViewById(R.id.UnSplashImageFragment_DownDragLoadView);
        downDragLoadView.setOnDragListener(downDragListener);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = recyclerView.getMeasuredWidth();
                int height = recyclerView.getMeasuredHeight();
                float zoomLevel = zoomLayoutManager.getZoomLevel();
//                int childHeight = zoomLayoutManager.getChildHeight();
//                int childWidth = zoomLayoutManager.getChildWidth();
//                int cols = width / childWidth + 1;
//                int rows = height / childHeight + 1;
                int cols = (int)zoomLevel + 1;
                int rows = (int)((height * 1.0F / width) * zoomLevel + 1);

                dataLength = 3 * cols * rows;
                if(!UnSplashService.isServiceRunning())
                {
                    waitRunnable = new Runnable() {
                        @Override
                        public void run() {
                            acquireData(dataLength, false);
                        }
                    };
                }else
                {
                    acquireData(dataLength, false);
                }
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        recyclerView.canScrollVertically(1);







//
//        zoomLayoutManager.setUpDragLoadView(upDragLoadView);
//        zoomLayoutManager.setDownDragLoadView(downDragLoadView);


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
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
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
