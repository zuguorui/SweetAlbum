package com.zu.sweetalbum.module.unsplash;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by zu on 17-9-15.
 */

public class SearchCollectionResultBean {
    public int total;
    @SerializedName("total_pages")
    public int totalPages;

    public List<CollectionBean> result;
}
