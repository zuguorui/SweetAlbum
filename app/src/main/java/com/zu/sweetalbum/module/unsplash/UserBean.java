package com.zu.sweetalbum.module.unsplash;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zu on 17-9-15.
 */

public class UserBean {
    public String id;
    public String name;
    @SerializedName("profile_image")
    public ProfileImage profileImage;

    public Links links;


    public static class ProfileImage
    {
        public String small;
        public String medium;
        public String large;
    }

    public static class Links
    {
        public String self;
        public String html;
        public String photos;
    }
}
