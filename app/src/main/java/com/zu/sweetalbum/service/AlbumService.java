package com.zu.sweetalbum.service;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.ImageModule;
import com.zu.sweetalbum.util.Comparators;
import com.zu.sweetalbum.util.ImageObserver;
import com.zu.sweetalbum.util.MyLog;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;


/**
 * Created by zu on 17-6-9.
 */

public class AlbumService extends Service {

    public static final String ACTION_LOAD_COMPLETE = "action_load_complete";
    public static final String ACTION_SORT_BY_DATE_FAIL = "action_sort_by_date_fail";
    public static final String ACTION_SORT_BY_DATE_SUCCESS = "action_sort_by_date_success";
    public static final String ACTION_NO_IMAGE = "action_no_image";
    public static final String ACTION_NO_CAMERA_IMAGE = "action_no_camera_image";
    public static final String ACTION_SORT_BY_FOLDER_FAIL = "action_sort_by_folder_fail";
    public static final String ACTION_SORT_BY_FOLDER_SUCCESS = "action_sort_by_folder_success";

    public static final String ACTION_GET_DATE_SORTED_LIST = "action_get_date_sorted_list";
    public static final String ACTION_GET_FOLDER_SORTED_LIST = "action_get_folder_sorted_list";
    public static final String ACTION_GET_ALBUM_LIST = "action_get_album_list";
    public static final String ACTION_ALBUM_LIST_PREPARE_SUCCESS = "action_album_list_prepare_success";
    public static final String ACTION_ALBUM_LIST_PREPARE_FAIL = "action_album_list_prepare_fail";

    public static final String ACTION_DELETE_IMAGE = "action_delete_image";
    public static final String ACTION_COPY_IMAGE = "action_copy_image";
    public static final String ACTION_CUT_IMAGE = "action_cut_image";
    public static final String ACTION_FAVORITE_IMAGE = "action_favorite_image";
    public static final String ACTION_RENAME_IMAGE = "action_rename_image";
    public static final String ACTION_DELETE_IMAGE_SUCCESS = "action_delete_image_success";
    public static final String ACTION_DELETE_IMAGE_FAIL = "action_delete_image_fail";
    public static final String ACTION_COPY_IMAGE_SUCCESS = "action_copy_image_success";
    public static final String ACTION_COPY_IMAGE_FAIL = "action_copy_image_fail";
    public static final String ACTION_DATA_UPDATE = "action_data_update";
    public static final String ACTION_RELOAD_FROM_MEDIA_PROVIDER = "action_reload_from_media_provider";

    private MyLog log = new MyLog("AlbumService", true);
    public class AlbumBinder extends Binder{
        public Service getService()
        {
            return AlbumService.this;
        }
    }



    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            switch (event.action)
            {
                case ACTION_GET_DATE_SORTED_LIST:
//                    getDateSortedList();
                    postToTaskQueue(GET_DATE_SORTED_LIST);
                    break;
                case ACTION_GET_FOLDER_SORTED_LIST:
//                    getFolderSortedList();
                    postToTaskQueue(GET_FOLDER_SORTED_LIST);
                    break;
                case ACTION_RELOAD_FROM_MEDIA_PROVIDER:
                    postToTaskQueue(UPDATE_DATA);
                    break;
                case ACTION_GET_ALBUM_LIST:
                    final String target = (String)event.content;
                    taskQueenExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            log.d("ACTION_GET_ALBUM_LIST");
                            taskQueenRunning = true;
                            if("date".equals(target))
                            {
                                RxBus.getInstance().post(new Event(ACTION_ALBUM_LIST_PREPARE_SUCCESS, dateSortedImages));
                            }else if(target != null)
                            {
                                LinkedList<ImageModule> imageModules = folderSortedImages.get(target);
                                RxBus.getInstance().post(new Event(ACTION_ALBUM_LIST_PREPARE_SUCCESS, imageModules));
                            }
                            checkTaskQueue();
                            taskQueenRunning  = false;
                        }
                    });

                    break;

            }
        }
    };
    private Consumer<? super Throwable> messageErrorConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(@NonNull Throwable throwable) throws Exception {
            log.e(throwable.getMessage());
        }
    };

    private Observer loadImageObserver = new Observer() {

        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(@NonNull Object o) {
            ImageModule imageModule = (ImageModule)o;
            images.add(imageModule);
            if(imageModule.isCamera)
            {
                cameraImages.add(imageModule);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
//            log.v("complete");
        }

    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case ImageObserver.CONTENT_CHANGE:
                    log.d("ImageObserver.CONTENT_CHANGE");
//                        sortByFolderAfterLoad = true;
//                        sortByDateAfterLoad = true;
//                        dateSortedImages.clear();
//                        folderSortedImages.clear();
//                        images.clear();
//                        cameraImages.clear();
//                        getImageListFromMediaStore();
                    postToTaskQueue(UPDATE_DATA);
                    break;
            }
            return true;
        }
    });

    private ImageObserver imageObserver;

    private AlbumBinder mBinder = new AlbumBinder();
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Observable imageLoadObservable = null;



    private boolean sortByDateAfterLoad = false;
    private boolean sortByFolderAfterLoad = false;
    private boolean noImages = false;
    private boolean noCameraImages = false;
    private volatile HashSet<ImageModule> images = new HashSet<>();
    private volatile HashSet<ImageModule> cameraImages = new HashSet<>();
    private volatile HashMap<String,LinkedList<ImageModule>> dateSortedImages = new HashMap<>();
    private volatile HashMap<String, LinkedList<ImageModule>> folderSortedImages = new HashMap<>();
    private LoadFromMediaThread loadFromMediaThread = null;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private static final int UPDATE_DATA = 1;
    private static final int GET_DATE_SORTED_LIST = 2;
    private static final int GET_FOLDER_SORTED_LIST = 3;
    private static final int GET_ALBUM_LIST = 4;
    private LinkedList<Integer> taskQueue = new LinkedList<>();
    private ExecutorService taskQueenExecutor = Executors.newSingleThreadExecutor();
    private boolean taskQueenRunning = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Disposable disable = RxBus.getInstance().toObservable().subscribe(messageConsumer, messageErrorConsumer);
        mDisposables.add(disable);
        postToTaskQueue(UPDATE_DATA);
//        getImageListFromMediaStore();
        imageObserver = new ImageObserver(mHandler);
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imageObserver);
    }

    @Override
    public void onDestroy() {
        mDisposables.clear();
        getContentResolver().unregisterContentObserver(imageObserver);
        super.onDestroy();

    }

    private void checkTaskQueue()
    {
        if(taskQueue != null && taskQueue.size() != 0)
        {
            int task = taskQueue.pollFirst();
            while(true)
            {
                if(taskQueue.size() == 0)
                {
                    break;
                }
                if(taskQueue.getFirst() == task)
                {
                    taskQueue.pollFirst();
                }else
                {
                    break;
                }
            }
            switch (task)
            {
                case UPDATE_DATA:
                    taskQueenExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            taskQueenRunning = true;
//                            postToTaskQueue(GET_DATE_SORTED_LIST);
//                            postToTaskQueue(GET_FOLDER_SORTED_LIST);
                            getImageListFromMediaStore();
                            sortImagesByDate();
                            sortImagesByFolder();
                            RxBus.getInstance().post(new Event(ACTION_DATA_UPDATE, null));
                            checkTaskQueue();
                            taskQueenRunning = false;
                        }
                    });
                    break;
                case GET_DATE_SORTED_LIST:
                    taskQueenExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            taskQueenRunning = true;
//                            getDateSortedList();
                            RxBus.getInstance().post(new Event(ACTION_SORT_BY_DATE_SUCCESS, dateSortedImages));
                            checkTaskQueue();
                            taskQueenRunning = false;
                        }
                    });
                    break;
                case GET_FOLDER_SORTED_LIST:
                    taskQueenExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            taskQueenRunning = true;
//                            getFolderSortedList();
                            RxBus.getInstance().post(new Event(ACTION_SORT_BY_FOLDER_SUCCESS, folderSortedImages));
                            checkTaskQueue();
                            taskQueenRunning = false;
                        }
                    });
                    break;

            }
        }
    }

    private void postToTaskQueue(int id)
    {
        taskQueue.addLast(id);
        log.d("taskQueue.size = " + taskQueue.size());
        if(!taskQueenRunning)
        {
            checkTaskQueue();
        }
    }

    private boolean checkPermission()
    {
        boolean result = true;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            {
                result = false;
            }
        }
        return  result;

    }

    private void getImageListFromMediaStore()
    {
        if(!checkPermission())
        {
//            Toast.makeText(this, "未获取读写存储权限", Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            images.clear();
            cameraImages.clear();
            dateSortedImages.clear();
            folderSortedImages.clear();
            ContentResolver resolver = getContentResolver();

            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            while(cursor.moveToNext())
            {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                String parent = path.substring(0, path.lastIndexOf("/"));
                String bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                ImageModule imageModule = new ImageModule(path, createDate, modifyDate, parent, (bucketName.equals("Camera") ? true : false));
                images.add(imageModule);
                if(imageModule.isCamera)
                {
                    cameraImages.add(imageModule);
                }
            }
            cursor.close();
            if(images.size() == 0)
            {
                noImages = true;
            }else
            {
                noImages = false;
            }
            if(cameraImages.size() == 0)
            {
                noCameraImages = true;
            }else
            {
                noCameraImages = false;
            }
//        log.d("images.size = " + images.size());
        }catch (Exception e)
        {
            e.printStackTrace();
//            Toast.makeText(this, "读取媒体数据库失败", Toast.LENGTH_SHORT).show();
        }


    }

    private void getDateSortedList()
    {
        if(dateSortedImages == null || dateSortedImages.size() == 0)
        {
            if(cameraImages == null || cameraImages.size() == 0)
            {
                if(!noCameraImages)
                {
                    sortByDateAfterLoad = true;
                    getImageListFromMediaStore();

                }else
                {
                    RxBus.getInstance().post(new Event(ACTION_NO_IMAGE, null));
                }
            }else
            {
                sortImagesByDate();
                RxBus.getInstance().post(new Event(ACTION_SORT_BY_DATE_SUCCESS, dateSortedImages));

            }
        }else
        {
            RxBus.getInstance().post(new Event(ACTION_SORT_BY_DATE_SUCCESS, dateSortedImages));
        }
    }



    private void sortImagesByDate()
    {
        if(cameraImages == null || cameraImages.size() == 0)
        {
            return;
        }

        dateSortedImages.clear();
        Iterator<ImageModule> iterator = cameraImages.iterator();
        while(iterator.hasNext())
        {
            ImageModule i = iterator.next();
            String date = dateFormatter.format(new Date(i.createDate));
            if(dateSortedImages.get(date) == null)
            {
                dateSortedImages.put(date, new LinkedList<ImageModule>());
            }
            dateSortedImages.get(date).add(i);
        }
        Set<String> keys = dateSortedImages.keySet();
        for(String key : keys)
        {
            dateSortedImages.put(key, sortImageByDate(dateSortedImages.get(key)));
        }
    }


    private void getFolderSortedList()
    {

        if(folderSortedImages == null || folderSortedImages.size() == 0)
        {
            if(images == null || images.size() == 0)
            {
                if(!noImages)
                {
                    sortByFolderAfterLoad = true;
                    getImageListFromMediaStore();
                }else
                {
                    RxBus.getInstance().post(new Event(ACTION_NO_IMAGE, null));
                }
            }else
            {
                sortImagesByFolder();
                RxBus.getInstance().post(new Event(ACTION_SORT_BY_FOLDER_SUCCESS, folderSortedImages));
            }
        }else
        {
            RxBus.getInstance().post(new Event(ACTION_SORT_BY_FOLDER_SUCCESS, folderSortedImages));
        }
    }

    private void sortImagesByFolder()
    {
        if(images == null || images.size() == 0)
        {
            return;
        }
        folderSortedImages.clear();
        Iterator<ImageModule> iterator = images.iterator();
        while(iterator.hasNext())
        {
            ImageModule i = iterator.next();
            String folder = i.parentFolder;
            if(folderSortedImages.get(folder) == null)
            {
                folderSortedImages.put(folder, new LinkedList<ImageModule>());
            }
            folderSortedImages.get(folder).add(i);
        }
        Set<String> keys = folderSortedImages.keySet();
        for(String key : keys)
        {
            folderSortedImages.put(key, sortImageByDate(folderSortedImages.get(key)));
        }
    }

    private LinkedList<ImageModule> sortImageByDate(Collection<ImageModule> collection)
    {
        LinkedList<ImageModule> result = new LinkedList<>();
        for(ImageModule imageModule : collection)
        {
            int i = 0;
            Iterator<ImageModule> iterator = result.iterator();
            while(iterator.hasNext())
            {
                ImageModule temp = iterator.next();
                if(Comparators.compareDate(imageModule, temp) > 0)
                {
                    break;
                }
                i++;
            }
            result.add(i, imageModule);
        }
        return result;
    }



    private void initLoadImageObservable()
    {
        imageLoadObservable = Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(@NonNull ObservableEmitter e) throws Exception {
                ContentResolver resolver = getContentResolver();

                Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                while(cursor.moveToNext())
                {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                    String parent = path.substring(0, path.lastIndexOf("/"));
                    String bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    ImageModule imageModule = new ImageModule(path, createDate, modifyDate, parent, (bucketName.equals("Camera") ? true : false));
                    e.onNext(imageModule);
                }
                cursor.close();
                e.onComplete();
            }
        });

    }

    private class LoadFromMediaThread extends Thread
    {
        private boolean stop = false;
        @Override
        public void run() {
            images.clear();
            ContentResolver resolver = getContentResolver();

            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            while(cursor.moveToNext())
            {
                if(stop == true)
                {
                    cursor.close();
                    return;
                }
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                String parent = path.substring(0, path.lastIndexOf("/"));
                String bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                ImageModule imageModule = new ImageModule(path, createDate, modifyDate, parent, (bucketName.equals("Camera") ? true : false));
                images.add(imageModule);
                if(imageModule.isCamera)
                {
                    cameraImages.add(imageModule);
                }
            }
            cursor.close();
            if(images.size() == 0)
            {
                noImages = true;
            }
            if(cameraImages.size() == 0)
            {
                noCameraImages = true;
            }

//            if(sortByDateAfterLoad)
//            {
//                getDateSortedList();
//                sortByDateAfterLoad = false;
//            }
//            if(sortByFolderAfterLoad)
//            {
//                getFolderSortedList();
//                sortByFolderAfterLoad = false;
//            }
            log.d("images.size = " + images.size());
        }

        @Override
        public void interrupt() {
            stop = true;
            super.interrupt();
        }
    }

}
