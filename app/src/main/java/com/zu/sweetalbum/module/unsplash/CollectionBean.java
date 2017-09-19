package com.zu.sweetalbum.module.unsplash;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zu on 17-9-15.
 */

public class CollectionBean implements Serializable{
    public String id;
    public String title;
    public String description;
    @SerializedName("total_photos")
    public int totalPhotots;

    @SerializedName("cover_photo")
    public PhotoBean coverPhoto;

    public UserBean user;

    public Links links;

    public static class Links implements Serializable{
        public String self;
        public String html;
        public String photos;
        public String related;
    }
}
