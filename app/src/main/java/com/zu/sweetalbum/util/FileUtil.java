package com.zu.sweetalbum.util;


import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zu.sweetalbum.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;

import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

/**
 * Created by zu on 17-6-30.
 */

public class FileUtil {

    public static final String TAG = "FileUtil";
    public static final int COPY_IMAGE = 0;
    public static final int CUT_IMAGE = 1;
    public static final int DELETE_IMAGE = 2;
    public static boolean copyImages(Context context, Handler handler, ArrayList<String> srcPaths, String dest)
    {
        Log.d(TAG, "copyImages");
        boolean result = false;
        HashSet<String> copyedImages = new HashSet<>();

        if(srcPaths == null || srcPaths.size() == 0)
        {
            return true;
        }
        if(dest == null)
        {
            return false;
        }



        File destFolder = new File(dest);
        if(!destFolder.exists())
        {
            destFolder.mkdirs();
        }
        if(!dest.endsWith("/"))
        {
            dest = dest + "/";
        }
        FileInputStream in = null;
        FileOutputStream out = null;


        int i = 1;
        try{
            for(String s : srcPaths)
            {
                if(handler != null)
                {
                    Message message = Message.obtain(null, COPY_IMAGE);
                    message.arg1 = i;
                    handler.sendMessage(message);
                }
                String name = s.substring(s.lastIndexOf("/") + 1, s.length());
                File srcFile = new File(s);
                if(!srcFile.exists())
                {
                    continue;
                }
                File destFile = new File(dest + name);
                in = new FileInputStream(srcFile);
                out = new FileOutputStream(destFile);
                byte[] buffer = new byte[100];
                int readBytes = 0;
                while((readBytes = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, readBytes);
                }

                out.flush();
                out.close();
                in.close();

//                MediaScannerConnection.scanFile(context, new String[]{destFile.getPath()}, null, null);
                copyedImages.add(destFile.getPath());
                i++;

            }


            result = true;

        }catch (Exception e)
        {
            e.printStackTrace();
            result = false;
        }finally {
            try{
                if(in != null)
                {
                    in.close();
                }
                if(out != null)
                {
                    out.close();
                }
                if(copyedImages.size() != 0)
                {
                    String[] temp = new String[copyedImages.size()];
                    int j = 0;
                    for(String s : copyedImages)
                    {
                        temp[j] = s;
                        j++;
                    }
                    MediaScannerConnection.scanFile(context, temp, null, null);

                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        return result;
    }

    public static boolean deleteImage(Context context, Handler handler, ArrayList<String> srcPaths)
    {
        Log.d(TAG, "deleteImages");
        boolean result = false;
        LinkedList<String> deletedImages = new LinkedList<>();

        if(srcPaths == null || srcPaths.size() == 0)
        {
            return false;
        }
        try{
            for(int i = 0; i < srcPaths.size(); i++)
            {
                if(handler != null)
                {
                    Message message = Message.obtain(null, DELETE_IMAGE);
                    message.arg1 = i + 1;
                    handler.sendMessage(message);
                }
                File file = new File(srcPaths.get(i));
                if(file.exists())
                {
                    file.delete();
                    Log.d(TAG, "delete file");
                    deletedImages.add(srcPaths.get(i));
                }else
                {
                    Log.d(TAG, "file not exist, file.path = " + file.getCanonicalPath());
                }

            }
            if(deletedImages.size() != 0)
            {
                String[] temp = new String[deletedImages.size()];

                ContentResolver contentResolver = context.getContentResolver();
                ArrayList<ContentProviderOperation> operations = new ArrayList<>(deletedImages.size());
                int i = 0;
                Iterator<String> iterator = deletedImages.iterator();
                while(iterator.hasNext())
                {
                    String s = iterator.next();
//                    operations.add(
//                            ContentProviderOperation.newDelete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                            .withSelection(MediaStore.Images.Media.DATA + "=?", new String[]{s})
//                            .build()
//                    );
                    contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{s});
                }
//                contentResolver.applyBatch(MediaStore.AUTHORITY, operations);




            }
            result = true;
        }catch (Exception e)
        {
            e.printStackTrace();
            result = false;
        }
        if(deletedImages.size() != srcPaths.size())
        {
            result = false;
        }

        return result;

    }

    public static boolean cutImages(Context context, Handler handler, ArrayList<String> srcPaths, String dest)
    {
        Log.d(TAG, "cutImages");
        boolean result = false;
        HashSet<String> copyedImages = new HashSet<>();

        if(srcPaths == null || srcPaths.size() == 0)
        {
            return true;
        }
        if(dest == null)
        {
            return false;
        }



        File destFolder = new File(dest);
        if(!destFolder.exists())
        {
            destFolder.mkdirs();
        }
        if(!dest.endsWith("/"))
        {
            dest = dest + "/";
        }
        FileInputStream in = null;
        FileOutputStream out = null;


        int i = 1;
        try{
            for(String s : srcPaths)
            {
                if(handler != null)
                {
                    Message message = Message.obtain(null, CUT_IMAGE);
                    message.arg1 = i;
                    handler.sendMessage(message);
                }
                String name = s.substring(s.lastIndexOf("/") + 1, s.length());
                File srcFile = new File(s);
                if(!srcFile.exists())
                {
                    continue;
                }
                File destFile = new File(dest + name);
                in = new FileInputStream(srcFile);
                out = new FileOutputStream(destFile);
                byte[] buffer = new byte[100];
                int readBytes = 0;
                while((readBytes = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, readBytes);
                }

                out.flush();
                out.close();
                in.close();

                MediaScannerConnection.scanFile(context, new String[]{destFile.getPath()}, null, null);
                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "=?", new String[]{s});
                srcFile.deleteOnExit();
                i++;

            }


            result = true;

        }catch (Exception e)
        {
            e.printStackTrace();
            result = false;
        }finally {
            try{
                if(in != null)
                {
                    in.close();
                }
                if(out != null)
                {
                    out.close();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        return result;
    }

    public static boolean renameImage(@NonNull Context context, String srcPath, String destName)
    {
        Log.d(TAG, "renameImages");
        if(srcPath == null || destName == null)
        {
            return false;
        }
        File srcFile = new File(srcPath);
        if(srcFile.exists())
        {
            String folder = srcFile.getParent();
            if(!folder.endsWith("/"))
            {
                folder += "/";
            }
            File destFile = new File(folder + destName);
            if(destFile.exists())
            {
                return false;
            }
            boolean result = srcFile.renameTo(destFile);
            if(result)
            {
                MediaScannerConnection.scanFile(context, new String[]{destFile.getPath()}, null, null);
                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "=?", new String[]{srcPath});

            }
            return result;
        }else
        {
            return false;
        }
    }

    public static Uri getImageUriByPath(Context context, String path)
    {
        Uri result = null;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=?", new String[]{path}, null);
        while(cursor.moveToNext())
        {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            if(id != 0)
            {
                result = Uri.parse("content://media/external/images/media/" + id);
                break;
            }
        }
        cursor.close();
        return  result;
    }
}
