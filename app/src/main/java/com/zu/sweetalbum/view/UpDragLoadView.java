package com.zu.sweetalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.view.AlbumListView.DragLoadView;

/**
 * Created by zu on 17-9-25.
 */

public class UpDragLoadView extends DragLoadView {
    private ImageView imageView;
    private TextView textView;
    private ValueAnimator animator = null;
    public UpDragLoadView(@NonNull Context context) {
        super(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        View view = LayoutInflater.from(context).inflate(R.layout.drag_load_view, null);
        this.addView(view);
        imageView = (ImageView)view.findViewById(R.id.DragLoad_imageView);
        textView = (TextView)view.findViewById(R.id.DragLoad_textView);
        textView.setText("下拉刷新");

    }

    @Override
    public void onDrag(float process) {
        imageView.setRotation(process);
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
    public void onLoadComplete(boolean success) {
        super.onLoadComplete(success);
        animator.resume();
        if(success)
        {
            textView.setText("刷新成功");
        }else
        {
            textView.setText("刷新失败");
        }
    }

    @Override
    public void onLoadStart() {

        super.onLoadStart();
        if(animator == null)
        {
            animator = ValueAnimator.ofFloat(0f, 360f);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float degree = (float)animation.getAnimatedValue();
                    imageView.setRotation(degree);
                }
            });

        }
        animator.start();
        textView.setText("正在刷新");


    }
}
