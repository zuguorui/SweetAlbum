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
import java.util.Objects;
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

    public static final String FLAG_ACHIEVE_END = "flag_achieve_end";


    public static final int CACHE_TIME = 5000;

    private  int PER_PAGE = -1;

    private LinkedList<PhotoBean> photoBeanList = new LinkedList<>();
    private LinkedList<PhotoBean> curatedPhotoBeanList = new LinkedList<>();
    private LinkedList<CollectionBean> collectionBeanList = new LinkedList<>();
    private LinkedList<CollectionBean> curatedCollectionBeanList = new LinkedList<>();

    private LinkedList<PhotoBean> searchPhotoResultList = new LinkedList<>();
    private LinkedList<CollectionBean> searchCollectionResultList = new LinkedList<>();



    private static boolean serviceRunning = false;

    private UnSplashUrlTool.ListCuratedPhotosService listCuratedPhotosService = null;
    private UnSplashUrlTool.ListPhotosService listPhotosService = null;
    private UnSplashUrlTool.ListCollectionsService listCollectionsService = null;
    private UnSplashUrlTool.ListCuratedCollectionsService listCuratedCollectionsService = null;
    private UnSplashUrlTool.SearchPhotoService searchPhotoService = null;
    private UnSplashUrlTool.SearchCollectionService searchCollectionService = null;

    private Status photoStatus = new Status();
    private Status curatedPhotoStatus = new Status();
    private Status collectionStatus = new Status();
    private Status curatedCollectionStatus = new Status();
    private Status searchPhotoStatus = new Status();
    private Status searchCollectionStatus = new Status();

    private Retrofit commonRetrofit = null;


    private Consumer messageConsumer = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {
            if(PER_PAGE == -1)
            {
                Object fromObj = event.getExtra("from"), toObj = event.getExtra("to");
                if(fromObj != null && toObj != null)
                {
                    int from = (int)fromObj;
                    int to = (int)toObj;
                    PER_PAGE = to - from;
                }
            }
            switch (event.action)
            {
                case ACTION_UNSPLASH_GET_PHOTO:
                {
                    Object fromObj = event.getExtra("from"), toObj = event.getExtra("to"), orderObj = event.getExtra("order");
                    if(fromObj == null || toObj == null)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }

                    int from = (int)fromObj;
                    int to = (int)toObj;
                    String order = (String)orderObj;
                    unsplashGetPhoto(from, to, order);
                }
                    break;
                case ACTION_UNSPLASH_GET_COLLECTION:
                {
                    Object fromObj = event.getExtra("from"), toObj = event.getExtra("to");
                    if(fromObj == null || toObj == null)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }

                    int from = (int)fromObj;
                    int to = (int)toObj;

                    unsplashGetCollection(from, to);
                }
                break;
                case ACTION_UNSPLASH_SEARCH_PHOTO:
                {
                    Object fromObj = event.getExtra("from"), toObj = event.getExtra("to"),
                    keywordObj = event.getExtra("keyword");
                    if(fromObj == null || toObj == null || keywordObj == null)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }

                    int from = (int)fromObj;
                    int to = (int)toObj;
                    String keyWord = (String)keywordObj;
                    unsplashSearchPhoto(keyWord, from, to);
                }
                break;
                case ACTION_UNSPLASH_SEARCH_COLLECTION:
                {
                    Object fromObj = event.getExtra("from"), toObj = event.getExtra("to"),
                            keywordObj = event.getExtra("keyword");
                    if(fromObj == null || toObj == null || keywordObj == null)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }

                    int from = (int)fromObj;
                    int to = (int)toObj;
                    String keyWord = (String)keywordObj;
                    unsplashSearchCollection(keyWord, from, to);
                }
                break;
                case ACTION_UNSPLASH_GET_CURATED_PHOTO:
                {
                    Object fromObj = event.getExtra("from"), toObj = event.getExtra("to"), orderObj = event.getExtra("order");
                    if(fromObj == null || toObj == null)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }

                    int from = (int)fromObj;
                    int to = (int)toObj;
                    String order = (String)orderObj;
                    unsplashGetCuratedPhoto(from, to, order);
                }
                break;
                case ACTION_UNSPLASH_GET_CURATED_COLLECTION:
                {
                    Object fromObj = event.getExtra("from"), toObj = event.getExtra("to");
                    if(fromObj == null || toObj == null)
                    {
                        throw new IllegalArgumentException("you must pass available page and per_page to UnSplashService");
                    }

                    int from = (int)fromObj;
                    int to = (int)toObj;
                    unsplashGetCuratedCollection(from, to);
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
//                .addInterceptor(new CacheInterceptor())
                .build();



        commonRetrofit = new Retrofit.Builder()
                .baseUrl(UnSplashUrlTool.HOST_NAME)
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    /**
     * 检查目标链表是否满足数据要求。
     *
     * @param from 请求数据开始的index
     * @param to 请求数据结束的index，区间为[from, to)
     * @param srcList 数据来源链表
     * @param targetList 结果集
     *
     * @return 0代表请求的数据范围完全在已有数据中。-1代表完全不在。1代表部分在
     * */
    private int checkAndFillList(int from, int to, LinkedList srcList, LinkedList targetList)
    {
        if(srcList == null)
        {
            return -1;
        }
        int size = srcList.size();
        if(targetList == null)
        {
            targetList = new LinkedList();
        }

        if(size >= to)
        {
            for(Object o : srcList.subList(from, to))
            {
                targetList.add(o);
            }


            return 0;
        }

        if(size <= from)
        {
            return -1;
        }

        if(to >= size && from < size)
        {
            for(Object o : srcList.subList(from, size))
            {
                targetList.add(o);
            }

            return 1;
        }
        return -1;
    }

    private void unsplashGetCuratedPhoto(final int from, final int to, final String order)
    {
        if(listCuratedPhotosService == null)
        {
            listCuratedPhotosService = commonRetrofit.create(UnSplashUrlTool.ListCuratedPhotosService.class);
        }

        LinkedList<PhotoBean> result = new LinkedList<>();
        int code = checkAndFillList(from, to, curatedPhotoBeanList, result);
        if(code == 0)
        {
            RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_PHOTO_SUCCESS, result));
            return;
        }

        if(curatedPhotoStatus.achieveEnd == true)
        {
            Event event = new Event(ACTION_UNSPLASH_GET_CURATED_PHOTO_SUCCESS, result);
            event.putExtra(FLAG_ACHIEVE_END, true);
            RxBus.getInstance().post(event);
            return;
        }


        Call<LinkedList<PhotoBean>> call = listCuratedPhotosService.getPhotoList(curatedPhotoStatus.pageNum, PER_PAGE, order);
        call.enqueue(new Callback<LinkedList<PhotoBean>>() {
            @Override
            public void onResponse(Call<LinkedList<PhotoBean>> call, Response<LinkedList<PhotoBean>> response) {
                LinkedList<PhotoBean> linkedList = response.body();

                if(linkedList != null && linkedList.size() != 0)
                {
                    curatedPhotoBeanList.addAll(linkedList);
                    curatedPhotoStatus.pageNum++;
                }

                if(linkedList == null || linkedList.size() < PER_PAGE)
                {
                    curatedPhotoStatus.achieveEnd = true;
                }
                unsplashGetCuratedPhoto(from, to, order);

            }

            @Override
            public void onFailure(Call<LinkedList<PhotoBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_PHOTO_FAIL, null));
            }
        });

    }

    private void unsplashGetPhoto(final int from, final int to, final String order)
    {

        if(listPhotosService == null) {
            listPhotosService = commonRetrofit.create(UnSplashUrlTool.ListPhotosService.class);
        }
        LinkedList<PhotoBean> result = new LinkedList<>();
        int code = checkAndFillList(from, to, photoBeanList, result);
        if(code == 0)
        {
            RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_PHOTO_SUCCESS, result));
            return;
        }

        if(photoStatus.achieveEnd == true)
        {
            Event event = new Event(ACTION_UNSPLASH_GET_PHOTO_SUCCESS, result);
            event.putExtra(FLAG_ACHIEVE_END, true);
            RxBus.getInstance().post(event);
            return;
        }

        Call<LinkedList<PhotoBean>> call = listPhotosService.getPhotoList(photoStatus.pageNum, PER_PAGE, order);
        call.enqueue(new Callback<LinkedList<PhotoBean>>() {
            @Override
            public void onResponse(Call<LinkedList<PhotoBean>> call, Response<LinkedList<PhotoBean>> response) {
                int code = response.code();
                LinkedList<PhotoBean> list = response.body();
                if(list != null && list.size() != 0)
                {
                    photoBeanList.addAll(list);
                    photoStatus.pageNum++;
                }

                if(list == null || list.size() < PER_PAGE)
                {
                    photoStatus.achieveEnd = true;
                }
                unsplashGetPhoto(from, to, order);

            }

            @Override
            public void onFailure(Call<LinkedList<PhotoBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_PHOTO_FAIL, null));
            }
        });

    }

    public void unsplashGetCollection(final int from, final int to)
    {
        if(listCollectionsService == null)
        {
            listCollectionsService = commonRetrofit.create(UnSplashUrlTool.ListCollectionsService.class);
        }

        LinkedList<CollectionBean> result = new LinkedList<>();
        int code = checkAndFillList(from, to, collectionBeanList, result);
        if(code == 0)
        {
            RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_COLLECTION_SUCCESS, result));
            return;
        }

        if(collectionStatus.achieveEnd == true)
        {
            Event event = new Event(ACTION_UNSPLASH_GET_COLLECTION_SUCCESS, result);
            event.putExtra(FLAG_ACHIEVE_END, true);
            RxBus.getInstance().post(event);
            return;
        }

        Call<LinkedList<CollectionBean>> call = listCollectionsService.getCollectionList(collectionStatus.pageNum, PER_PAGE);
        call.enqueue(new Callback<LinkedList<CollectionBean>>() {
            @Override
            public void onResponse(Call<LinkedList<CollectionBean>> call, Response<LinkedList<CollectionBean>> response) {
                LinkedList<CollectionBean> list = response.body();
                if(list != null && list.size() != 0)
                {
                    collectionBeanList.addAll(list);
                    collectionStatus.pageNum++;
                }

                if(list == null || list.size() < PER_PAGE)
                {
                    collectionStatus.achieveEnd = true;
                }

                unsplashGetCollection(from, to);
            }

            @Override
            public void onFailure(Call<LinkedList<CollectionBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_COLLECTION_FAIL, null));
            }
        });
    }

    public void unsplashGetCuratedCollection(final int from, final int to)
    {
        if(listCuratedCollectionsService == null)
        {
            listCuratedCollectionsService = commonRetrofit.create(UnSplashUrlTool.ListCuratedCollectionsService.class);
        }

        LinkedList<CollectionBean> result = new LinkedList<>();
        int code = checkAndFillList(from, to, curatedCollectionBeanList, result);
        if(code == 0)
        {
            RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_COLLECTION_SUCCESS, result));
            return;
        }

        if(curatedCollectionStatus.achieveEnd == true)
        {
            Event event = new Event(ACTION_UNSPLASH_GET_CURATED_COLLECTION_SUCCESS, result);
            event.putExtra(FLAG_ACHIEVE_END, true);
            RxBus.getInstance().post(event);
            return;
        }

        Call<LinkedList<CollectionBean>> call = listCollectionsService.getCollectionList(curatedCollectionStatus.pageNum, PER_PAGE);
        call.enqueue(new Callback<LinkedList<CollectionBean>>() {
            @Override
            public void onResponse(Call<LinkedList<CollectionBean>> call, Response<LinkedList<CollectionBean>> response) {
                LinkedList<CollectionBean> list = response.body();
                if(list != null && list.size() != 0)
                {
                    curatedCollectionBeanList.addAll(list);
                    curatedCollectionStatus.pageNum++;
                }
                if(list == null || list.size() < PER_PAGE)
                {
                    curatedCollectionStatus.achieveEnd = true;
                }
                unsplashGetCuratedCollection(from, to);
            }

            @Override
            public void onFailure(Call<LinkedList<CollectionBean>> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_GET_CURATED_COLLECTION_FAIL, null));
            }
        });
    }

    private void unsplashSearchPhoto(@android.support.annotation.NonNull final String keyWord, final int from, final int to)
    {
        if(searchPhotoService == null)
        {
            searchPhotoService = commonRetrofit.create(UnSplashUrlTool.SearchPhotoService.class);
        }

        LinkedList<PhotoBean> result = new LinkedList<>();
        int code = checkAndFillList(from, to, searchPhotoResultList, result);
        if(code == 0)
        {
            RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_PHOTO_SUCCESS, result));
            return;
        }

        if(searchPhotoStatus.achieveEnd == true)
        {
            Event event = new Event(ACTION_UNSPLASH_SEARCH_PHOTO_SUCCESS, result);
            event.putExtra(FLAG_ACHIEVE_END, true);
            RxBus.getInstance().post(event);
            return;
        }

        Call<SearchPhotoResultBean> call = searchPhotoService.searchPhoto(keyWord, searchPhotoStatus.pageNum, PER_PAGE);
        call.enqueue(new Callback<SearchPhotoResultBean>() {
            @Override
            public void onResponse(Call<SearchPhotoResultBean> call, Response<SearchPhotoResultBean> response) {
                SearchPhotoResultBean resultBean = response.body();
                if(resultBean != null)
                {
                    if(resultBean.result != null && resultBean.result.size() != 0)
                    {
                        searchPhotoResultList.addAll(resultBean.result);
                        searchPhotoStatus.pageNum++;
                    }
                    if(resultBean.result == null || resultBean.result.size() < PER_PAGE)
                    {
                        searchPhotoStatus.achieveEnd = true;
                    }
                    unsplashSearchPhoto(keyWord, from, to);

                }else
                {
                    RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_PHOTO_FAIL, null));
                }


            }

            @Override
            public void onFailure(Call<SearchPhotoResultBean> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_PHOTO_FAIL, null));
            }
        });
    }

    private void unsplashSearchCollection(@android.support.annotation.NonNull final String keyword, final int from, final int to)
    {
        if(searchCollectionService == null)
        {
            searchCollectionService = commonRetrofit.create(UnSplashUrlTool.SearchCollectionService.class);
        }

        LinkedList<CollectionBean> result = new LinkedList<>();
        int code = checkAndFillList(from, to, searchCollectionResultList, result);
        if(code == 0)
        {
            RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_COLLECTION_SUCCESS, result));
            return;
        }

        if(searchCollectionStatus.achieveEnd == true)
        {
            Event event = new Event(ACTION_UNSPLASH_SEARCH_COLLECTION_SUCCESS, result);
            event.putExtra(FLAG_ACHIEVE_END, true);
            RxBus.getInstance().post(event);
            return;
        }

        Call<SearchCollectionResultBean> call = searchCollectionService.searchCollection(keyword, searchCollectionStatus.pageNum, PER_PAGE);
        call.enqueue(new Callback<SearchCollectionResultBean>() {
            @Override
            public void onResponse(Call<SearchCollectionResultBean> call, Response<SearchCollectionResultBean> response) {
                SearchCollectionResultBean resultBean = response.body();
                if(resultBean != null)
                {
                    if(resultBean.result != null && resultBean.result.size() != 0)
                    {
                        searchCollectionResultList.addAll(resultBean.result);
                        searchCollectionStatus.pageNum++;
                    }
                    if(resultBean.result == null || resultBean.result.size() < PER_PAGE)
                    {
                        searchCollectionStatus.achieveEnd = true;
                    }
                    unsplashSearchCollection(keyword, from, to);

                }else
                {
                    RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_COLLECTION_FAIL, null));
                }
            }

            @Override
            public void onFailure(Call<SearchCollectionResultBean> call, Throwable t) {
                RxBus.getInstance().post(new Event(ACTION_UNSPLASH_SEARCH_COLLECTION_FAIL, null));
            }
        });
    }



    private class Status{
        public int pageNum = 0;
        public boolean achieveEnd = false;
    }

    private interface OnNetCallback{
        void onSuccess();
        void onFail();
        void onSuccess(int code);
        void onFail(String message);
    }

    private class OnNetCallbackImpl implements OnNetCallback
    {

        private List<Object> targetList = null;
        private int page, perPage;

        public OnNetCallbackImpl(List<Object> targetList, int page, int perPage) {
            this.targetList = targetList;
            this.page = page;
            this.perPage = perPage;
        }

        @Override
        public void onSuccess() {
            onSuccess(0);
        }

        @Override
        public void onFail() {
            onFail(null);
        }

        @Override
        public void onSuccess(int code) {

        }

        @Override
        public void onFail(String message) {

        }
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

