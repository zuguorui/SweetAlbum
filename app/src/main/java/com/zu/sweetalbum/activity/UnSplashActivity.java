package com.zu.sweetalbum.activity;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.view.TextViewPagerIndicator;

public class UnSplashActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private FrameLayout customActionBarArea;
    private ViewGroup actionBarContainer;
    private ImageView slideIndicator;

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
        customActionBarArea = (FrameLayout)actionBar.findViewById(R.id.MainActivity_actionBar_custom_area);
        slideIndicator = (ImageView)actionBar.findViewById(R.id.MainActivity_ActionBar_slideIndicator);

        mViewPager = (ViewPager) findViewById(R.id.UNSPLASHActivity_ViewPager);

    }
}
