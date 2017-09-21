package com.zu.sweetalbum.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.module.unsplash.UnSplashSignInterceptor;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.UnSplashUrlTool;
import com.zu.sweetalbum.util.rxbus.Event;
import com.zu.sweetalbum.util.rxbus.RxBus;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zu on 2017/9/16.
 */

public class UnSplashService extends Service {
    public static final String ACTION_UNSPLASH_GET_PHOTO = "action_unsplash_get_photo";
    public static final String ACTION_UNSPLASH_GET_PHOTO_SUCCESS = "action_unsplash_get_photo_success";
    public static final String ACTION_UNSPLASH_GET_PHOTO_FAIL = "action_unsplash_get_photo_fail";


    public static final int CACHE_TIME = 5000;

    private LinkedList<PhotoBean> photoBeanLinkedList = new LinkedList<>();



    private static boolean serviceRunning = false;

    private UnSplashUrlTool.ListPhotosService listPhotosService = null;
    private Retrofit commonRetrofit = null;
    private PhotoListManager photoListManager = new PhotoListManager();

    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            switch (event.action)
            {
                case ACTION_UNSPLASH_GET_PHOTO:
                {
                    Bundle bundle = (Bundle)event.content;
                    int page = bundle.getInt("page", -1);
                    int perPage = bundle.getInt("per_page", -1);
                    String order = bundle.getString("order");
                    if(page == -1 || perPage == -1)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }
                    unsplashGetPhoto(page, perPage, order);
                }
                    break;

                default:
                    break;
            }
        }
    };

    private Consumer errorConsumer = new Consumer() {
        @Override
        public void accept(@NonNull Object o) throws Exception {

        }
    };

    private CompositeDisposable mDisposable = new CompositeDisposable();

    public static boolean isServiceRunning()
    {
        return serviceRunning;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        serviceRunning = true;
        initCommonRetrofit();
        Disposable disposable = RxBus.getInstance().toObservable().subscribe(messageConsumer, errorConsumer);
        mDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunning = false;
        mDisposable.clear();
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initCommonRetrofit()
    {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new UnSplashSignInterceptor())
                .addInterceptor(new CacheInterceptor())
                .build();



        commonRetrofit = new Retrofit.Builder()
                .baseUrl(UnSplashUrlTool.HOST_NAME)
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    private void unsplashGetPhoto(int page, int perPage, String order)
    {

        if(listPhotosService == null) {
            listPhotosService = commonRetrofit.create(UnSplashUrlTool.ListPhotosService.class);
        }
        Call<LinkedList<PhotoBean>> call = listPhotosService.getPhotoList(page, perPage, order);
        call.enqueue(new Callback<LinkedList<PhotoBean>>() {
            @Override
            public void onResponse(Call<LinkedList<PhotoBean>> call, Response<LinkedList<PhotoBean>> response) {
                LinkedList<PhotoBean> photoBeanList = response.body();
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_PHOTO_SUCCESS, photoBeanList));
            }

            @Override
            public void onFailure(Call<LinkedList<PhotoBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_PHOTO_FAIL, null));
            }
        });

    }

    private class PhotoListManager{
        public LinkedList<PhotoBean> data;
        public int lastPage = 0;
        public int lastPerPage = 0;
    }

    private class CacheInterceptor implements Interceptor
    {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {

            boolean forceNet = false;
            int netStatus = CommonUtil.getNetState();
            Request request = chain.request();
            if(request.url().pathSegments().contains("search"))
            {
                forceNet = true;
            }
            if(!forceNet)
            {
                if(netStatus == CommonUtil.NET_INVALID)
                {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
            }

            okhttp3.Response response = chain.proceed(request);
            if(!forceNet)
            {
                response = response.newBuilder()
                        .removeHeader("cache_control")
                        .removeHeader("Pragma")
                        .header("Cache_Control", "max-age=" + (1000 * 60))
                        .build();
            }
            return response;
        }
    }

}
