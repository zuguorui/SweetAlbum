package com.zu.sweetalbum.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.LruCache;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.Comparators;
import com.zu.sweetalbum.util.Function;
import com.zu.sweetalbum.util.ImageOperations;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.sweetalbum.view.AlbumListView.ZoomLayoutManager;
import com.zu.sweetalbum.view.CheckableView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.zu.sweetalbum.activity.MainActivity.COPY_REQUEST_CODE;
import static com.zu.sweetalbum.activity.MainActivity.CUT_REQUEST_CODE;
import static com.zu.sweetalbum.activity.MainActivity.DELETE_REQUEST_CODE;
import static com.zu.sweetalbum.activity.MainActivity.SHARE_REQUEST_CODE;
import static com.zu.sweetalbum.activity.SelectFolderActivity.ACTION_COPY;
import static com.zu.sweetalbum.activity.SelectFolderActivity.ACTION_CUT;
import static com.zu.sweetalbum.activity.SelectFolderActivity.SELECTED_IMAGES;

public class AlbumBrowseActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AlbumBrowseActivity";
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            switch (event.action)
            {
                case Event.ACTION_ALBUM_LIST_PREPARE_SUCCESS:
                    log.d("Event.ACTION_ALBUM_LIST_PREPARE_SUCCESS");
                    if(event.content == null)
                    {
                        data.clear();
                    }else
                    {
                        data = (LinkedList<ImageModule>)event.content;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(data == null || data.size() == 0)
                            {
                                Toast.makeText(AlbumBrowseActivity.this, folderName + "中没有任何照片", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            adapter.setData(data);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    break;
                case Event.ACTION_DATA_UPDATE:
                    log.d("Event.ACTION_DATA_UPDATE");
                    RxBus.getInstance().post(new Event(Event.ACTION_GET_ALBUM_LIST, folderName));
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

    private View titleActionBar;
    private TextView titleTextView;
    private FrameLayout customArea;
    private View selectedActionBar;
    private ImageView imageCopyButton;
    private ImageView imageCutButton;
    private ImageView imageSelectAllButton;
    private ImageView imageFavoriteButton;
    private ImageView imageShareButton;
    private ImageView imageDeleteButton;
    private ViewGroup actionBarContainer;
    private ImageView indicator;

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private ZoomLayoutManager zoomLayoutManager;

    private MyLog log = new MyLog("AlbumBrowseActivity", true);
    private LinkedList<ImageModule> data = new LinkedList<>();

    private LayerDrawable slideIndicatorDrawable = null;
    private String folderName = null;
    private int[] checkedItems;
    private boolean imageSelectAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_browse);
        initViews();
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposables.add(disposable);
        Intent intent = getIntent();
        if(intent != null)
        {
            folderName = intent.getStringExtra("folder_name");
            if(folderName != null)
            {
                RxBus.getInstance().post(new Event(Event.ACTION_GET_ALBUM_LIST, folderName));
                String name = folderName;
                if(name.charAt(name.length() - 1) == '/')
                {
                    name = name.substring(0, name.length() - 1);
                }
                name = name.substring(name.lastIndexOf("/") + 1, name.length());
                titleTextView.setText(name);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposables.clear();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                if(customArea.getChildAt(0) != titleActionBar)
                {
                    zoomLayoutManager.emptyCheckedItems();
                    zoomLayoutManager.setMultiCheckMode(false);
                    result = true;
                }else
                {
                    result = super.onKeyDown(keyCode, event);
                }
                break;
            default:
                result = super.onKeyDown(keyCode, event);



        }
        return result;
    }

    private void initViews()
    {
        actionBarContainer = (ViewGroup) findViewById(R.id.AlbumBrowseActivity_actionBar);
        ViewGroup.LayoutParams layoutParams = actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());

        indicator = (ImageView)findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        customArea = (FrameLayout)findViewById(R.id.MainActivity_actionBar_custom_area);
        recyclerView = (RecyclerView) findViewById(R.id.AlbumBrowseActivity_recyclerView);
        adapter = new MyAdapter(this, data);
        zoomLayoutManager = new ZoomLayoutManager(this, 4, 4, 2, adapter);
        recyclerView.setOnTouchListener(zoomLayoutManager.getZoomOnTouchListener());
        recyclerView.setLayoutManager(zoomLayoutManager);
        initTitleActionBar();
        initSelectedActionBar();
        customArea.addView(titleActionBar);

        BitmapDrawable drawable1 = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.back_000000_128));
        BitmapDrawable drawable2 = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.close_000000_128));
        drawable2.setAlpha(0);
        Drawable[] drawables = new Drawable[]{drawable1, drawable2};
        slideIndicatorDrawable = new LayerDrawable(drawables);
        indicator.setImageDrawable(slideIndicatorDrawable);
        indicator.setOnClickListener(this);
        zoomLayoutManager.setOnItemLongClickListener(new ZoomLayoutManager.OnItemLongClickListener() {

            @Override
            public void onItemLongClicked(int position) {
                zoomLayoutManager.setMultiCheckMode(true);
                zoomLayoutManager.setCheckedItems(new int[]{position});
                showActionBar(selectedActionBar);
            }
        });

        zoomLayoutManager.setOnItemClickListener(new ZoomLayoutManager.OnItemClickListener() {

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
                    Intent intent = new Intent(AlbumBrowseActivity.this, ImageCheckActivity.class);
                    intent.putExtra("album", folderName);
                    intent.putExtra("img_name", ((ImageModule)target).path);
                    startActivity(intent);
                }
            }
        });

        zoomLayoutManager.setOnItemCheckedListener(new ZoomLayoutManager.OnItemCheckedListener() {
            @Override
            public void onItemChecked(int[] checkedItemPosition) {
                checkedItems = checkedItemPosition;
                if(checkedItemPosition.length == 0)
                {
                    zoomLayoutManager.setMultiCheckMode(false);
                    showActionBar(titleActionBar);
                }

                if(checkedItemPosition.length == adapter.getItemCount())
                {
                    notifyImageSelectAll(true);
                }else
                {
                    notifyImageSelectAll(false);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        zoomLayoutManager.emptyCheckedItems();
        zoomLayoutManager.setMultiCheckMode(false);

        switch (requestCode)
        {
            case COPY_REQUEST_CODE:

                break;
            case CUT_REQUEST_CODE:

                break;
            case DELETE_REQUEST_CODE:

                break;
            case SHARE_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {
                    Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show();
                }else if (resultCode == RESULT_CANCELED)
                {
                    Toast.makeText(this, "分享取消", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void notifyImageSelectAll(boolean selectAll)
    {
        if(selectAll)
        {
            imageSelectAll = true;
            imageSelectAllButton.setImageDrawable(getResources().getDrawable(R.drawable.select_all_active_0000ff, null));
        }else
        {
            imageSelectAll = false;
            imageSelectAllButton.setImageDrawable(getResources().getDrawable(R.drawable.select_all_000000_128, null));
        }
    }

    private void initTitleActionBar()
    {
        titleActionBar = getLayoutInflater().inflate(R.layout.text_action_bar, null);
        titleTextView = (TextView)titleActionBar.findViewById(R.id.TextActionBar_textView_albumName);
    }

    private void initSelectedActionBar()
    {
        selectedActionBar = getLayoutInflater().inflate(R.layout.image_selected_action_bar, null);
        imageCopyButton = (ImageView)selectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_copy);
        imageCutButton = (ImageView)selectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_cut);
        imageSelectAllButton = (ImageView)selectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_selectAll);
        imageFavoriteButton = (ImageView)selectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_favorite);
        imageShareButton = (ImageView)selectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_share);
        imageDeleteButton = (ImageView)selectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_delete);

        imageCopyButton.setOnClickListener(this);
        imageCutButton.setOnClickListener(this);
        imageSelectAllButton.setOnClickListener(this);
        imageFavoriteButton.setOnClickListener(this);
        imageShareButton.setOnClickListener(this);
        imageDeleteButton.setOnClickListener(this);
    }

    private ArrayList<String> getSelectedItems()
    {
        ArrayList<String> result = new ArrayList<>(checkedItems.length);
        for(int i : checkedItems)
        {
            Object o = adapter.getItem(i);
            if(o.getClass().equals(ImageModule.class))
            {
                ImageModule imageModule = (ImageModule)o;
                result.add(imageModule.path);
            }
        }
        return result;
    }

    private void showActionBar(final View actionBar)
    {
        final View removedView = customArea.getChildAt(0);
        customArea.addView(actionBar);
        ViewGroup.MarginLayoutParams l = (ViewGroup.MarginLayoutParams) actionBar.getLayoutParams();
        l.topMargin = actionBar.getMeasuredHeight();
        actionBar.setLayoutParams(l);

        final boolean reverseIndicator = (actionBar == titleActionBar);
        final int i = customArea.getMeasuredHeight() - customArea.getPaddingTop() - customArea.getPaddingBottom();
        ValueAnimator animator = ValueAnimator.ofInt(0, i);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int curr = (int)animation.getAnimatedValue();
                if(removedView != null)
                {
                    setActionBarTopMargin(removedView, -curr);

                }
                setActionBarTopMargin(actionBar, i - curr);
                if(!reverseIndicator)
                {
                    changeSlideIndicatorState((curr * 1.0f) / i);
                }else
                {
                    changeSlideIndicatorState(1 - ((curr * 1.0f) / i));
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(removedView != null)
                {
                    customArea.removeView(removedView);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(200);
        animator.start();
    }

    private void changeSlideIndicatorState(float process)
    {
        if(slideIndicatorDrawable == null)
        {
            return;
        }
        indicator.setRotation(process * 180);
        slideIndicatorDrawable.getDrawable(0).setAlpha((int)((1-process) * 255));
        slideIndicatorDrawable.getDrawable(1).setAlpha((int)((process) * 255));
    }

    private void setActionBarTopMargin(View view, int topMargin)
    {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.topMargin = topMargin;
        params.bottomMargin = -topMargin;
        view.setLayoutParams(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.MainActivity_ActionBar_slideIndicator:
                if(zoomLayoutManager.isInMultiCheckMode())
                {
                    zoomLayoutManager.emptyCheckedItems();
                    zoomLayoutManager.setMultiCheckMode(false);
                }else
                {
                    this.finish();
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_copy:

                ArrayList<String> checkedItems1 = getSelectedItems();
                if(checkedItems1 != null && checkedItems1.size() != 0)
                {
                    Intent intent = new Intent(this, SelectFolderActivity.class);
                    intent.setAction(ACTION_COPY);
                    intent.putExtra(SELECTED_IMAGES, checkedItems1);
                    startActivityForResult(intent, COPY_REQUEST_CODE);
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_cut:
                ArrayList<String> checkedItems2 = getSelectedItems();
                if(checkedItems2 != null && checkedItems2.size() != 0)
                {
                    Intent intent = new Intent(this, SelectFolderActivity.class);
                    intent.setAction(ACTION_CUT);
                    intent.putExtra(SELECTED_IMAGES, checkedItems2);
                    startActivityForResult(intent, COPY_REQUEST_CODE);
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_selectAll:
                if(imageSelectAll)
                {
                    zoomLayoutManager.emptyCheckedItems();
                    zoomLayoutManager.setMultiCheckMode(false);
                }else
                {
                    selectAll();
                }

                break;
            case R.id.ImageSelectionActionBar_imageButton_share:
                ArrayList<String> checkedItems3 = getSelectedItems();
                ImageOperations.shareImages(this, checkedItems3, SHARE_REQUEST_CODE);
                break;
            case R.id.ImageSelectionActionBar_imageButton_favorite:

                break;
            case R.id.ImageSelectionActionBar_imageButton_delete:
                ArrayList<String> checkItems4 = getSelectedItems();
                if(checkItems4 != null && checkItems4.size() != 0)
                {
                    ImageOperations.deleteImages(AlbumBrowseActivity.this, checkItems4, new Function<Void, Boolean>() {
                        @Override
                        public Void apply(Boolean aBoolean) {
                            if(aBoolean)
                            {
                                Toast.makeText(AlbumBrowseActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(AlbumBrowseActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                            zoomLayoutManager.emptyCheckedItems();
                            zoomLayoutManager.setMultiCheckMode(false);
                            return null;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void selectAll()
    {
        int[] allPosition = adapter.getAllCheckablePosition();
        zoomLayoutManager.setCheckedItems(allPosition);
    }



    private class MyAdapter extends ImageNoGroupedAdapter
    {
        public MyAdapter(@android.support.annotation.NonNull Context context, LinkedList<? extends Object> mInnerData) {
            super(context, mInnerData);
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CheckableView checkableView = new CheckableView(context);
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            checkableView.setContentView(imageView);

//            View view = LayoutInflater.from(context).inflate(R.layout.image_item, null);
            ImageHolder result = new ImageHolder(checkableView);
            return result;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ImageHolder imageHolder = (ImageHolder)holder;


            ImageModule imageModule = data.get(position);


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

        public int[] getAllCheckablePosition()
        {

            int[] checkedPositions = new int[getItemCount()];
            for(int i = 0; i < getItemCount(); i++)
            {
                checkedPositions[i] = i;
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
