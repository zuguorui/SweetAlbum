package com.zu.sweetalbum.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.ProgressDialogProxy;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.FileUtil;
import com.zu.sweetalbum.module.Function;
import com.zu.sweetalbum.util.ImageOperations;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SelectFolderActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String ACTION_COPY = "action_copy";
    public static final String ACTION_CUT = "action_cut";
    public static final String ACTION_DELETE = "action_delete";
    public static final String SELECTED_IMAGES = "selected_images";
    private ImageView indicator;
    private FrameLayout customArea;
    private ViewGroup actionBarContainer;
    private View textActionBar;
    private TextView titleTextView;
    private RecyclerView recyclerView;
    private ZoomLayoutManager zoomLayoutManager;
    private MyAdapter adapter;

    private ProgressDialogProxy progressDialogProxy = null;

    private String action = null;
    private ArrayList<String> selectedImages = null;

    private volatile LinkedList<String> sortedKeys = new LinkedList<>();

    private HashMap<String, LinkedList<ImageModule>> data = new HashMap<>();
    private MyLog log = new MyLog("SelectFolderActivity", true);
    private Handler mHandler = new Handler();
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            switch (event.action)
            {
                case Event.ACTION_SORT_BY_FOLDER_SUCCESS:
                    data = (HashMap<String, LinkedList<ImageModule>>) event.content;
                    initData();
                    adapter.setData(sortedKeys);
                    adapter.notifyDataSetChanged();
                    break;
                case Event.ACTION_COPY_IMAGE_SUCCESS:
                    setResult(RESULT_OK);
                    Toast.makeText(SelectFolderActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case Event.ACTION_COPY_IMAGE_FAIL:
                    setResult(3);
                    Toast.makeText(SelectFolderActivity.this, "复制失败", Toast.LENGTH_SHORT).show();
                    finish();
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



    private ZoomLayoutManager.OnItemClickListener onItemClickListener = new ZoomLayoutManager.OnItemClickListener() {
        @Override
        public void onItemClicked(int position) {
            if(action.equals(ACTION_COPY))
            {
                final String albumPath = (String)adapter.getItem(position);
                ImageOperations.copyImages(SelectFolderActivity.this, selectedImages, albumPath, new Function<Void, Boolean>() {
                    @Override
                    public Void apply(Boolean aBoolean) {
                        if(aBoolean)
                        {
                            Toast.makeText(SelectFolderActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }else
                        {
                            Toast.makeText(SelectFolderActivity.this, "复制失败", Toast.LENGTH_SHORT).show();
                            setResult(3);
                            finish();
                        }
                        return null;
                    }
                });
            }else if(action.equals(ACTION_CUT))
            {
                final String albumPath = (String)adapter.getItem(position);
                ImageOperations.cutImages(SelectFolderActivity.this, selectedImages, albumPath, new Function<Void, Boolean>() {
                    @Override
                    public Void apply(Boolean aBoolean) {
                        if(aBoolean)
                        {
                            Toast.makeText(SelectFolderActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }else
                        {
                            Toast.makeText(SelectFolderActivity.this, "剪切失败", Toast.LENGTH_SHORT).show();
                            setResult(3);
                            finish();
                        }
                        return null;
                    }
                });
            }
        }

    };




//    private void cutImages(final ArrayList<String> srcPaths, final String dest)
//    {
//        if(srcPaths != null && srcPaths.size() != 0)
//        {
//
//            final String albumName = dest.substring(dest.lastIndexOf("/") + 1, dest.length());
//            String message = "确认要将" + srcPaths.size() + "张照片移动到" + albumName + "中吗？";
//            AlertDialog.Builder builder = new AlertDialog.Builder(SelectFolderActivity.this);
//            builder.setTitle("移动");
//            builder.setMessage(message);
//            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//
//            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    progressDialogProxy = new ProgressDialogProxy(SelectFolderActivity.this, srcPaths.size(), "正在移动");
//                    progressDialogProxy.setMessage("准备移动");
//                    progressDialogProxy.setProgress(0);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            boolean result = FileUtil.cutImages(SelectFolderActivity.this, updateDialogHandler, srcPaths, dest);
//                            if(result)
//                            {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if(progressDialogProxy != null)
//                                        {
//                                            progressDialogProxy.dismiss();
//                                            progressDialogProxy = null;
//                                        }
//                                        setResult(RESULT_OK);
//                                        finish();
//                                    }
//                                });
//                            }else
//                            {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if(progressDialogProxy != null)
//                                        {
//                                            progressDialogProxy.dismiss();
//                                            progressDialogProxy = null;
//                                        }
//                                        setResult(3);
//                                        finish();
//                                    }
//                                });
//                            }
//                        }
//                    }).start();
//                }
//            });
//            builder.create().show();
//        }
//    }

    private Handler updateDialogHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case FileUtil.COPY_IMAGE:
                    if(progressDialogProxy != null)
                    {
                        if(msg.arg1 == -1)
                        {
                            progressDialogProxy.dismiss();
                            progressDialogProxy = null;
                        }else
                        {
                            String message = "正在复制第" + msg.arg1 + "张，共" + selectedImages.size() + "张";
                            progressDialogProxy.setMessage(message);
                            progressDialogProxy.setProgress(msg.arg1);
                        }
                    }
                    break;

            }
            return true;
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_folder);
        initViews();
        titleTextView.setText("选择目标文件夹");

        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposables.add(disposable);

        Intent intent = getIntent();
        action = intent.getAction();
        selectedImages = intent.getStringArrayListExtra(SELECTED_IMAGES);


        RxBus.getInstance().post(new Event(Event.ACTION_GET_FOLDER_SORTED_LIST, null));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposables.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.MainActivity_ActionBar_slideIndicator:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }
    }

    private void initViews()
    {
        actionBarContainer = (ViewGroup)findViewById(R.id.SelectFolderActivity_actionBar);
        ViewGroup.LayoutParams layoutParams = actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());

        indicator = (ImageView)findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        customArea = (FrameLayout)findViewById(R.id.MainActivity_actionBar_custom_area);
        textActionBar = getLayoutInflater().inflate(R.layout.text_action_bar, null);
        titleTextView = (TextView)textActionBar.findViewById(R.id.TextActionBar_textView_albumName);
        recyclerView = (RecyclerView) findViewById(R.id.SelectFolderActivity_recyclerView);
        adapter = new MyAdapter(this, sortedKeys);
        zoomLayoutManager = new ZoomLayoutManager(this, 2, 2, 2, adapter);
        zoomLayoutManager.setOnItemClickListener(onItemClickListener);
        recyclerView.setLayoutManager(zoomLayoutManager);
        recyclerView.setAdapter(adapter);
        customArea.addView(textActionBar);
        indicator.setImageResource(R.drawable.back_000000_128);
        indicator.setOnClickListener(this);

    }



    private void initData()
    {
        sortedKeys.clear();
        sortedKeys = CommonUtil.sortKey(data.keySet());
    }


    private class MyAdapter extends ImageNoGroupedAdapter
    {
        public MyAdapter(@android.support.annotation.NonNull Context context, LinkedList<? extends Object> data) {
            super(context, data);
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CheckableView checkableView = new CheckableView(context);
            View view = LayoutInflater.from(context).inflate(R.layout.item_album, null);
            checkableView.setContentView(view);
            AlbumHolder holder = new AlbumHolder(checkableView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if(position < 0 || position >= sortedKeys.size())
            {
                return;
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int mPosition = position;
                    layoutManager.onItemClicked(mPosition);
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
