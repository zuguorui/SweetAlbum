package com.zu.sweetalbum.util;

import com.zu.sweetalbum.module.ImageModule;

import java.util.Comparator;

/**
 * Created by zu on 17-7-1.
 */

public class Comparators {

    public static int compareDate(ImageModule o1, ImageModule o2)
    {
        int result = 0;
        if (o1.createDate > o2.createDate)
        {
            result = 1;
        }else if(o1.createDate < o2.createDate)
        {
            result = -1;
        }else
        {
            result = 0;
        }
        return result;
    }

    public static int comparePath(ImageModule o1, ImageModule o2)
    {
        return o1.path.compareTo(o2.path);
    }

    public static int compareName(ImageModule o1, ImageModule o2)
    {
        String name1 = o1.path.substring(o1.path.lastIndexOf("/") + 1, o1.path.length());
        String name2 = o2.path.substring(o2.path.lastIndexOf("/") + 1, o2.path.length());
        return name1.compareTo(name2);
    }
    public static class DateComparator implements Comparator<ImageModule>
    {
        @Override
        public int compare(ImageModule o1, ImageModule o2) {
            int result = 0;
            if (o1.createDate > o2.createDate)
            {
                result = 1;
            }else if(o1.createDate < o2.createDate)
            {
                result = -1;
            }else
            {
                result = 0;
            }
            return result;
        }
    }

    public static class PathComparator implements Comparator<ImageModule>
    {
        @Override
        public int compare(ImageModule o1, ImageModule o2) {
            int result = 0;
            return o1.path.compareTo(o2.path);
        }
    }

    public static class NameComparator implements Comparator<ImageModule>
    {
        @Override
        public int compare(ImageModule o1, ImageModule o2) {
            String name1 = o1.path.substring(o1.path.lastIndexOf("/") + 1, o1.path.length());
            String name2 = o2.path.substring(o2.path.lastIndexOf("/") + 1, o2.path.length());
            return name1.compareTo(name2);
        }
    }
}
