package com.zu.sweetalbum.activity;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.view.TextViewPagerIndicator;

public class UnSplashActivity extends AppCompatActivity implements View.OnClickListener{
    private ViewPager mViewPager;
    private FrameLayout customActionBarArea;
    private ViewGroup actionBarContainer;
    private ImageView slideIndicator;
    private ImageView refreshButton;
    private ImageView searchButton;


    private TextViewPagerIndicator viewPagerIndicator;
    private ViewGroup actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_splash);
        initViews();
    }

    private void initViews()
    {
        actionBarContainer = (ViewGroup)findViewById(R.id.UNSPLASHActivity_ActionBar);
        customActionBarArea = (FrameLayout)actionBarContainer.findViewById(R.id.MainActivity_actionBar_custom_area);
        slideIndicator = (ImageView)actionBarContainer.findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        slideIndicator.setOnClickListener(this);
        actionBar = (ViewGroup) getLayoutInflater().inflate(R.layout.unsplash_action_bar, null);
        customActionBarArea.addView(actionBar);
        refreshButton = (ImageView)actionBar.findViewById(R.id.UnSplashActionBar_imageButton_refresh);
        refreshButton.setOnClickListener(this);
        searchButton = (ImageView)actionBar.findViewById(R.id.UnSplashActionBar_imageButton_search);
        searchButton.setOnClickListener(this);

        mViewPager = (ViewPager) findViewById(R.id.UNSPLASHActivity_ViewPager);
        viewPagerIndicator = (TextViewPagerIndicator)findViewById(R.id.UNSPLASHActivity_viewPagerIndicator);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());

        layoutParams = (ViewGroup.MarginLayoutParams) viewPagerIndicator.getLayoutParams();
        layoutParams.topMargin = -layoutParams.height;




    }

    @Override
    public void onClick(View view) {

    }
}
