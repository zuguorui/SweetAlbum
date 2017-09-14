package com.zu.sweetalbum.util;

import android.support.annotation.NonNull;

import java.net.URL;

import okio.Okio;
import retrofit2.http.GET;

/**
 * Created by zu on 17-9-5.
 */

public class UnSplashUrlTool {
    public static final String APP_ID = "client_id=e042b370a2001c6ab6754b296c1fee8935e6aa0240ef54ca8871109cb12913c6";
    public static final String SECRET = "a8f18a39f5fcfcb34b2e128f4b775d484fa4515981d7b77574a746c64800fe73";

    public static final String HOST_NAME = "https://api.unsplash.com";
    public static final String COLLECTIONS = "collections";
    public static final String PHOTOS = "photos";
    public static final String SEARCH = "search";
    public static final String PAGE = "page=";
    public static final String PER_PAGE = "per_page=";
    public static final String QUERY = "query=";

    public static void signUrlWithAppId(URL url)
    {
        String s = url.toString();
        s += "&" + APP_ID;
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
                + APP_ID + "&"
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
                + APP_ID + "&"
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
                + APP_ID + "&"
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
                + APP_ID + "&"
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


}
