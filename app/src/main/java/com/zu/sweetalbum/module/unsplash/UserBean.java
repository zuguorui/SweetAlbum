package com.zu.sweetalbum.module.unsplash;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zu on 17-9-15.
 */

public class UserBean implements Serializable{
    public String id;
    public String name;
    @SerializedName("profile_image")
    public ProfileImage profileImage;

    public Links links;


    public static class ProfileImage implements Serializable
    {
        public String small;
        public String medium;
        public String large;
    }

    public static class Links implements Serializable
    {
        public String self;
        public String html;
        public String photos;
    }
}
