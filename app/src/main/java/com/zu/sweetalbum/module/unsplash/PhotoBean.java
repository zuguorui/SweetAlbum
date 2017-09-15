package com.zu.sweetalbum.module.unsplash;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zu on 17-9-15.
 */

public class PhotoBean {
    public UserBean user;
    public UrlBean urls;
    public Links links;
    public String id;

    public static class Links{
        public String self;
        public String html;
        public String download;
        @SerializedName("download_location")
        public String downloadLocation;
    }
}
