package com.zu.sweetalbum.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zu.sweetalbum.R;
import com.zu.sweetalbum.fragment.DateSortedFragment;
import com.zu.sweetalbum.fragment.FolderSortedFragment;
import com.zu.sweetalbum.service.AlbumService;
import com.zu.sweetalbum.swiftp.FtpActivity;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.module.Function;
import com.zu.sweetalbum.util.ImageOperations;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;
import com.zu.sweetalbum.view.MyViewPager;
import com.zu.sweetalbum.view.SlideLayout;
import com.zu.sweetalbum.view.TextViewPagerIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.zu.sweetalbum.activity.SelectFolderActivity.ACTION_COPY;
import static com.zu.sweetalbum.activity.SelectFolderActivity.ACTION_CUT;
import static com.zu.sweetalbum.activity.SelectFolderActivity.SELECTED_IMAGES;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 10;

    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {

        }
    };

    private Consumer errorConsumer = new Consumer<Throwable>(){
        @Override
        public void accept(@NonNull Throwable throwable) throws Exception {
            throwable.printStackTrace();
        }
    };

    MyLog log = new MyLog("MainActivity", true);

    private MyViewPager viewPager;
    private ImageView slideIndicator;
    private TextViewPagerIndicator viewPagerIndicator;
    private DateSortedFragment dateSortedFragment;
    private FolderSortedFragment folderSortedFragment;
    private SlideLayout slideLayout;

    private MyFragmentAdapter fragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager());

    private ArrayList<Fragment> fragments = new ArrayList<>(2);

    private LayerDrawable slideIndicatorDrawable = null;

    private FrameLayout customActionBarArea;
    private ViewGroup actionBarContainer;

    private View normalActionBar;
    private View imageSelectedActionBar;
    private View albumSelectedActionBar;

    private ImageView imageCopyButton;
    private ImageView imageCutButton;
    private ImageView imageSelectAllButton;
    private ImageView imageFavoriteButton;
    private ImageView imageShareButton;
    private ImageView imageDeleteButton;

    private ImageView albumToFolderButton;
    private ImageView albumDeleteButton;

    private View ftpTransButton;
    private View aboutButton;

    private View slideMenu;

    private ImageView dailyWallpaper;

    private boolean allowViewPagerScroll = true;
    public static final int COPY_REQUEST_CODE = 1;
    public static final int CUT_REQUEST_CODE = 2;
    public static final int DELETE_REQUEST_CODE = 3;
    public static final int SHARE_REQUEST_CODE = 4;

    private int currentActionBar = 0;


    private static final int NORMAL_ACTION_BAR_CODE = 0;
    private static final int SELECT_IMAGE_ACTION_BAR_CODE = 1;
    private static final int SELECT_FOLDER_ACTOPM_BAR_CODE = 2;


    private boolean imageSelectAll = false;
    private ArrayList<String> selectedImages = new ArrayList<>();
    private String dailyWallpaperPath;


    private void initNormalActionBar()
    {
        normalActionBar = getLayoutInflater().inflate(R.layout.normal_action_bar, null);
        viewPagerIndicator = (TextViewPagerIndicator)normalActionBar.findViewById(R.id.NormalActionBar_viewPagerIndicator);
        viewPagerIndicator.addTags(new String[]{ "照片", "相册"});
        viewPagerIndicator.addOnTagClickedListener(new TextViewPagerIndicator.OnTagClickedListener() {
            @Override
            public void onTagClicked(int position) {
                viewPager.setCurrentItem(position, true);
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                viewPagerIndicator.listen(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        viewPagerIndicator.addOnTagClickedListener(new TextViewPagerIndicator.OnTagClickedListener() {
            @Override
            public void onTagClicked(int position) {
                viewPager.setCurrentItem(position , true);
                if(slideLayout.getSlideState() == 0.0f)
                {
                    slideLayout.scrollToLeft();
                }
            }
        });
    }

    private void initImageSelectedActionBar()
    {
        imageSelectedActionBar = getLayoutInflater().inflate(R.layout.image_selected_action_bar, null);
        imageCopyButton = (ImageView)imageSelectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_copy);
        imageCutButton = (ImageView)imageSelectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_cut);
        imageSelectAllButton = (ImageView)imageSelectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_selectAll);
        imageFavoriteButton = (ImageView)imageSelectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_favorite);
        imageShareButton = (ImageView)imageSelectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_share);
        imageDeleteButton = (ImageView)imageSelectedActionBar.findViewById(R.id.ImageSelectionActionBar_imageButton_delete);

        imageCopyButton.setOnClickListener(this);
        imageCutButton.setOnClickListener(this);
        imageSelectAllButton.setOnClickListener(this);
        imageFavoriteButton.setOnClickListener(this);
        imageShareButton.setOnClickListener(this);
        imageDeleteButton.setOnClickListener(this);
    }

    private void initAlbumSelectionActionBar()
    {
        albumSelectedActionBar = getLayoutInflater().inflate(R.layout.album_selected_action_bar, null);
        albumDeleteButton = (ImageView)albumSelectedActionBar.findViewById(R.id.AlbumSelectionActionBar_imageButton_delete);
        albumToFolderButton = (ImageView)albumSelectedActionBar.findViewById(R.id.AlbumSelectionActionBar_imageButton_toFolder);

        albumDeleteButton.setOnClickListener(this);
        albumToFolderButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.MainActivity_ActionBar_slideIndicator:
                if(customActionBarArea.getChildAt(0) == imageSelectedActionBar)
                {
                    dateSortedFragment.cancelMultiCheckMode();
                }else if(customActionBarArea.getChildAt(0) == albumSelectedActionBar)
                {
                    folderSortedFragment.cancelMultiCheckMode();
                }else
                {
                    if(slideLayout.getSlideState() == 1.0f)
                    {
                        slideLayout.scrollToRight();

                    }else
                    {
                        slideLayout.scrollToLeft();
                    }
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_copy:
                ArrayList<String> checkedItems1 = dateSortedFragment.getCheckedItems();
                if(checkedItems1 != null && checkedItems1.size() != 0)
                {
                    Intent intent = new Intent(this, SelectFolderActivity.class);
                    intent.setAction(ACTION_COPY);
                    intent.putExtra(SELECTED_IMAGES, checkedItems1);
                    startActivityForResult(intent, COPY_REQUEST_CODE);
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_delete:
                selectedImages = dateSortedFragment.getCheckedItems();
                if(selectedImages != null && selectedImages.size() != 0)
                {
                    ImageOperations.deleteImages(this, selectedImages, new Function<Void, Boolean>() {
                        @Override
                        public Void apply(Boolean aBoolean) {
                            if(aBoolean)
                            {
                                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                            if(viewPager.getCurrentItem() == 0)
                            {
                                dateSortedFragment.cancelMultiCheckMode();
                            }else if(viewPager.getCurrentItem() == 1)
                            {
                                folderSortedFragment.cancelMultiCheckMode();
                            }
                            return null;
                        }
                    });
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_cut:
                ArrayList<String> checkedItems2 = dateSortedFragment.getCheckedItems();
                if(checkedItems2 != null && checkedItems2.size() != 0)
                {
                    Intent intent = new Intent(this, SelectFolderActivity.class);
                    intent.setAction(ACTION_CUT);
                    intent.putExtra(SELECTED_IMAGES, checkedItems2);
                    startActivityForResult(intent, CUT_REQUEST_CODE);
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_selectAll:
                if(imageSelectAll)
                {
                    dateSortedFragment.selectNone();
                }else
                {
                    dateSortedFragment.selectAll();
                }
                break;
            case R.id.ImageSelectionActionBar_imageButton_share:
                ArrayList<String> checkedItems3 = dateSortedFragment.getCheckedItems();
                ImageOperations.shareImages(this, checkedItems3, SHARE_REQUEST_CODE);
                break;
            case R.id.AlbumSelectionActionBar_imageButton_delete:
                ArrayList<String> checkItems4 = folderSortedFragment.getCheckedItems();
                if(checkItems4 != null && checkItems4.size() != 0)
                {
                    ImageOperations.deleteFolders(MainActivity.this, checkItems4, new Function<Void, Boolean>() {
                        @Override
                        public Void apply(Boolean aBoolean) {
                            if(aBoolean)
                            {
                                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                            if(viewPager.getCurrentItem() == 0)
                            {
                                dateSortedFragment.cancelMultiCheckMode();
                            }else if(viewPager.getCurrentItem() == 1)
                            {
                                folderSortedFragment.cancelMultiCheckMode();
                            }
                            return null;
                        }
                    });
                }
                break;
            case R.id.SlideMenu_ftp_trans:
            {
                Intent intent = new Intent(this, FtpActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.SlideMenu_about:
            {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(viewPager.getCurrentItem() == 0)
        {
            dateSortedFragment.cancelMultiCheckMode();
        }else if(viewPager.getCurrentItem() == 1)
        {
            folderSortedFragment.cancelMultiCheckMode();
        }
//        showActionBar(normalActionBar);
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




    private synchronized void showActionBar(final int actionBarCode)
    {
        if(currentActionBar == actionBarCode)
        {
            return;
        }
        currentActionBar = actionBarCode;
        final View removedView = customActionBarArea.getChildAt(0);
        View toAddedActionBar = null;
        switch (actionBarCode)
        {
            case NORMAL_ACTION_BAR_CODE:
                toAddedActionBar = normalActionBar;
                break;
            case SELECT_FOLDER_ACTOPM_BAR_CODE:
                toAddedActionBar = albumSelectedActionBar;
                break;
            case SELECT_IMAGE_ACTION_BAR_CODE:
                toAddedActionBar = imageSelectedActionBar;
                break;
            default:
                break;
        }
        if(toAddedActionBar == null)
        {
            return;
        }
        final View actionBar = toAddedActionBar;
        if(removedView == actionBar)
        {
            return;
        }

        customActionBarArea.addView(actionBar);
        ViewGroup.MarginLayoutParams l = (ViewGroup.MarginLayoutParams) actionBar.getLayoutParams();
        l.topMargin = actionBar.getMeasuredHeight();
        actionBar.setLayoutParams(l);
        final boolean reverseSlideIndicator = (actionBar == normalActionBar);

        final int i = customActionBarArea.getMeasuredHeight() - customActionBarArea.getPaddingTop() - customActionBarArea.getPaddingBottom();
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
                if(!reverseSlideIndicator)
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
                    customActionBarArea.removeView(removedView);
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

    private void setActionBarTopMargin(View view, int topMargin)
    {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.topMargin = topMargin;
        params.bottomMargin = -topMargin;
        view.setLayoutParams(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        Intent intent = new Intent(this, AlbumService.class);
        startService(intent);
//        bindService(intent,connection, BIND_AUTO_CREATE);
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposables.add(disposable);
        requestNeededPermission();

    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareDailyWallpaper();
    }

    private void prepareDailyWallpaper()
    {
        Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String result = "";
                String folderPath = getExternalFilesDir(null).getPath() + "/DailyWallPaper/";
                String path = folderPath + "BingPaper-" + new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()) + ".jpg";
                File folder = new File(folderPath);
                if(!folder.exists())
                {
                    folder.mkdirs();
                }
                File imgFile = new File(path);
                if(imgFile.exists())
                {
                    dailyWallpaperPath = path;
                    return dailyWallpaperPath;
                }

                URL url = new URL(getString(R.string.bing_img_high_url));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
//                if(connection instanceof HttpsURLConnection)
//                {
//                    SSLContext sslContext = SSLContext.getDefault();
//                }
                connection.connect();
                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {

                    return "";
                }
                InputStream in = connection.getInputStream();
                OutputStream out = new FileOutputStream(imgFile);
                byte[] buffer = new byte[100];
                int readBytes = 0;
                try{
                    while((readBytes = in.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, readBytes);
                    }
                    result = path;
                }catch (Exception e)
                {
                    e.printStackTrace();
                }finally {
                    try{
                        if(in != null)
                        {
                            in.close();
                        }
                        if(out != null)
                        {
                            out.close();
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }


                return result;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        if(s != null && !s.equals(""))
                        {
                            Glide.with(MainActivity.this)
                                    .load(new File(s))
                                    .centerCrop()
                                    .into(dailyWallpaper);
                        }
                    }
                });
    }

    private void requestNeededPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
        if(requestCode != PERMISSIONS_REQUEST_CODE)
        {
            Toast.makeText(this, "授权出错", Toast.LENGTH_SHORT).show();
            return;
        }
        if(grantResults.length > 0)
        {
            for(int i : grantResults)
            {
                if(i != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "权限未获取", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            RxBus.getInstance().post(new Event(Event.ACTION_RELOAD_FROM_MEDIA_PROVIDER, null));
        }
    }

    @Override
    protected void onDestroy() {
        mDisposables.clear();
//        unbindService(connection);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                if(customActionBarArea.getChildAt(0) == imageSelectedActionBar)
                {
                    dateSortedFragment.cancelMultiCheckMode();
                    result = true;
                }else if(customActionBarArea.getChildAt(0) == albumSelectedActionBar)
                {
                    folderSortedFragment.cancelMultiCheckMode();
                    result = true;
                }else if(slideLayout.getSlideState() == 0)
                {
                    slideLayout.scrollToLeft();
                    result = true;
                }
                else
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
        viewPager = (MyViewPager)findViewById(R.id.MainActivity_ViewPager);

        slideIndicator = (ImageView)findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        slideLayout = (SlideLayout)findViewById(R.id.MainActivity_root);
        slideMenu = findViewById(R.id.MainActivity_slideMenu);
        slideLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = (int)(slideLayout.getWidth() * 0.75f);
                ViewGroup.LayoutParams layoutParams = slideMenu.getLayoutParams();
                layoutParams.width = width;
                slideMenu.setLayoutParams(layoutParams);
                slideLayout.requestLayout();
            }
        });
        dailyWallpaper = (ImageView)findViewById(R.id.SlideMenu_imageView_dailyWallpaper);
        dailyWallpaper.setOnClickListener(this);
        ftpTransButton = findViewById(R.id.SlideMenu_ftp_trans);
        ftpTransButton.setOnClickListener(this);
        aboutButton = findViewById(R.id.SlideMenu_about);
        aboutButton.setOnClickListener(this);

        customActionBarArea = (FrameLayout)findViewById(R.id.MainActivity_actionBar_custom_area);
        actionBarContainer = (ViewGroup)findViewById(R.id.MainActivity_ActionBar);

        ViewGroup.LayoutParams layoutParams = actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());





        dateSortedFragment = new DateSortedFragment();
        folderSortedFragment = new FolderSortedFragment();
        fragments.add(dateSortedFragment);
        fragments.add(folderSortedFragment);
        viewPager.setAdapter(fragmentAdapter);


        slideLayout.addOnSlideListener(new SlideLayout.OnSlideListener() {
            @Override
            public void onSlide(float process) {
                changeSlideIndicatorState(1 - process);
            }
        });

        slideIndicator.setOnClickListener(this);
        BitmapDrawable drawable1 = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.menu_000000_128));
        BitmapDrawable drawable2 = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.arrow_right_000000_128));

        drawable2.setAlpha(0);
        Drawable[] drawables = new Drawable[]{drawable1, drawable2};
        slideIndicatorDrawable = new LayerDrawable(drawables);
        slideIndicator.setImageDrawable(slideIndicatorDrawable);

        initNormalActionBar();
        initAlbumSelectionActionBar();
        initImageSelectedActionBar();
        customActionBarArea.addView(normalActionBar);


    }

    private void changeSlideIndicatorState(float process)
    {
        if(slideIndicatorDrawable == null)
        {
            return;
        }
        slideIndicator.setRotation(process * 180);
        slideIndicatorDrawable.getDrawable(0).setAlpha((int)((1-process) * 255));
        slideIndicatorDrawable.getDrawable(1).setAlpha((int)((process) * 255));
    }

    private class MyFragmentAdapter extends FragmentPagerAdapter
    {

        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position >= fragments.size() || position < 0)
            {
                return null;
            }
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public void notifyImageSelected(int selectedCount)
    {
        log.d("notifyImageSelected, count = " + selectedCount);
        if(selectedCount > 0)
        {
            if(customActionBarArea.getChildAt(0) != imageSelectedActionBar)
            {
                showActionBar(SELECT_IMAGE_ACTION_BAR_CODE);
                slideLayout.setSlideEnable(false);

            }
            viewPager.setScrollEnable(false);

        }else
        {
            if(customActionBarArea.getChildAt(0) != normalActionBar)
            {
                showActionBar(NORMAL_ACTION_BAR_CODE);
                slideLayout.setSlideEnable(true);
            }
            viewPager.setScrollEnable(true);

        }

    }

    public void notifyAlbumSelected(int selectedCount)
    {
        log.d("notifyAlbumSelected, count = " + selectedCount);
        if(selectedCount > 0)
        {
            if(customActionBarArea.getChildAt(0) != albumSelectedActionBar)
            {
                showActionBar(SELECT_FOLDER_ACTOPM_BAR_CODE);
                slideLayout.setSlideEnable(false);
            }
            viewPager.setScrollEnable(false);

        }else
        {
            if(customActionBarArea.getChildAt(0) != normalActionBar)
            {
                showActionBar(NORMAL_ACTION_BAR_CODE);
                slideLayout.setSlideEnable(true);
            }
            viewPager.setScrollEnable(true);

        }
    }

    public void notifyImageSelectAll(boolean selectAll)
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



}
