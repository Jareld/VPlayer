package com.example.lyc.vrexplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lyc.vrexplayer.R;

import java.io.File;

public class PicAndVidioActivity
        extends AppCompatActivity
        implements View.OnClickListener
{

    private static final String TAG = "PicAndVidioActivity";
    private Button mTv_video, mTv_pic, mTv_pic_right, mTv_video_right;

    private LinearLayout mLl_right;
    private static final int REQUEST_READ_STORAGE = 0;
    private static final int SWITCH_PIC           = 1;
    private GradientDrawable mDrawable;
    private View             mLayout;
    private int[] pic_video_bg        = new int[]{R.mipmap.test_bg_1,
                                                  R.mipmap.test_bg_4,
                                                  R.mipmap.test_bg_5};
    private int[] pic_video_button_bg = new int[]{R.mipmap.test_button_1,
                                                  R.mipmap.test_button_4,
                                                  R.mipmap.test_button_5};

    private int     pic_video_bg_num = 0;
    private Handler mHandler         = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SWITCH_PIC:
                    if (pic_video_bg_num == 5) {
                        pic_video_bg_num = 0;
                    } else {
                        pic_video_bg_num++;
                    }
                    Log.d(TAG, "handleMessage: lunxun");

                    Glide.with(PicAndVidioActivity.this)
                         .load(pic_video_bg[pic_video_bg_num])
                         .centerCrop()
                         .diskCacheStrategy(DiskCacheStrategy.RESULT)
                         .into(mIv_left);
                    Glide.with(PicAndVidioActivity.this)
                         .load(pic_video_bg[pic_video_bg_num])
                         .centerCrop()
                         .diskCacheStrategy(DiskCacheStrategy.RESULT)
                         .into(mIv_right);
                    break;
            }
        }
    };
    private LinearLayout mll_left;
    private String       Wifi_Path;
    private ImageView    mIv_left;
    private ImageView    mIv_right;
    private Button       mBtn_left_wifi;
    private Button       mBtn_right_wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().getDecorView()
                   .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_new_pic_and_vidio, null);
        //mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(mLayout);

        initView();
        initEvent();
        initData();
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: layout" + mLayout.getSystemUiVisibility());
                if (mLayout.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE) {
                    mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                } else {
                    mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

            }
        });

    }

    private String Pic_Path;
    private String Video_Path;


    private void initData() {
        //先检查权限 如果没有权限 先申请权限
        checkPermissions();
        mDrawable = new GradientDrawable();
        mDrawable.setShape(GradientDrawable.RECTANGLE); // 画框
        mDrawable.setStroke(8, Color.RED); // 边框粗细及颜色
        mDrawable.setColor(Color.parseColor("#50000000")); // 边框内部颜色

        //mTv_pic.setBackgroundDrawable(mDrawable);
        //  mTv_video.setBackgroundColor(Color.parseColor("#50000000"));
        mTv_pic.setTextColor(Color.parseColor("#ff0000"));
        mTv_pic_right.setTextColor(Color.parseColor("#ff0000"));
        //左右保持同步
        //  mTv_pic_right.setBackgroundDrawable(mDrawable);

        // mTv_video_right.setBackgroundColor(Color.parseColor("#50000000"));

        //来一个轮训器
        // mHandler.sendEmptyMessageDelayed(SWITCH_PIC , 5000);

    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                                               Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestReadStoragePermission();
            Log.d(TAG, "checkPermissions: yes");
        } else {
            Log.d(TAG, "checkPermissions: no");
            SharedPreferences sp = getSharedPreferences("Path", Activity.MODE_PRIVATE);

            Pic_Path = sp.getString("Pic_Path", "0");
            Video_Path = sp.getString("Video_Path", "0");

            if (Pic_Path.equals("0")) {
                String PVRootPath = getPicAndVideoPath();
                Log.d(TAG, "initData:PVRootPath " + PVRootPath);
                //在这个路径下    看有没有VrexMedia这个文件夹  如果没有的话就创建  如果有的话
                boolean isExist = checkoutVrexMeidaIsExist(PVRootPath);
                //如果有这个文件夹  再看看有没有 pic和video两个文件夹  如果有 为这两个赋值 如果没有的话就要进行创建
                Log.d(TAG, "initData: isExist" + isExist);
                if (isExist) {
                    Log.d(TAG, "initData: isExist");
                    //如果有    再看
                    checkoutPicAndVideIsExist(PVRootPath);
                } else {
                    //如果没有  就要先创建VrexMedia这个
                    String path       = PVRootPath + "/Samson";
                    File   VMPathFile = new File(path);
                    VMPathFile.mkdirs();
                    //再创建 其余两个
                    File picsFile = new File(path + "/Pictures");
                    picsFile.mkdirs();
                    File videosFile = new File(path + "/Videos");
                    videosFile.mkdirs();
                    File wifiFile = new File(path + "/Wifi-Tranfer");
                    wifiFile.mkdirs();
                }
                //第一次申请后放进sp里面去
                Pic_Path = PVRootPath + "/Samson/Pictures";
                Video_Path = PVRootPath + "/Samson/Videos";
                Wifi_Path = PVRootPath + "/Samson/Wifi-Tranfer";
            }
        }

    }

    private void requestReadStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            Log.d(TAG, "requestReadStoragePermission: 1");

        } else {
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                              REQUEST_READ_STORAGE);


        }
    }

    //权限申请的反馈
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        Log.v(TAG, "[onRequestPermissionsResult]");
        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //如果允许这样的权限的话
                //查看是否有外部存储卡   如果有  返回外部路径
                //如果没有返回自己的路径
                String PVRootPath = getPicAndVideoPath();
                Log.d(TAG, "initData:PVRootPath " + PVRootPath);
                //在这个路径下    看有没有VrexMedia这个文件夹  如果没有的话就创建  如果有的话
                boolean isExist = checkoutVrexMeidaIsExist(PVRootPath);
                //如果有这个文件夹  再看看有没有 pic和video两个文件夹  如果有 为这两个赋值 如果没有的话就要进行创建
                Log.d(TAG, "initData: isExist" + isExist);
                if (isExist) {
                    Log.d(TAG, "initData: isExist");
                    //如果有    再看
                    checkoutPicAndVideIsExist(PVRootPath);
                } else {
                    //如果没有  就要先创建VrexMedia这个
                    String path       = PVRootPath + "/Samson";
                    File   VMPathFile = new File(path);
                    VMPathFile.mkdirs();
                    //再创建 其余两个
                    File picsFile = new File(path + "/Pictures");
                    picsFile.mkdirs();
                    File videosFile = new File(path + "/Videos");
                    videosFile.mkdirs();
                    File wifiFile = new File(path + "/Wifi-Tranfer");
                    wifiFile.mkdirs();
                }
                //第一次申请后放进sp里面去
                SharedPreferences        sp   = getSharedPreferences("Path", Activity.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                Pic_Path = PVRootPath + "/Samson/Pictures";
                Video_Path = PVRootPath + "/Samson/Videos";
                Wifi_Path = PVRootPath + "/Samson/Wifi-Tranfer";
                edit.putString("Pic_Path", Pic_Path);
                edit.putString("Video_Path", Video_Path);
                edit.putString("Wifi-Tranfer", Video_Path);
                edit.commit();
            } else {
                finish();
            }

        } else {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkoutPicAndVideIsExist(String sPath) {
        String path = sPath + "/Samson";
        File   file = new File(path);

        boolean hasPic   = false;
        boolean hasVideo = false;
        boolean hasWifi  = false;
        if (file != null & file.list().length != 0) {
            for (String fileName : file.list()) {
                Log.d(TAG, "checkoutPicAndVideIsExist: " + fileName);
                if (fileName.equals("Pictures")) {
                    hasPic = true;
                }
                if (fileName.equals("Videos")) {
                    hasVideo = true;
                }
                if (fileName.equals("Wifi-Tranfer")) {
                    hasWifi = true;
                }
            }
        }
        if (!hasPic) {
            //如果没有pic这个文件夹 创建
            File picsFile = new File(path + "/Pictures");
            picsFile.mkdirs();
        }
        if (!hasVideo) {
            //如果没有pic这个文件夹 创建
            File videosFile = new File(path + "/Videos");
            videosFile.mkdirs();
        }
        if (!hasWifi) {
            File videosFile = new File(path + "/Wifi-Tranfer");
            videosFile.mkdirs();
        }

    }

    private boolean checkoutVrexMeidaIsExist(String PVRootPath) {
        File files = new File(PVRootPath);

        if (files != null && files.list().length != 0) {
            for (String file : files.list()) {
                if (file.equals("Samson")) {
                    //存在这个文件夹
                    return true;
                }
            }
        }
        //如果到这里就是不存在这个文件夹了
        return false;
    }


    private String getPicAndVideoPath() {
        //看有没有 外部卡
        if (Environment.getExternalStorageDirectory()
                       .exists())
        {
            //如果有外部的卡
            return Environment.getExternalStorageDirectory()
                              .getAbsolutePath();
        } else {
            //没有
            return Environment.getDataDirectory()
                              .getAbsolutePath();
        }


    }

    private void initEvent() {
        mTv_pic.setOnClickListener(this);
        mTv_video.setOnClickListener(this);
        mTv_pic_right.setOnClickListener(this);
        mTv_video_right.setOnClickListener(this);
        mBtn_left_wifi.setOnClickListener(this);
        mBtn_right_wifi.setOnClickListener(this);
    }

    private void initView() {
        Log.d(TAG, "initView: ");
        mTv_pic = (Button) findViewById(R.id.new_pic_video_state_pic);
        mTv_video = (Button) findViewById(R.id.new_pic_video_state_video);
        mTv_pic.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mTv_pic.requestFocus();
        //获取到对称面的信息
        mll_left = (LinearLayout) findViewById(R.id.pic_video_ll_fenge);
        mLl_right = (LinearLayout) findViewById(R.id.pic_video_ll_fenge2);
        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mIv_right = (ImageView) findViewById(R.id.iv_right);

        mTv_pic_right = (Button) findViewById(R.id.new_pic_video_state_pic2);
        mTv_video_right = (Button) findViewById(R.id.new_pic_video_state_video2);

        mBtn_left_wifi = (Button) findViewById(R.id.wifi_p2p_left);
        mBtn_right_wifi = (Button) findViewById(R.id.wifi_p2p_right);

        mBtn_left_wifi.setSelected(false);
        mBtn_right_wifi.setSelected(false);

        //所有的控件都已经找到了   先判断  看横屏还是竖屏
        setViewFromOrientation();
        chooseButtonFoucs(true);

        Glide.with(PicAndVidioActivity.this)
             .load(pic_video_bg[pic_video_bg_num])
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_left);
        Glide.with(PicAndVidioActivity.this)
             .load(pic_video_bg[pic_video_bg_num])
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_right);
    }

    private void setViewFromOrientation() {
        Configuration newConfig = this.getResources()
                                      .getConfiguration();
        int ori = newConfig.orientation; //获取屏幕方向
        if (ori == newConfig.ORIENTATION_LANDSCAPE) {
            //如果是横屏
            mLl_right.setVisibility(View.VISIBLE);
            mIv_right.setVisibility(View.VISIBLE);

        } else if (ori == newConfig.ORIENTATION_PORTRAIT) {

            mLl_right.setVisibility(View.GONE);
            mIv_right.setVisibility(View.GONE);

        }


    }

    private boolean ifFirstClick = true;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:

                chooseButtonFoucs(true);

                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:

                chooseButtonFoucs(false);

                return true;
            case KeyEvent.KEYCODE_BUTTON_A:
                Log.d(TAG, "onKeyDown: " + mTv_pic.isSelected() + "::" + mTv_video.isSelected());
                if (ifFirstClick) {

                    if (mTv_pic.isSelected()) {
                        mTv_pic.performClick();
                    } else if (mTv_video.isSelected()) {
                        mTv_video.performClick();
                    }
                }
                return true;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void chooseButtonFoucs(boolean isPic) {
        if (isPic) {
            //            mTv_pic.setBackgroundDrawable(mDrawable);
            //            mTv_video.setBackgroundColor(Color.parseColor("#50000000"));
            //            //左右保持同步
            //            mTv_pic_right.setBackgroundDrawable(mDrawable);
            //            ;
            //            mTv_video_right.setBackgroundColor(Color.parseColor("#50000000"));
            //
            //            mTv_pic.requestFocus();
            mTv_pic.setTextColor(Color.parseColor("#ff0000"));
            mTv_pic_right.setTextColor(Color.parseColor("#ff0000"));
            mTv_video.setTextColor(Color.parseColor("#000000"));
            mTv_video_right.setTextColor(Color.parseColor("#000000"));
            mTv_pic.requestFocus();
            mTv_pic.setSelected(true);
            mTv_video.setSelected(false);
        } else {

            //            mTv_pic.setBackgroundColor(Color.parseColor("#50000000"));
            //            mTv_video.setBackgroundDrawable(mDrawable);
            //            ;
            //            //左右保持同步
            //            mTv_pic_right.setBackgroundColor(Color.parseColor("#50000000"));
            //            mTv_video_right.setBackgroundDrawable(mDrawable);
            //            ;
            //            mTv_video.requestFocus();
            mTv_video.setTextColor(Color.parseColor("#ff0000"));
            mTv_video_right.setTextColor(Color.parseColor("#ff0000"));
            mTv_pic.setTextColor(Color.parseColor("#000000"));
            mTv_pic_right.setTextColor(Color.parseColor("#000000"));
            mTv_video.requestFocus();
            mTv_video.setSelected(true);

            mTv_pic.setSelected(false);
        }


    }

    @Override
    protected void onResume() {
        if (mTv_pic.getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
            mTv_pic.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        // TODO: 2017/3/23 跳转之后要结束handle

        switch (v.getId()) {
            case R.id.new_pic_video_state_pic:
                Intent picIntent = new Intent(this, FilesActivity.class);
                picIntent.putExtra("video_pic", "pic");
                picIntent.putExtra("path", getPicAndVideoPath() + "/Samson");
                Log.d(TAG, "onClick: Pic_Path" + Pic_Path);
                startActivity(picIntent);

                break;
            case R.id.new_pic_video_state_pic2:
                Intent picIntent2 = new Intent(this, FilesActivity.class);
                picIntent2.putExtra("video_pic", "pic");
                picIntent2.putExtra("path", getPicAndVideoPath() + "/Samson");
                Log.d(TAG, "onClick: Pic_Path" + Pic_Path);
                startActivity(picIntent2);
                break;
            case R.id.new_pic_video_state_video:
                //                Intent videoIntent = new Intent(this, MainActivity.class);
                //                videoIntent.putExtra("path", Video_Path);
                //
                //                startActivity(videoIntent);
                Intent videoIntent = new Intent(this, FilesActivity.class);
                videoIntent.putExtra("video_pic", "video");
                videoIntent.putExtra("path", getPicAndVideoPath() + "/Samson");
                Log.d(TAG, "onClick: Pic_Path" + Pic_Path);
                startActivity(videoIntent);
                break;
            case R.id.new_pic_video_state_video2:
                //                Intent videoIntent2 = new Intent(this, MainActivity.class);
                //                videoIntent2.putExtra("path", Video_Path);
                //
                //                startActivity(videoIntent2);
                Intent videoIntent2 = new Intent(this, FilesActivity.class);
                videoIntent2.putExtra("video_pic", "video");
                videoIntent2.putExtra("path", getPicAndVideoPath() + "/Samson");
                Log.d(TAG, "onClick: Pic_Path" + Pic_Path);
                startActivity(videoIntent2);
                break;
            case R.id.wifi_p2p_left:
                                Intent wifip2pIntent = new Intent(this, WifiP2pRecActivity.class);

                                startActivity(wifip2pIntent);
              //  setTestBG();
                break;
            case R.id.wifi_p2p_right:
                Intent wifip2pIntent2 = new Intent(this, WifiP2pRecActivity.class);

                startActivity(wifip2pIntent2);
             //   setTestBG();

                break;
        }
    }

    private void setTestBG() {
        if (pic_video_bg_num == pic_video_bg.length - 1) {
            pic_video_bg_num = 0;
        } else {
            pic_video_bg_num++;
        }
        Glide.with(PicAndVidioActivity.this)
             .load(pic_video_bg[pic_video_bg_num])
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_left);
        Glide.with(PicAndVidioActivity.this)
             .load(pic_video_bg[pic_video_bg_num])
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_right);

        Bitmap   bitmap   = BitmapFactory.decodeResource(getResources(),
                                                         pic_video_button_bg[pic_video_bg_num]);
        Drawable drawable = new BitmapDrawable(bitmap);
        mBtn_left_wifi.setBackground(drawable);
        mBtn_right_wifi.setBackground(drawable);
        mTv_pic.setBackground(drawable);
        mTv_pic_right.setBackground(drawable);
        mTv_video.setBackground(drawable);
        mTv_video_right.setBackground(drawable);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setViewFromOrientation();


        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mHandler.hasMessages(SWITCH_PIC)) {
            mHandler.removeMessages(SWITCH_PIC);
        }
        super.onBackPressed();
    }

}
