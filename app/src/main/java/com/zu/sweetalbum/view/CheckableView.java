package com.zu.sweetalbum.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.view.AlbumListView.CheckableItem;

/**
 * Created by zu on 17-7-24.
 */

public class CheckableView extends CheckableItem {
    private CheckBox checkBox;
    private boolean checkable = false;
    private Drawable foreGround = null;
    private ImageView gifMark;



    public CheckableView(@NonNull Context context) {
        this(context, null);
    }

    public CheckableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        checkBox = new CheckBox(context);
        MarginLayoutParams layoutParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = getPaddingLeft();
        layoutParams.topMargin = getPaddingTop();
        checkBox.setLayoutParams(layoutParams);
        addView(checkBox);

        checkBox.setVisibility(GONE);

        gifMark = new ImageView(context);
        MarginLayoutParams layoutParams1 = new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        layoutParams1.rightMargin = 0;
        layoutParams1.bottomMargin = 0;

        gifMark.setLayoutParams(layoutParams1);
        gifMark.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        gifMark.setImageResource(R.drawable.gif_512);

        gifMark.setVisibility(GONE);

        addView(gifMark);

        if(foreGround == null)
        {
            foreGround = getResources().getDrawable(R.drawable.image_button_background, null);
        }
        setForeground(foreGround);
    }

    @Override
    public void setCheckable(boolean checkable) {
        if(checkable)
        {
            checkBox.setVisibility(VISIBLE);
        }else
        {
            checkBox.setChecked(false);
            checkBox.setVisibility(GONE);
        }
        this.checkable = checkable;



    }

    public void setIsGif(boolean isGif)
    {
        if(isGif)
        {
            gifMark.setVisibility(VISIBLE);
        }else
        {
            gifMark.setVisibility(GONE);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if(checkable)
        {
            checkBox.setChecked(checked);
        }
    }

    @Override
    public boolean isChecked() {
        if(!checkable)
        {
            return false;
        }
        return checkBox.isChecked();
    }

    @Override
    public void toggle() {
        if(checkable)
        {
            checkBox.setChecked(!checkBox.isChecked());
        }
    }
}
