package com.zu.sweetalbum.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.module.Function;
import com.zu.sweetalbum.module.ProgressDialogProxy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by zu on 17-7-4.
 */

public class ImageOperations {

    public static void deleteImages(final Context context, final ArrayList<String> selectedImages, final Function<Void, Boolean> doOnComplete)
    {
        if(selectedImages != null && selectedImages.size() != 0)
        {
            final ProgressDialogProxy progressDialogProxy = new ProgressDialogProxy(context, selectedImages.size(), "正在删除");

            final Handler updateDialogHandler = new Handler(context.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what)
                    {
                        case FileUtil.DELETE_IMAGE:
                            if(progressDialogProxy != null)
                            {
                                if(msg.arg1 == -1)
                                {
                                    progressDialogProxy.dismiss();
                                }else
                                {
                                    String message = "正在删除第" + msg.arg1 + "张，共" + selectedImages.size() + "张";
                                    progressDialogProxy.setMessage(message);
                                    progressDialogProxy.setProgress(msg.arg1);
                                }
                            }
                            break;
                    }
                    return true;
                }
            });


            String message = "确认要将" + selectedImages.size() + "张照片删除吗？删除后不可恢复。";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("删除");
            builder.setMessage(message);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    progressDialogProxy.dismiss();
                }
            });

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    progressDialogProxy.setMessage("准备删除");
                    progressDialogProxy.setProgress(0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean result = FileUtil.deleteImage(context, updateDialogHandler, selectedImages);
                            updateDialogHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(progressDialogProxy != null)
                                    {
                                        progressDialogProxy.dismiss();

                                    }
                                    if(doOnComplete != null)
                                    {
                                        doOnComplete.apply(result);
                                    }
                                }
                            });
                        }
                    }).start();
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }

    public static void deleteFolders(final Context context, final ArrayList<String> selectedImages, final Function<Void, Boolean> doOnComplete)
    {
        if(selectedImages == null || selectedImages.size() == 0)
        {
            return;
        }
        LinkedList<String> deletedFiles = new LinkedList<>();
        for(String s : selectedImages)
        {
            File folder = new File(s);
            if(folder.isFile())
            {
                continue;
            }else if(folder.isDirectory())
            {
                File[] files = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        if(name.endsWith(".jpg") || name.endsWith(".jpeg")
                                || name.endsWith(".bmp") || name.endsWith(".png")
                                || name.endsWith(".gif"))
                        {
                            return true;
                        }else
                        {
                            return false;
                        }

                    }
                });
                if(files != null && files.length != 0)
                {
                    for(File file : files)
                    {
                        deletedFiles.add(file.getPath());
                    }
                }
            }
        }
        if(deletedFiles.size() == 0)
        {
            return;
        }
        final ArrayList<String> deletePaths = new ArrayList<>(deletedFiles);
        final int count = deletePaths.size();

        final ProgressDialogProxy progressDialogProxy = new ProgressDialogProxy(context, count, "正在删除");
        final Handler updateDialogHandler = new Handler(context.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what)
                {
                    case FileUtil.DELETE_IMAGE:
                        if(progressDialogProxy != null)
                        {
                            if(msg.arg1 == -1)
                            {
                                progressDialogProxy.dismiss();

                            }else
                            {
                                String message = "正在删除第" + msg.arg1 + "张，共" + count + "张";
                                progressDialogProxy.setMessage(message);
                                progressDialogProxy.setProgress(msg.arg1);
                            }
                        }
                        break;

                }
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除相册");
        String message = "确定要删除相册吗？删除相册将会删除本文件夹目录内的所有图片，但是不会删除本文件夹内的其他文件、子文件夹以及子文件夹" +
                "内的图片";
        builder.setMessage(message);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                progressDialogProxy.dismiss();

            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                progressDialogProxy.setMessage("准备删除");
                progressDialogProxy.setProgress(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean result = FileUtil.deleteImage(context, updateDialogHandler, deletePaths);
                        updateDialogHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialogProxy.dismiss();
                                if(doOnComplete != null)
                                {
                                    doOnComplete.apply(result);
                                }
                            }
                        });

                    }
                }).start();

            }
        });

        builder.create().show();

    }

    public static void shareImages(Activity activity, ArrayList<String> selectedImages, int... requestCode)
    {
        if(selectedImages != null && selectedImages.size() != 0)
        {
            ArrayList<Uri> uris = new ArrayList<>(selectedImages.size());

            for(String s : selectedImages)
            {
                Uri uri = FileUtil.getImageUriByPath(activity, s);
                if(uri != null)
                {
                    uris.add(uri);

                }

            }
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(requestCode != null)
            {
                activity.startActivityForResult(Intent.createChooser(intent, "请选择"), requestCode[0]);
            }else
            {
                activity.startActivity(Intent.createChooser(intent, "请选择"));
            }


        }
    }

    public static void copyImages(final Context context, final ArrayList<String> srcPaths, final String dest, final Function<Void, Boolean> doOnComplete)
    {
        if(srcPaths != null && srcPaths.size() != 0)
        {
            final ProgressDialogProxy progressDialogProxy = new ProgressDialogProxy(context, srcPaths.size(), "正在复制");
            final Handler updateDialogHandler = new Handler(context.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what)
                    {
                        case FileUtil.COPY_IMAGE:
                            if(progressDialogProxy != null)
                            {
                                if(msg.arg1 == -1)
                                {
                                    progressDialogProxy.dismiss();

                                }else
                                {
                                    String message = "正在复制第" + msg.arg1 + "张，共" + srcPaths.size() + "张";
                                    progressDialogProxy.setMessage(message);
                                    progressDialogProxy.setProgress(msg.arg1);
                                }
                            }
                            break;

                    }
                    return true;
                }
            });
            final String albumName = dest.substring(dest.lastIndexOf("/") + 1, dest.length());
            String message = "确认要将" + srcPaths.size() + "张照片复制到" + albumName + "中吗？";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("复制");
            builder.setMessage(message);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    progressDialogProxy.dismiss();
                }
            });

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    progressDialogProxy.setMessage("准备复制");
                    progressDialogProxy.setProgress(0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean result = FileUtil.copyImages(context,updateDialogHandler, srcPaths, dest);
                            updateDialogHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(progressDialogProxy != null)
                                    {
                                        progressDialogProxy.dismiss();
                                    }

                                    if(doOnComplete != null)
                                    {
                                        doOnComplete.apply(result);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            });
            builder.create().show();
        }
    }

    public static void cutImages(final Context context, final ArrayList<String> srcPaths, final String dest, final Function<Void, Boolean> doOnComplete)
    {
        if(srcPaths != null && srcPaths.size() != 0)
        {
            final ProgressDialogProxy progressDialogProxy = new ProgressDialogProxy(context, srcPaths.size(), "正在移动");
            final Handler updateDialogHandler = new Handler(context.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what)
                    {
                        case FileUtil.CUT_IMAGE:
                            if(progressDialogProxy != null)
                            {
                                if(msg.arg1 == -1)
                                {
                                    progressDialogProxy.dismiss();

                                }else
                                {
                                    String message = "正在移动第" + msg.arg1 + "张，共" + srcPaths.size() + "张";
                                    progressDialogProxy.setMessage(message);
                                    progressDialogProxy.setProgress(msg.arg1);
                                }
                            }
                            break;

                    }
                    return true;
                }
            });
            final String albumName = dest.substring(dest.lastIndexOf("/") + 1, dest.length());
            String message = "确认要将" + srcPaths.size() + "张照片移动到" + albumName + "中吗？";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("移动");
            builder.setMessage(message);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    progressDialogProxy.dismiss();
                }
            });

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    progressDialogProxy.setMessage("准备移动");
                    progressDialogProxy.setProgress(0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean result = FileUtil.cutImages(context, updateDialogHandler, srcPaths, dest);
                            updateDialogHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(progressDialogProxy != null)
                                    {
                                        progressDialogProxy.dismiss();
                                    }
                                    if(doOnComplete != null)
                                    {
                                        doOnComplete.apply(result);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            });
            builder.create().show();
        }
    }

    public static void renameImage(@NonNull final Context context, @NonNull final String srcPath, final Function<Void, Boolean> doOnComplete)
    {
        String srcName = srcPath.substring(srcPath.lastIndexOf("/") + 1, srcPath.length());


        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View inputView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_input_text, null);
        final EditText editText = (EditText)inputView.findViewById(R.id.Dialog_input);
        editText.setText(srcName);
        builder.setView(inputView);
        builder.setTitle("重命名");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editable editable = editText.getText();
                if(editable.length() == 0)
                {
                    Toast.makeText(context, "文件名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String destName = editable.toString();
                if(destName.trim().equals("") || destName.startsWith(" ") || destName.endsWith(" "))
                {
                    Toast.makeText(context, "文件名不能以空格开头或结尾", Toast.LENGTH_SHORT).show();
                    return;
                }
                File srcFile = new File(srcPath);
                if(!srcFile.exists())
                {
                    Toast.makeText(context, "源文件不存在", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                String folder = srcFile.getParent();
                if(!folder.endsWith("/"))
                {
                    folder += "/";
                }
                final String destPath = folder + destName;
                File destFile = new File(destPath);
                final StringBuilder sb = new StringBuilder();

                if(destFile.exists())
                {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle("目标文件已存在");
                    builder1.setMessage("目标文件"+destName+"已存在，是否覆盖？");
                    builder1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            sb.append("N");
                            return;
                        }
                    });
                    builder1.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> path = new ArrayList<String>();
                            path.add(destPath);
                            FileUtil.deleteImage(context, null, path);
                            boolean result = FileUtil.renameImage(context, srcPath, destName);
                            dialog.dismiss();
                            Toast.makeText(context, (result == true ? "重命名成功" : "重命名失败"), Toast.LENGTH_SHORT).show();
                            if(doOnComplete != null)
                            {
                                doOnComplete.apply(result);
                            }
                        }
                    });
                    builder1.create().show();

                }else
                {
                    boolean result = FileUtil.renameImage(context, srcPath, destName);
                    dialog.dismiss();
                    Toast.makeText(context, (result == true ? "重命名成功" : "重命名失败"), Toast.LENGTH_SHORT).show();
                    if(doOnComplete != null)
                    {
                        doOnComplete.apply(result);
                    }
                }

            }
        });
        builder.create().show();

    }
}
