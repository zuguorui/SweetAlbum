package com.zu.sweetalbum.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.LruCache;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.FileUtil;
import com.zu.sweetalbum.util.Function;
import com.zu.sweetalbum.util.ImageOperations;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.ImageCheckView;
import com.zu.sweetalbum.view.ImageSwitchView;
import com.zu.sweetalbum.view.TouchEventInterceptLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.zu.sweetalbum.activity.MainActivity.COPY_REQUEST_CODE;
import static com.zu.sweetalbum.activity.MainActivity.CUT_REQUEST_CODE;
import static com.zu.sweetalbum.activity.MainActivity.SHARE_REQUEST_CODE;
import static com.zu.sweetalbum.activity.SelectFolderActivity.ACTION_COPY;
import static com.zu.sweetalbum.activity.SelectFolderActivity.ACTION_CUT;
import static com.zu.sweetalbum.activity.SelectFolderActivity.SELECTED_IMAGES;

public class ImageCheckActivity extends AppCompatActivity implements View.OnClickListener{

    private MyLog log = new MyLog("ImageCheckActivity", true);
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            switch (event.action)
            {
                case Event.ACTION_ALBUM_LIST_PREPARE_SUCCESS:
                    final int currentPosition = switchView.getCurrentShowPosition();
                    if(event.content == null)
                    {
                        data.clear();
                    }else if(album.equals("date"))
                    {
                        data.clear();
                        HashMap<String, LinkedList<ImageModule>> d = (HashMap<String, LinkedList<ImageModule>>) event.content;
                        if(d != null)
                        {
                            LinkedList<String> sortedKey = sortKey(d.keySet());
                            for(String s : sortedKey)
                            {
                                data.addAll(d.get(s));
                            }
                        }
                    }else
                    {
                        data = (LinkedList<ImageModule>)event.content;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(data == null || data.size() == 0)
                            {
                                Toast.makeText(ImageCheckActivity.this, album + "中没有任何照片", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            imageSwitcherAdapter.notifyDataSetChanged();
                            if(imgName != null)
                            {
                                Integer resultPosition = null;
                                int i = 0;
                                Iterator<ImageModule> iterator = data.iterator();
                                while(iterator.hasNext())
                                {
                                    ImageModule module = iterator.next();

                                    if(imgName.equals(module.path))
                                    {
                                        resultPosition = i;
                                        break;
                                    }
                                    i++;
                                }
                                if(resultPosition != null)
                                {
                                    switchView.setPositionToShow(resultPosition);

                                }else
                                {
                                    switchView.setPositionToShow(currentPosition < 0 ? 0 : currentPosition);
                                }
                            }
                        }
                    });
                    break;
                case Event.ACTION_DATA_UPDATE:
                    RxBus.getInstance().post(new Event(Event.ACTION_GET_ALBUM_LIST, album));
                    break;
            }

        }
    };

    private Consumer errorConsumer = new Consumer<Throwable>(){
        @Override
        public void accept(@NonNull Throwable throwable) throws Exception {
            throwable.printStackTrace();
        }
    };

    private GestureDetector gestureDetector ;

    private FrameLayout customActionBarArea;
    private ImageSwitchView switchView;
    private ImageView shareButton;
    private ImageView favoriteButton;
    private ImageView menuButton;
    private TextView imgNameTextView;
    private ImageView backButton;
    private View actionBar;
    private PopupMenu popupMenu;
    private ViewGroup actionBarContainer;
    private View rootView;
    private TouchEventInterceptLayout interceptLayout;
    private Integer forePosition = null;


//    private LinkedList<String> optionResource = new LinkedList<>();
//    private ArrayAdapter<String> spinnerArrayAdapter;
    private ImageSwitcherAdapter imageSwitcherAdapter = new ImageSwitcherAdapter();
    private String album = "date";
    private String imgName = null;
    private LinkedList<ImageModule> data = new LinkedList<>();

    private long lastDownTime = 0;
    private boolean moved = false;
    private int touchSlop = 3;
    private int lastX = 0, lastY = 0, newX, newY;


    private ValueAnimator fullScreenAnimator = null;

    private static final int SET_WALLPAPER_REQUEST_CODE = 13;

    private PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            int currentPosition = switchView.getCurrentShowPosition();
            ArrayList<String> imagePaths = new ArrayList<>();
            Object o = imageSwitcherAdapter.getItem(currentPosition);
            if(o != null && o instanceof ImageModule)
            {
                imagePaths.add(((ImageModule) o).path);
            }
            if(imagePaths.size() == 0)
            {
                return true;
            }
            switch(id)
            {
                case R.id.PopupMenu_imageCopy:
                    Intent intent = new Intent(ImageCheckActivity.this, SelectFolderActivity.class);
                    intent.setAction(ACTION_COPY);
                    intent.putExtra(SELECTED_IMAGES, imagePaths);
                    startActivityForResult(intent, COPY_REQUEST_CODE);
                    break;
                case R.id.PopupMenu_imageCut:
                    Intent intent1 = new Intent(ImageCheckActivity.this, SelectFolderActivity.class);
                    intent1.setAction(ACTION_CUT);
                    intent1.putExtra(SELECTED_IMAGES, imagePaths);
                    startActivityForResult(intent1, CUT_REQUEST_CODE);
                    break;
                case R.id.PopupMenu_imageDelete:
                    ImageOperations.deleteImages(ImageCheckActivity.this, imagePaths, new Function<Void, Boolean>() {
                        @Override
                        public Void apply(Boolean aBoolean) {
                            if(aBoolean)
                            {
                                Toast.makeText(ImageCheckActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(ImageCheckActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    });
                    break;
                case R.id.PopupMenu_imageRename:
                    ImageOperations.renameImage(ImageCheckActivity.this, ((ImageModule) o).path, null);
                    break;
                case R.id.PopupMenu_imageInfo: {
                    ViewGroup content = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_image_info, null);
                    TextView dateTextView = (TextView) content.findViewById(R.id.DialogImageInfo_textView_date);
                    TextView pathTextView = (TextView) content.findViewById(R.id.DialogImageInfo_textView_path);
                    TextView sizeTextView = (TextView) content.findViewById(R.id.DialogImageInfo_textView_size);
                    Object o1 = imageSwitcherAdapter.getItem(switchView.getCurrentShowPosition());
                    if (o1 == null) {
                        return true;
                    }
                    ImageModule imageModule = (ImageModule) o1;

                    dateTextView.setText(CommonUtil.formatDate(imageModule.createDate));
                    pathTextView.setText(imageModule.path);
                    File file = new File(imageModule.path);
                    sizeTextView.setText(CommonUtil.getFormatedSize(file.length()));
                    AlertDialog.Builder builder = new AlertDialog.Builder(ImageCheckActivity.this);
                    builder.setTitle("详细信息");
                    builder.setView(content);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    break;
                }
                case R.id.PopupMenu_imageSetWallPaper:
                {
                    Object o1 = imageSwitcherAdapter.getItem(switchView.getCurrentShowPosition());
                    if(o1 == null)
                    {
                        return true;
                    }
                    final ImageModule imageModule = (ImageModule)o1;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ImageCheckActivity.this);
                    builder.setTitle("选项");
                    builder.setItems(new String[]{"桌面壁纸", "锁屏壁纸", "桌面和锁屏", "使用其他应用"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int mFlag = -10;
                                    switch (which)
                                    {
                                        case 0:
                                        case 1:
                                        case 2:
                                            mFlag = which;
                                            break;
                                        case 3:
                                            mFlag = -1;
                                            break;
                                        default:
                                            break;
                                    }
                                    if(mFlag == -10)
                                    {
                                        return;
                                    }
                                    if(mFlag == -1)
                                    {
                                        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        intent.putExtra("mimeType", "image/*");
                                        Uri uri = FileUtil.getImageUriByPath(ImageCheckActivity.this, imageModule.path);
                                        if(uri == null)
                                        {
                                            return;
                                        }
                                        intent.setData(uri);
                                        startActivityForResult(intent, SET_WALLPAPER_REQUEST_CODE);
                                    }else
                                    {
                                        final int flag = mFlag;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Looper.prepare();
                                                try{

                                                    final Bitmap bitmap = Glide.with(ImageCheckActivity.this)
                                                            .load(new File(imageModule.path))
                                                            .asBitmap()
                                                            .skipMemoryCache(true)
                                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                                            .get();

                                                    final WallpaperManager wallpaperManager = WallpaperManager.getInstance(ImageCheckActivity.this);

                                                    int result = 0;
                                                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N && flag != -1)
                                                    {
                                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ImageCheckActivity.this);
                                                        builder1.setTitle("注意");
                                                        builder1.setMessage(R.string.set_wall_paper_on_low_version);
                                                        builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which){
                                                                try{
                                                                    wallpaperManager.setBitmap(bitmap);
                                                                    Toast.makeText(ImageCheckActivity.this, "设置壁纸成功", Toast.LENGTH_SHORT).show();
                                                                }catch (Exception e)
                                                                {
                                                                    e.printStackTrace();
                                                                    Toast.makeText(ImageCheckActivity.this, "设置壁纸失败", Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                                        builder1.create().show();
                                                    }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && flag != -1)
                                                    {
                                                        switch(flag)
                                                        {
                                                            case 0:
                                                                result = wallpaperManager.setBitmap(bitmap, null, true,
                                                                        WallpaperManager.FLAG_SYSTEM);
                                                                break;
                                                            case 1:
                                                                result = wallpaperManager.setBitmap(bitmap, null, true,
                                                                        WallpaperManager.FLAG_LOCK);
                                                                break;
                                                            case 2:
                                                                result = wallpaperManager.setBitmap(bitmap, null, true,
                                                                        WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                                                                break;
                                                            default:
                                                                break;
                                                        }

                                                        if(result != 0)
                                                        {
                                                            Toast.makeText(ImageCheckActivity.this, "设置壁纸成功", Toast.LENGTH_SHORT).show();
                                                        }else
                                                        {
                                                            Toast.makeText(ImageCheckActivity.this, "设置壁纸失败", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                }catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                    Toast.makeText(ImageCheckActivity.this, "设置壁纸失败", Toast.LENGTH_SHORT).show();
                                                }
                                                Looper.loop();
                                            }
                                        }).start();

                                    }
                                }
                            });
                    builder.create().show();
                    break;

                }

                default:
                    break;

            }
            return true;
        }
    };

    private void animateFullScreen()
    {
        if(fullScreenAnimator == null)
        {
            fullScreenAnimator = ValueAnimator.ofInt(0, 100);
            fullScreenAnimator.setDuration(300);
            fullScreenAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final int process = (int)animation.getAnimatedValue();
                    log.d("process value = " + process);
                    int actionBarHeight = actionBarContainer.getHeight();
                    ViewGroup.MarginLayoutParams l = (ViewGroup.MarginLayoutParams) actionBarContainer.getLayoutParams();
                    l.topMargin = -(int)( (process * 1.0f / 100) * actionBarHeight) ;
                    actionBarContainer.setLayoutParams(l);

                    rootView.getBackground().setAlpha((int)((process * 1.0f / 100) * 255));


                }
            });
        }

        if(isFullScreen())
        {
            setFullScreen(false);
            fullScreenAnimator.reverse();
        }else
        {
            setFullScreen(true);
            fullScreenAnimator.start();
        }




    }

    private boolean isFullScreen()
    {

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if((layoutParams.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0)
        {
            return true;
        }else
        {
            return false;
        }
    }

    private void setFullScreen(boolean fullScreen)
    {
        if(fullScreen)
        {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(layoutParams);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }else
        {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(layoutParams);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private LinkedList<String> sortKey(Set<String> keys)
    {
        LinkedList<String> sortedKeys = new LinkedList<>();
        sortedKeys.clear();
        for(String key : keys)
        {
            int i = 0;
            Iterator<String> iterator = sortedKeys.iterator();
            while(iterator.hasNext())
            {
                String item = iterator.next();
                if(key.compareTo(item) > 0)
                {
                    break;
                }
                i++;
            }
            sortedKeys.add(i, key);
        }
        return sortedKeys;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_check);
        initViews();
        Intent info = getIntent();
        album = info.getStringExtra("album");
        imgName = info.getStringExtra("img_name");
        if(album == null)
        {
            album  = "date";
        }
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer,errorConsumer);
        mDisposables.add(disposable);
        RxBus.getInstance().post(new Event(Event.ACTION_GET_ALBUM_LIST, album));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposables.clear();
    }
    boolean menuShow = false;
    private void initViews()
    {
        rootView = findViewById(R.id.ImageCheckActivity_root);
        actionBarContainer = (ViewGroup) findViewById(R.id.ImageCheckActivity_ActionBar);
        ViewGroup.LayoutParams layoutParams = actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());


        switchView = (ImageSwitchView)findViewById(R.id.ImageCheckActivity_imageSwitchView);
        switchView.setAdapter(imageSwitcherAdapter);

        customActionBarArea = (FrameLayout) findViewById(R.id.MainActivity_actionBar_custom_area);
        backButton = (ImageView)findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        actionBar = getLayoutInflater().inflate(R.layout.single_image_action_bar, null);
        imgNameTextView = (TextView)actionBar.findViewById(R.id.SingleImageActionBar_textView_imageName);
        customActionBarArea.addView(actionBar);
        shareButton = (ImageView)actionBar.findViewById(R.id.SingleImageActionBar_imageButton_share);
        favoriteButton = (ImageView)actionBar.findViewById(R.id.SingleImageActionBar_imageButton_favorite);
        menuButton = (ImageView)actionBar.findViewById(R.id.SingleImageActionBar_imageButton_menu);
        interceptLayout = (TouchEventInterceptLayout)findViewById(R.id.ImageCheckActivity_touchEventInterceptLayout);

        backButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        favoriteButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        ColorDrawable backDrawable = new ColorDrawable(Color.BLACK);
        backDrawable.setAlpha(0);
        rootView.setBackground(backDrawable);

        backButton.setImageDrawable(getResources().getDrawable(R.drawable.back_000000_128));

        popupMenu = new PopupMenu(this, menuButton);
        popupMenu.setOnMenuItemClickListener(onMenuItemClickListener);
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                menuShow = false;
            }
        });
        popupMenu.inflate(R.menu.single_image_popup_menu);


        interceptLayout.setOnTouchEventReceiveListener(new TouchEventInterceptLayout.OnTouchEventReceiveListener() {
            @Override
            public void onTouchEventReceive(MotionEvent event) {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                        newX = (int)event.getX();
                        newY = (int)event.getY();
                        lastX = newX;
                        lastY = newY;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        lastX = newX;
                        lastY = newY;
                        newX = (int)event.getX();
                        newY = (int)event.getY();
                        if(Math.abs(newX - lastX) > touchSlop || Math.abs(newY - lastY) > touchSlop)
                        {
                            moved = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        long thisTime = System.currentTimeMillis();
                        if(thisTime - lastDownTime > 300 && !moved)
                        {
                            animateFullScreen();
                        }
                        lastDownTime = thisTime;
                        moved = false;
                        break;
                    default:
                        break;

                }
            }
        });

        switchView.setOnImageSwitchListener(new ImageSwitchView.OnImageSwitchListener() {
            @Override
            public void onImageSwitch(int oldPosition, int newPosition) {
                Object item = imageSwitcherAdapter.getItem(newPosition);
                if(item != null)
                {
                    ImageModule i = (ImageModule)item;
                    String path = i.path;
                    path = path.substring(path.lastIndexOf("/") + 1, path.length());
                    imgNameTextView.setText(path);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.MainActivity_ActionBar_slideIndicator:
                finish();
                break;
            case R.id.SingleImageActionBar_imageButton_share:
                int checkedPosition1 = switchView.getCurrentShowPosition();
                Object o1 = imageSwitcherAdapter.getItem(checkedPosition1);
                if(o1 != null && o1.getClass().equals(ImageModule.class))
                {
                    ArrayList<String> images = new ArrayList<>(1);
                    images.add(((ImageModule)o1).path);
                    ImageOperations.shareImages(this, images, SHARE_REQUEST_CODE);
                }
                break;
            case R.id.SingleImageActionBar_imageButton_favorite:

                break;
            case R.id.SingleImageActionBar_imageButton_menu:
                if(!menuShow)
                {
                    popupMenu.show();
                    menuShow = true;
                }else
                {
                    popupMenu.dismiss();
                }
                break;
            default:
                break;
        }
    }

    private class ImageSwitcherAdapter extends BaseAdapter
    {

        ExecutorService preloadExecutor = Executors.newSingleThreadExecutor();
        private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(3){
//            @Override
//            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
//                super.entryRemoved(evicted, key, oldValue, newValue);
//                if(oldValue != null && !oldValue.isRecycled())
//                {
//                    log.d("recycle bitmap");
////                    oldValue.recycle();
//                }
//            }


            @Override
            protected void onItemEvicted(String key, Bitmap item) {
                super.onItemEvicted(key, item);
                if(item != null && !item.isRecycled())
                {
                    item.recycle();
                }
            }
        };
        private int lastGetPosition = -1;
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
             if(position < 0 || position >= data.size())
             {
                 return null;
             }
             return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            log.d("convert view = " + (convertView == null ? "null" : "not null"));
            if(position < 0 || position >= getCount())
            {
                return null;
            }
            if(convertView == null)
            {
                ImageCheckView i = new ImageCheckView(ImageCheckActivity.this);
                convertView = i;
            }
            ImageCheckView imageCheckView = (ImageCheckView)convertView;
            ImageModule m = (ImageModule) getItem(position);
            if(m == null)
            {
                return null;
            }

//            Glide.with(ImageCheckActivity.this)
//                    .load(new File(m.path))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(imageCheckView);


            final Bitmap bitmap = lruCache.get(m.path);
            if(bitmap == null || bitmap.isRecycled())
            {
                log.d("bitmap == null");
                Glide.with(ImageCheckActivity.this)
                        .load(new File(m.path))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(imageCheckView);
            }else
            {
                log.d("bitmap != null");
                imageCheckView.setImageBitmap(bitmap);
            }


            if(lastGetPosition == -1)
            {
                lastGetPosition = position;
            }

            if(position - lastGetPosition < 0)
            {
                final int preloadPosition = position - 1;
                if(preloadPosition >= 0)
                {
                    final ImageModule i = (ImageModule) getItem(preloadPosition);
//                                Glide.with(ImageCheckActivity.this)
//                                        .load(new File(i.path))
//                                        .asBitmap()
//                                        .into(new SimpleTarget<Bitmap>() {
//                                            @Override
//                                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                                lruCache.put(i.path, resource);
//                                            }
//                                        });
                    preloadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(i != null && lruCache.get(i.path) == null && !i.path.toLowerCase().endsWith(".gif"))
                            {

                                try{
                                    Bitmap bitmap1 = Glide.with(ImageCheckActivity.this)
                                            .load(new File(i.path))
                                            .asBitmap()
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .get();
                                    lruCache.put(i.path, bitmap1);
                                }catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });



                }

            }else if(position - lastGetPosition > 0) {
                final int preloadPosition = position + 1;

                if (preloadPosition < getCount()) {

                    final ImageModule i = (ImageModule) getItem(preloadPosition);
//                    if (i != null && lruCache.get(i.path) == null && !i.path.toLowerCase().endsWith(".gif")) {
//                        Glide.with(ImageCheckActivity.this)
//                                .load(new File(i.path))
//                                .asBitmap()
//                                .into(new SimpleTarget<Bitmap>() {
//                                    @Override
//                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                        lruCache.put(i.path, resource);
//                                    }
//                                });
//                    }


                    preloadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(i != null && lruCache.get(i.path) == null && !i.path.toLowerCase().endsWith(".gif"))
                            {

                                try{
                                    Bitmap bitmap1 = Glide.with(ImageCheckActivity.this)
                                            .load(new File(i.path))
                                            .asBitmap()
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .get();
                                    lruCache.put(i.path, bitmap1);
                                }catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });



                }

            }
            lastGetPosition = position;

            return convertView;
        }
    }

}
