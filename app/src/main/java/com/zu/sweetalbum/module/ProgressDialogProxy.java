package com.zu.sweetalbum.module;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zu.sweetalbum.R;

/**
 * Created by zu on 17-6-30.
 */

public class ProgressDialogProxy {
    private AlertDialog dialog = null;
    private ProgressBar progressBar;
    private TextView messageTextView;
    private Context context;

    public ProgressDialogProxy(Context context, int max, String title) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.process_dialog, null);
        progressBar = (ProgressBar)dialogView.findViewById(R.id.ProcessDialog_processBar);
        progressBar.setMax(max);
        messageTextView = (TextView)dialogView.findViewById(R.id.ProcessDialog_textView_message);
        builder.setView(dialogView);
        builder.setTitle(title);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void setMessage(String message)
    {
        messageTextView.setText(message);
    }

    public void setProgress(int progress)
    {
        progressBar.setProgress(progress);
    }

    public void setMax(int max){
        progressBar.setMax(max);
    }

    public void dismiss() {
        if(dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
        }

    }

    public void setTitle(String title)
    {
        dialog.setTitle(title);
    }
}

