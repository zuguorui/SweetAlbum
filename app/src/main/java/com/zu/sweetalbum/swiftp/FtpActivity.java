package com.zu.sweetalbum.swiftp;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.activity.FileBrowserActivity;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.MyLog;

import org.w3c.dom.Text;

import java.net.InetAddress;

public class FtpActivity extends AppCompatActivity implements View.OnClickListener{
    private MyLog log = new MyLog("FtpActivity", true);
    private static final int FILE_BROWSE_REQUEST_CODE = 1;

    private EditText userNameTextView;
    private EditText passwordTextView;
    private SwitchCompat startStopSwitch;
    private CheckBox allowAnonymousCheckBox;
    private CheckBox keepAwakeCheckBox;

    private TextView ftpStateTextView;
    private TextView ftpAddressTextView;
    private TextView rootDirTextView;
    private View ftpStateLayout;

    private TextView titleTextView;
    private View titleActionBar;
    private ViewGroup actionBarContainer;

    private static final int runningColor = Color.parseColor("#33ffff");
    private static final int stopColor = Color.parseColor("#999999");

    private ValueAnimator valueAnimator = null;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initRunningState();
            switch (intent.getAction())
            {
                case FsService.ACTION_STARTED:
                    Toast.makeText(FtpActivity.this, "服务器已启动", Toast.LENGTH_SHORT).show();
                    animateStartOrStop(true);
                    break;
                case FsService.ACTION_STOPPED:
                    Toast.makeText(FtpActivity.this, "服务器已停止", Toast.LENGTH_SHORT).show();
                    animateStartOrStop(false);
                    break;
                case FsService.ACTION_FAILEDTOSTART:
                    Toast.makeText(FtpActivity.this, "服务器启动失败", Toast.LENGTH_SHORT).show();
                    animateStartOrStop(false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);
        initViews();
        initRunningState();



    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FsService.ACTION_FAILEDTOSTART);
        intentFilter.addAction(FsService.ACTION_STARTED);
        intentFilter.addAction(FsService.ACTION_STOPPED);
        registerReceiver(receiver, intentFilter);
        initRunningState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    private void initViews()
    {
        actionBarContainer = (ViewGroup)findViewById(R.id.FtpActivity_actionBar);
        ViewGroup.LayoutParams layoutParams = actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());


        ImageView backArrow = (ImageView)actionBarContainer.findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        backArrow.setImageResource(R.drawable.back_000000_128);

        FrameLayout customArea = (FrameLayout)findViewById(R.id.MainActivity_actionBar_custom_area);
        titleActionBar = getLayoutInflater().inflate(R.layout.text_action_bar, null);
        titleTextView = (TextView)titleActionBar.findViewById(R.id.TextActionBar_textView_albumName);
        titleTextView.setText("无线传图");
        customArea.addView(titleActionBar);

        backArrow.setOnClickListener(this);

        ftpStateLayout = findViewById(R.id.FtpActivity_layout_ftpState);
        ftpStateTextView = (TextView)findViewById(R.id.FtpActivity_textView_ftpState);
        ftpAddressTextView = (TextView)findViewById(R.id.FtpActivity_textView_ftpAddress);
        rootDirTextView = (TextView)findViewById(R.id.FtpActivity_textView_choseRootDir);
        rootDirTextView.setOnClickListener(this);

        userNameTextView = (EditText)findViewById(R.id.FtpActivity_editText_userName);
        passwordTextView = (EditText)findViewById(R.id.FtpActivity_editText_password);


        startStopSwitch = (SwitchCompat)findViewById(R.id.FtpActivity_switch_startStop);
        allowAnonymousCheckBox = (CheckBox)findViewById(R.id.FtpActivity_checkBox_allowAnonymous);
        keepAwakeCheckBox = (CheckBox)findViewById(R.id.FtpActivity_checkBox_keepAwake);

        userNameTextView.setText(FsSettings.getUserName());
        passwordTextView.setText(FsSettings.getPassWord());
        allowAnonymousCheckBox.setChecked(FsSettings.allowAnoymous());
        keepAwakeCheckBox.setChecked(FsSettings.shouldTakeFullWakeLock());

        userNameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    String text = userNameTextView.getText().toString();
                    if(!text.equals(FsSettings.getUserName()))
                    {
                        FsSettings.setUserName(text);
                    }

                }
            }
        });

        passwordTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    String text = passwordTextView.getText().toString();
                    if(!text.equals(FsSettings.getPassWord()))
                    {
                        FsSettings.setPassWord(text);
                    }
                }
            }
        });



        allowAnonymousCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FsSettings.setAllowAnonymous(isChecked);
            }
        });
        keepAwakeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FsSettings.setTakeFullWakeLock(isChecked);
            }
        });

        startStopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    Intent intent = new Intent(FsService.ACTION_START_FTPSERVER);
                    sendBroadcast(intent);
                }else
                {
                    Intent intent = new Intent(FsService.ACTION_STOP_FTPSERVER);
                    sendBroadcast(intent);
                }
            }
        });

        if(FsService.isRunning())
        {
            ftpStateLayout.setBackgroundColor(runningColor);
        }else
        {
            ftpStateLayout.setBackgroundColor(stopColor);
        }

    }

    private void animateStartOrStop(boolean start)
    {
        if(valueAnimator == null)
        {
            valueAnimator = ValueAnimator.ofArgb(stopColor, runningColor);
            valueAnimator.setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ftpStateLayout.setBackgroundColor((int)animation.getAnimatedValue());
                }
            });
        }
        if(start)
        {
            valueAnimator.start();
        }else
        {
            valueAnimator.reverse();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.MainActivity_ActionBar_slideIndicator:
                finish();
                break;
            case R.id.FtpActivity_textView_choseRootDir:
                Intent intent = new Intent(this, FileBrowserActivity.class);
                intent.putExtra("path", FsSettings.getChrootDirAsString());
                startActivityForResult(intent, FILE_BROWSE_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case FILE_BROWSE_REQUEST_CODE:
                if(resultCode == RESULT_OK)
                {
                    String path = data.getStringExtra("selected_path");
                    if(path != null)
                    {
                        boolean result = FsSettings.setChrootDir(path);
                        if(result)
                        {
                            rootDirTextView.setText(path);
                        }else
                        {
                            Toast.makeText(this, "设置根目录失败", Toast.LENGTH_SHORT).show();
                        }
                    }else
                    {
                        Toast.makeText(this, "返回目录为空", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
    }

    private void initRunningState()
    {
        boolean isServerRunning = FsService.isRunning();
        userNameTextView.setEnabled(!isServerRunning);
        passwordTextView.setEnabled(!isServerRunning);
        startStopSwitch.setChecked(isServerRunning);
        allowAnonymousCheckBox.setEnabled(!isServerRunning);
        keepAwakeCheckBox.setEnabled(!isServerRunning);

        rootDirTextView.setText(FsSettings.getChrootDirAsString());
        rootDirTextView.setEnabled(!isServerRunning);
        if(isServerRunning)
        {
            InetAddress address = FsService.getLocalInetAddress();
            ftpStateTextView.setText("FTP服务正在运行");
            StringBuilder sb = new StringBuilder();
            sb.append("FTP运行在ftp://");
            sb.append(address.getHostAddress() + ":" + FsSettings.getPortNumber());
            ftpAddressTextView.setText(sb.toString());

        }else
        {
            ftpStateTextView.setText("FTP服务已停止");
            ftpAddressTextView.setText("");
        }
    }
}
