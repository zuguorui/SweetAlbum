package com.zu.sweetalbum.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zu.sweetalbum.R;
import com.zu.sweetalbum.util.CommonUtil;
import com.zu.sweetalbum.util.CrashHandler;

import java.io.File;

public class AboutActivity extends AppCompatActivity {
    private TextView titleTextView;
    private View titleActionBar;
    private ViewGroup actionBarContainer;

    private TextView aboutTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initViews();
        initAboutText();
    }

    private void initViews()
    {
        actionBarContainer = (ViewGroup)findViewById(R.id.AboutActivity_actionBar);
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
        titleTextView.setText("关于");
        customArea.addView(titleActionBar);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aboutTextView = (TextView)findViewById(R.id.AboutActivity_text);
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void initAboutText()
    {
        String github = "Github(https://github.com/zuguorui/SweetAlbum)";
        String csdn = "csdn(http://blog.csdn.net/zuguorui)";
        final String mail = "zu_guorui@126.com";

        String temp = getResources().getString(R.string.app_name) + "为个人开发，严禁任何人二次编译作为商业用途。本软件为开源软件，有兴趣的朋友可以前往我的\n" + github +"\n查看源码。";
        SpannableString ss = new SpannableString(temp);
        ss.setSpan(new UnderlineSpan(), temp.indexOf(github), temp.indexOf(github) + github.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/zuguorui/SweetAlbum"));
                startActivity(intent);
            }
        }, temp.indexOf(github), temp.indexOf(github) + github.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        stringBuilder.append(ss);
        aboutTextView.append(ss);
        temp = "或者可以到我的\n"+ csdn +"\n上查看相关技术的文章。\n";
        ss = new SpannableString(temp);
        ss.setSpan(new UnderlineSpan(), temp.indexOf(csdn), temp.indexOf(csdn) + csdn.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://blog.csdn.net/zuguorui"));
                startActivity(intent);
            }
        }, temp.indexOf(csdn), temp.indexOf(csdn) + csdn.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        stringBuilder.append(ss);
        aboutTextView.append(ss);

        temp = "如果发现bug或者发生崩溃，可发送反馈到我的邮箱：" + mail + "。（注意，将会附加崩溃日志，您也可以取消附件）。";
        ss = new SpannableString(temp);
        ss.setSpan(new UnderlineSpan(), temp.indexOf(mail), temp.indexOf(mail) + mail.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String logPath = CrashHandler.getInstance().getCrashFilePath();
                Uri uri = null;
                if(logPath != null)
                {
                    File logFile = new File(logPath);
                    uri = FileProvider.getUriForFile(AboutActivity.this, "com.zu.sweetalbum", logFile);
                }

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + "bug反馈");
                if(uri != null)
                {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                }
                intent.setType("*/*");
                intent.setType("message/rfc882");
                intent.createChooser(intent, "选择邮件客户端");
                startActivity(intent);
            }
        }, temp.indexOf(mail), temp.indexOf(mail) + mail.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        aboutTextView.append(ss);
//        stringBuilder.append(temp);
//        aboutTextView.setText(stringBuilder.toString());

    }
}
