package com.zu.sweetalbum.util;

import android.content.Context;

import com.zu.sweetalbum.App;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by zu on 17-7-8.
 */

public class CommonUtil {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    private static DecimalFormat decimalFormat = new DecimalFormat(".00");
    public static LinkedList<String> sortKey(Collection<String> keys)
    {
        LinkedList<String> result = new LinkedList<>();
        for(String key : keys)
        {
            int i = 0;
            Iterator<String> iterator = result.iterator();
            while(iterator.hasNext())
            {
                String item = iterator.next();
                if(key.compareTo(item) > 0)
                {
                    break;
                }
                i++;
            }
            result.add(i, key);
        }
        return result;
    }

    public static int getStatusBarHeight() {
        int result = 0;
        Context context = App.getAppContext();
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String formatDate(long time)
    {
        return dateFormat.format(time);
    }

    public static String getFormatedSize(long sizeInByte)
    {
        float mSize = 0f;
        if(sizeInByte >> 30 > 0)
        {
            mSize = sizeInByte * 1.0f / (1024 * 1024 * 1024);

            return decimalFormat.format(mSize) + "GB";
        }

        if(sizeInByte >> 20 > 0)
        {
            mSize = sizeInByte * 1.0f / (1024 * 1024);
            return decimalFormat.format(mSize) + "MB";
        }

        if(sizeInByte >> 10 > 0)
        {
            mSize = sizeInByte * 1.0f / 1024;
            return decimalFormat.format(mSize) + "KB";
        }

        return sizeInByte + "B";
    }
}
