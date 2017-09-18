package com.zu.sweetalbum.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.module.unsplash.UnSplashSignInterceptor;
import com.zu.sweetalbum.util.UnSplashUrlTool;
import com.zu.sweetalbum.util.rxbus.Event;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
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

    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            switch (event.action)
            {
                case ACTION_UNSPLASH_GET_PHOTO:

                    break;
                default:
                    break;
            }
        }
    };

    public static boolean isServiceRunning()
    {
        return serviceRunning;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        serviceRunning = true;
        initCommonRetrofit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunning = false;
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
        Call<List<PhotoBean>> call = listPhotosService.getPhotoList(page, perPage, order);
        call.enqueue(new Callback<List<PhotoBean>>() {
            @Override
            public void onResponse(Call<List<PhotoBean>> call, Response<List<PhotoBean>> response) {
                List<PhotoBean> photoBeanList = response.body();

            }

            @Override
            public void onFailure(Call<List<PhotoBean>> call, Throwable t) {

            }
        });

    }

    private class PhotoRequestManager{

    }

}
