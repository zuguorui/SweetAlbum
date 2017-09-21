package com.zu.sweetalbum.module.unsplash;

import com.zu.sweetalbum.util.UnSplashUrlTool;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.zu.sweetalbum.util.UnSplashUrlTool.APP_ID;
import static com.zu.sweetalbum.util.UnSplashUrlTool.CLIENT_ID;

/**
 * Created by zu on 17-9-15.
 */

public class UnSplashSignInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
//        HttpUrl url = request.url().newBuilder()
//                .addQueryParameter(CLIENT_ID, APP_ID)
//                .build();
//        request = request.newBuilder().url(url).build();
        request = request.newBuilder()
                .header("Authorization", "Client-ID " + UnSplashUrlTool.APP_ID)
                .build();

        return chain.proceed(request);
    }


}
