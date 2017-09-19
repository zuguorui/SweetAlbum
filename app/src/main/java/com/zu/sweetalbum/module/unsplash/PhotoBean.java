package com.zu.sweetalbum.module.unsplash;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zu on 17-9-15.
 */

public class PhotoBean implements Serializable{
    public UserBean user;
    public UrlBean urls;
    public Links links;
    public String id;

    public static class Links implements Serializable{

        public Links()
        {

        }
//        @Override
//        public int describeContents() {
//            return 0;
//        }

//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeString(self);
//            dest.writeString(html);
//            dest.writeString(download);
//            dest.writeString(downloadLocation);
//        }
//        private Links(Parcel in)
//        {
//            self = in.readString();
//            html = in.readString();
//            download = in.readString();
//            downloadLocation = in.readString();
//        }





        public String self;
        public String html;
        public String download;
        @SerializedName("download_location")
        public String downloadLocation;
    }
}
