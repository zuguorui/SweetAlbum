package com.zu.sweetalbum.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.animation.ValueAnimatorCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.view.AlbumListView.DragLoadView;

import java.util.zip.Inflater;

/**
 * Created by zu on 17-9-25.
 */

public class UpDragLoadView extends DragLoadView {
    public UpDragLoadView(@NonNull Context context, int layoutId) {
        super(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setLayoutParams(layoutParams);
        View view = LayoutInflater.from(context).inflate(layoutId, null);
        this.addView(view);
    }

    @Override
    public void onDrag(float process) {

        super.onDrag(process);
    }

    @Override
    public void onDragRelease() {
        super.onDragRelease();
    }

    @Override
    public void onDragStart() {
        super.onDragStart();
    }

    @Override
    public void onLoadComplete() {
        super.onLoadComplete();
    }
}
