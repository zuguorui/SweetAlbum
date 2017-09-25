package com.zu.sweetalbum.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zu.sweetalbum.module.unsplash.CollectionBean;
import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.module.unsplash.SearchCollectionResultBean;
import com.zu.sweetalbum.module.unsplash.SearchPhotoResultBean;
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

    public static final String ACTION_UNSPLASH_GET_CURATED_PHOTO = "action_unsplash_get_curated_photo";
    public static final String ACTION_UNSPLASH_GET_CURATED_PHOTO_SUCCESS = "action_unsplash_get_curated_photo_success";
    public static final String ACTION_UNSPLASH_GET_CURATED_PHOTO_FAIL = "action_unsplash_get_curated_photo_fail";

    public static final String ACTION_UNSPLASH_GET_COLLECTION = "action_unsplash_get_collection";
    public static final String ACTION_UNSPLASH_GET_COLLECTION_SUCCESS = "action_unsplash_get_collection_success";
    public static final String ACTION_UNSPLASH_GET_COLLECTION_FAIL = "action_unsplash_get_collection_fail";

    public static final String ACTION_UNSPLASH_GET_CURATED_COLLECTION = "action_unsplash_get_curated_collection";
    public static final String ACTION_UNSPLASH_GET_CURATED_COLLECTION_SUCCESS = "action_unsplash_get_curated_collection_success";
    public static final String ACTION_UNSPLASH_GET_CURATED_COLLECTION_FAIL = "action_unsplash_get_curated_collection_fail";

    public static final String ACTION_UNSPLASH_SEARCH_PHOTO = "action_unsplash_search_photo";
    public static final String ACTION_UNSPLASH_SEARCH_PHOTO_SUCCESS = "action_unsplash_search_photo_success";
    public static final String ACTION_UNSPLASH_SEARCH_PHOTO_FAIL = "action_unsplash_search_photo_fail";

    public static final String ACTION_UNSPLASH_SEARCH_COLLECTION = "action_unsplash_search_collection";
    public static final String ACTION_UNSPLASH_SEARCH_COLLECTION_SUCCESS = "action_unsplash_search_collection_success";
    public static final String ACTION_UNSPLASH_SEARCH_COLLECTION_FAIL = "action_unsplash_search_collection_fail";


    public static final int CACHE_TIME = 5000;

    private LinkedList<PhotoBean> photoBeanLinkedList = new LinkedList<>();



    private static boolean serviceRunning = false;

    private UnSplashUrlTool.ListCuratedPhotosService listCuratedPhotosService = null;
    private UnSplashUrlTool.ListPhotosService listPhotosService = null;
    private UnSplashUrlTool.ListCollectionsService listCollectionsService = null;
    private UnSplashUrlTool.ListCuratedCollectionsService listCuratedCollectionsService = null;
    private UnSplashUrlTool.SearchPhotoService searchPhotoService = null;
    private UnSplashUrlTool.SearchCollectionService searchCollectionService = null;

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
                case ACTION_UNSPLASH_GET_COLLECTION:
                {
                    Bundle bundle = (Bundle)event.content;
                    int page = bundle.getInt("page", -1);
                    int perPage = bundle.getInt("per_page", -1);
                    if(page == -1 || perPage == -1)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }
                    unsplashGetCollection(page, perPage);
                }
                break;
                case ACTION_UNSPLASH_SEARCH_PHOTO:
                {
                    Bundle bundle = (Bundle)event.content;
                    int page = bundle.getInt("page", -1);
                    int perPage = bundle.getInt("per_page", -1);
                    String keyWord = bundle.getString("key_word");
                    if(page == -1 || perPage == -1)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }
                    if(keyWord == null || keyWord.equals(""))
                    {
                        throw new IllegalArgumentException("you must pass available keyword to search photo");
                    }
                    unsplashSearchPhoto(keyWord, page, perPage);
                }
                break;
                case ACTION_UNSPLASH_SEARCH_COLLECTION:
                {
                    Bundle bundle = (Bundle)event.content;
                    int page = bundle.getInt("page", -1);
                    int perPage = bundle.getInt("per_page", -1);
                    String keyWord = bundle.getString("key_word");
                    if(page == -1 || perPage == -1)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }
                    if(keyWord == null || keyWord.equals(""))
                    {
                        throw new IllegalArgumentException("you must pass available keyword to search collection");
                    }
                    unsplashSearchCollection(keyWord, page, perPage);
                }
                break;
                case ACTION_UNSPLASH_GET_CURATED_PHOTO:
                {
                    Bundle bundle = (Bundle)event.content;
                    int page = bundle.getInt("page", -1);
                    int perPage = bundle.getInt("per_page", -1);
                    String order = bundle.getString("order");
                    if(page == -1 || perPage == -1)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }
                    unsplashGetCuratedPhoto(page, perPage, order);
                }
                break;
                case ACTION_UNSPLASH_GET_CURATED_COLLECTION:
                {
                    Bundle bundle = (Bundle)event.content;
                    int page = bundle.getInt("page", -1);
                    int perPage = bundle.getInt("per_page", -1);
                    if(page == -1 || perPage == -1)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }
                    unsplashGetCuratedCollection(page, perPage);
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

    private void unsplashGetCuratedPhoto(int page, int perPage,String order)
    {
        if(listCuratedPhotosService == null)
        {
            listCuratedPhotosService = commonRetrofit.create(UnSplashUrlTool.ListCuratedPhotosService.class);
        }
        Call<LinkedList<PhotoBean>> call = listCuratedPhotosService.getPhotoList(page, perPage, order);
        call.enqueue(new Callback<LinkedList<PhotoBean>>() {
            @Override
            public void onResponse(Call<LinkedList<PhotoBean>> call, Response<LinkedList<PhotoBean>> response) {
                LinkedList<PhotoBean> linkedList = response.body();
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_PHOTO_SUCCESS, linkedList));
            }

            @Override
            public void onFailure(Call<LinkedList<PhotoBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_PHOTO_FAIL, null));
            }
        });

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

    public void unsplashGetCollection(int page, int perPage)
    {
        if(listCollectionsService == null)
        {
            listCollectionsService = commonRetrofit.create(UnSplashUrlTool.ListCollectionsService.class);
        }
        Call<LinkedList<CollectionBean>> call = listCollectionsService.getCollectionList(page, perPage);
        call.enqueue(new Callback<LinkedList<CollectionBean>>() {
            @Override
            public void onResponse(Call<LinkedList<CollectionBean>> call, Response<LinkedList<CollectionBean>> response) {
                LinkedList<CollectionBean> collectionBeenList = response.body();
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_COLLECTION_SUCCESS, collectionBeenList));
            }

            @Override
            public void onFailure(Call<LinkedList<CollectionBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_COLLECTION_FAIL, null));
            }
        });
    }

    public void unsplashGetCuratedCollection(int page, int perPage)
    {
        if(listCuratedCollectionsService == null)
        {
            listCuratedCollectionsService = commonRetrofit.create(UnSplashUrlTool.ListCuratedCollectionsService.class);
        }
        Call<LinkedList<CollectionBean>> call = listCollectionsService.getCollectionList(page, perPage);
        call.enqueue(new Callback<LinkedList<CollectionBean>>() {
            @Override
            public void onResponse(Call<LinkedList<CollectionBean>> call, Response<LinkedList<CollectionBean>> response) {
                LinkedList<CollectionBean> collectionBeenList = response.body();
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_COLLECTION_SUCCESS, collectionBeenList));
            }

            @Override
            public void onFailure(Call<LinkedList<CollectionBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_COLLECTION_FAIL, null));
            }
        });
    }

    private void unsplashSearchPhoto(@android.support.annotation.NonNull String keyWord, int page, int perPage)
    {
        if(searchPhotoService == null)
        {
            searchPhotoService = commonRetrofit.create(UnSplashUrlTool.SearchPhotoService.class);
        }

        Call<SearchPhotoResultBean> call = searchPhotoService.searchPhoto(keyWord, page, perPage);
        call.enqueue(new Callback<SearchPhotoResultBean>() {
            @Override
            public void onResponse(Call<SearchPhotoResultBean> call, Response<SearchPhotoResultBean> response) {
                SearchPhotoResultBean resultBean = response.body();
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_PHOTO_SUCCESS, resultBean));
            }

            @Override
            public void onFailure(Call<SearchPhotoResultBean> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_PHOTO_FAIL, null));
            }
        });
    }

    private void unsplashSearchCollection(@android.support.annotation.NonNull String keyword, int page, int perPage)
    {
        if(searchCollectionService == null)
        {
            searchCollectionService = commonRetrofit.create(UnSplashUrlTool.SearchCollectionService.class);
        }
        Call<SearchCollectionResultBean> call = searchCollectionService.searchCollection(keyword, page, perPage);
        call.enqueue(new Callback<SearchCollectionResultBean>() {
            @Override
            public void onResponse(Call<SearchCollectionResultBean> call, Response<SearchCollectionResultBean> response) {
                SearchCollectionResultBean resultBean = response.body();
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_COLLECTION_SUCCESS, resultBean));
            }

            @Override
            public void onFailure(Call<SearchCollectionResultBean> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_COLLECTION_FAIL, null));
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
