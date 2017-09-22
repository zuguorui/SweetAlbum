package com.zu.sweetalbum.module.unsplash;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by zu on 17-9-15.
 */

public class SearchPhotoResultBean implements Serializable{
    public int total;
    @SerializedName("total_pages")
    public int totalPages;

    public LinkedList<PhotoBean> result;
}
