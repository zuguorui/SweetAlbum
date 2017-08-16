package com.zu.sweetalbum.util;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

/**
 * Created by zu on 17-6-29.
 */

public class ImageObserver extends ContentObserver {
    public static final int CONTENT_CHANGE = 1;
    private Handler mHandler = null;
    public ImageObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Message message = Message.obtain(null, CONTENT_CHANGE);
        if(mHandler != null)
        {
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
    }
}
