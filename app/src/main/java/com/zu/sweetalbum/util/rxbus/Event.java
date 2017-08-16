package com.zu.sweetalbum.util.rxbus;

/**
 * Created by zu on 17-6-12.
 */

public class Event {

    public Object content = null;

    public String action = null;


    public Event(String action, Object content) {
        this.content = content;
        this.action = action;
    }

    public static final String ACTION_LOAD_COMPLETE = "action_load_complete";
    public static final String ACTION_SORT_BY_DATE_FAIL = "action_sort_by_date_fail";
    public static final String ACTION_SORT_BY_DATE_SUCCESS = "action_sort_by_date_success";
    public static final String ACTION_NO_IMAGE = "action_no_image";
    public static final String ACTION_NO_CAMERA_IMAGE = "action_no_camera_image";
    public static final String ACTION_SORT_BY_FOLDER_FAIL = "action_sort_by_folder_fail";
    public static final String ACTION_SORT_BY_FOLDER_SUCCESS = "action_sort_by_folder_success";

    public static final String ACTION_GET_DATE_SORTED_LIST = "action_get_date_sorted_list";
    public static final String ACTION_GET_FOLDER_SORTED_LIST = "action_get_folder_sorted_list";
    public static final String ACTION_GET_ALBUM_LIST = "action_get_album_list";
    public static final String ACTION_ALBUM_LIST_PREPARE_SUCCESS = "action_album_list_prepare_success";
    public static final String ACTION_ALBUM_LIST_PREPARE_FAIL = "action_album_list_prepare_fail";

    public static final String ACTION_DELETE_IMAGE = "action_delete_image";
    public static final String ACTION_COPY_IMAGE = "action_copy_image";
    public static final String ACTION_CUT_IMAGE = "action_cut_image";
    public static final String ACTION_FAVORITE_IMAGE = "action_favorite_image";
    public static final String ACTION_RENAME_IMAGE = "action_rename_image";
    public static final String ACTION_DELETE_IMAGE_SUCCESS = "action_delete_image_success";
    public static final String ACTION_DELETE_IMAGE_FAIL = "action_delete_image_fail";
    public static final String ACTION_COPY_IMAGE_SUCCESS = "action_copy_image_success";
    public static final String ACTION_COPY_IMAGE_FAIL = "action_copy_image_fail";
    public static final String ACTION_DATA_UPDATE = "action_data_update";
    public static final String ACTION_RELOAD_FROM_MEDIA_PROVIDER = "action_reload_from_media_provider";

}
