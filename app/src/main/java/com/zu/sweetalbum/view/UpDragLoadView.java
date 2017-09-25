package com.zu.sweetalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.RotateDrawable;
import android.support.annotation.NonNull;
import android.support.v4.animation.ValueAnimatorCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.view.AlbumListView.DragLoadView;

import java.util.zip.Inflater;

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
        View view = LayoutInflater.from(context).inflate(R.layout.up_drag_load_view, null);
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
    public void onLoadComplete() {
        super.onLoadComplete();
        animator.resume();
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


    }
}
