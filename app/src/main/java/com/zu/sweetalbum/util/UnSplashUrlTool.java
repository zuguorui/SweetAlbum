package com.zu.sweetalbum.util;

import android.support.annotation.NonNull;

import com.zu.sweetalbum.module.unsplash.CollectionBean;
import com.zu.sweetalbum.module.unsplash.PhotoBean;
import com.zu.sweetalbum.module.unsplash.SearchCollectionResultBean;
import com.zu.sweetalbum.module.unsplash.SearchPhotoResultBean;

import java.net.URL;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import okio.Okio;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by zu on 17-9-5.
 */

public class UnSplashUrlTool {
    public static final String CLIENT_ID = "client_id";
    public static final String APP_ID = "e042b370a2001c6ab6754b296c1fee8935e6aa0240ef54ca8871109cb12913c6";
    public static final String SECRET = "a8f18a39f5fcfcb34b2e128f4b775d484fa4515981d7b77574a746c64800fe73";

    public static final String HOST_NAME = "https://api.unsplash.com";
    public static final String COLLECTIONS = "collections";
    public static final String PHOTOS = "photos";
    public static final String SEARCH = "search";
    public static final String PAGE = "page";
    public static final String PER_PAGE = "per_page";
    public static final String QUERY = "query";

    public static final String ORDER_LATEST = "latest";
    public static final String ORDER_OLDEST = "oldest";
    public static final String ORDER_POPULAR = "popular";

    public static void signUrlWithAppId(URL url)
    {
        String s = url.toString();
        s += "&" + CLIENT_ID + "=" + APP_ID;
        try{
            url = new URL(s);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static URL listCollectionURL(Integer page, Integer perPage)
    {
        String s = HOST_NAME + "/"
                + COLLECTIONS + "?"
                + CLIENT_ID + "=" + APP_ID + "&"
                + (page == null ? "" : PAGE + page + "&")
                + (perPage == null ? "" : PER_PAGE + perPage + "&");
        try{
            URL url = new URL(s);
            return url;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }

    public static URL searchCollectionURL(@NonNull String keyWord, Integer page, Integer perPage)
    {
        String s = HOST_NAME + "/"
                + SEARCH + "/"
                + COLLECTIONS + "?"
                + CLIENT_ID + "=" + APP_ID + "&"
                + QUERY + keyWord + "&"
                + (page == null ? "" : PAGE + page + "&")
                + (perPage == null ? "" : PER_PAGE + perPage + "&");
        try{
            URL url = new URL(s);
            return url;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static URL listPhotoURL(Integer page, Integer perPage)
    {
        String s = HOST_NAME + "/"
                + PHOTOS + "?"
                + CLIENT_ID + "=" + APP_ID + "&"
                + (page == null ? "" : PAGE + page + "&")
                + (perPage == null ? "" : PER_PAGE + perPage + "&");
        try{
            URL url = new URL(s);
            return url;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static URL searchPhotoURL(@NonNull String keyWord, Integer page, Integer perPage)
    {
        String s = HOST_NAME + "/"
                + SEARCH + "/"
                + PHOTOS + "?"
                + CLIENT_ID + "=" + APP_ID + "&"
                + QUERY + keyWord + "&"
                + (page == null ? "" : PAGE + page + "&")
                + (perPage == null ? "" : PER_PAGE + perPage + "&");
        try{
            URL url = new URL(s);
            return url;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public interface ListPhotosService
    {
        @GET("/photos")
        Call<List<PhotoBean>> getPhotoList(@Query("page") int page, @Query("per_page") int perPage, @Query("order_by") String order);
    }

    public interface ListCuratedPhotosService
    {
        @GET("/photos/curated")
        Call<List<PhotoBean>> getPhotoList(@Query("page") int page, @Query("per_page") int perPage, @Query("order_by") String order);
    }

    public interface ListCollectionsService{
        @GET("/collections")
        Call<List<CollectionBean>> getCollectionList(@Query("page") int page, @Query("per_page") int perPage);
    }

    public interface ListCuratedCollectionsService{
        @GET("/collections/curated")
        Call<List<CollectionBean>> getCollectionList(@Query("page") int page, @Query("per_page") int perPage);
    }

    public interface SearchPhotoService{
        @GET("/search/photos")
        Call<SearchPhotoResultBean> searchPhoto(@Query("query") String keyWord, @Query("page") int page, @Query("per_page") int perPage);
    }

    public interface SearchCollectionService{
        @GET("/search/collections")
        Call<SearchCollectionResultBean> searchCollection(@Query("query") String keyWord, @Query("page") int page, @Query("per_page") int perPage);
    }

    public interface AccessUrlService{
        @GET("{p}")
        Call<ResponseBody> accessUrl(@Path("p") String url);
    }

}
