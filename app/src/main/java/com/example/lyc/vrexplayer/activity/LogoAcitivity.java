package com.example.lyc.vrexplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.view.MyAnimationDrawable;

public class LogoAcitivity
        extends AppCompatActivity
{

    private static final int STARTACTIVITY = 1;
    private ImageView mIv_logo;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().getDecorView()
                   .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_logo_acitivity, null);
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(mLayout);


        initView();
        initData();
        initEvent();
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STARTACTIVITY:
                    Intent intent = new Intent(LogoAcitivity.this, PicAndVidioActivity.class);
                    startActivity(intent);
                    finish();

                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void initView() {
        mIv_logo = (ImageView) findViewById(R.id.iv_logo);

    }

    private void initData() {


        MyAnimationDrawable.animateRawManuallyFromXML(R.drawable.logo_animlist,
                                                      mIv_logo, new Runnable() {

                    @Override
                    public void run() {
                        // TODO onStart
                        // 动画开始时回调

                    }
                }, new Runnable() {

                    @Override
                    public void run() {
                        // TODO onComplete
                        mHandler.sendEmptyMessageDelayed(STARTACTIVITY ,1000);

                    }
                });
    }

    private void initEvent() {

    }
}
