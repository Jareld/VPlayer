package com.example.lyc.vrexplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.Utils.SelectedPosition;
import com.example.lyc.vrexplayer.view.ObservableScrollView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class FilesActivity
        extends AppCompatActivity
        implements ObservableScrollView.ScrollViewKeydownListener
{

    private static final String            TAG        = "Jareld _filesactivity";
    private              ArrayList<String> mArrayList = new ArrayList();
    private View                 mLayout;
    private ObservableScrollView mScrollView;
    private LinearLayout         mLl_container;
    private ArrayList<LinearLayout> mLl_Arr = new ArrayList<>();
    private int mVideo_Pic;
    private static final int    PIC            = 1;
    private static final int    VIDEO          = 2;
    private static final String PIC_SP         = "PIC_SP";
    private static final String VIDEO_SP       = "VIDEO_SP";
    private static final String PIC_SP_NUM_I   = "PIC_SP_NUM_I";
    private static final String VIDEO_SP_NUM_I = "VIDEO_SP_NUM_I";
    private static final String PIC_SP_NUM_J   = "PIC_SP_NUM_J";
    private static final String VIDEO_SP_NUM_J = "VIDEO_SP_NUM_J";
    private static final String PIC_SP_BJ      = "PIC_SP_BJ";
    private static final String VIDEO_SP_BJ    = "VIDEO_SP_BJ";

    private Context mContext;
    private SelectedPosition mSelectedPostion = new SelectedPosition();
    private ImageView mIv_left;
    private ImageView mIv_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().getDecorView()
                   .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_files, null);
        //mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(mLayout);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLayout.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE) {
                    mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                } else {
                    mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

            }
        });

        initView();
        initData();
        initEvent();
        Log.d(TAG, "onCreate: "+mArrayList.size());
        if(mArrayList.size()!=0){
        new Thread() {
            @Override
            public void run() {

                while (mLl_Arr.get(0)
                              .getMeasuredHeight() == 0) { ; }
                FilesActivity.this.onScrollViewKeydownChanged(KeyEvent.KEYCODE_DPAD_DOWN);
            }
        }.start();}
    }

    private void initView() {
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mLl_container = (LinearLayout) findViewById(R.id.ll_scrollview_container);
        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mIv_right = (ImageView) findViewById(R.id.iv_right);
        Glide.with(FilesActivity.this)
             .load(R.mipmap.pic_video_bg)
             .sizeMultiplier(0.5f)
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_left);
        Glide.with(FilesActivity.this)
             .load(R.mipmap.pic_video_bg)
             .sizeMultiplier(0.5f)
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_right);
    }


    private void initData() {
        mContext = getApplicationContext();
        //遍历这个samson文件夹
        Intent            intent    = getIntent();
        String            video_pic = intent.getStringExtra("video_pic");
        String            path      = intent.getStringExtra("path");
        File              file      = new File(path);
        File[]            files     = file.listFiles();
        ArrayList<String> beforeArr = new ArrayList<>();
        ArrayList<String> nowArr    = new ArrayList<>();
        if (video_pic.equals("pic")) {
            //说明是图片的
            addPicPath(files);
            mVideo_Pic = PIC;
            //遍历所有的文件 跟之前来做对比

            SharedPreferences sharedPreferences = getSharedPreferences(PIC_SP, MODE_PRIVATE);
            int               anInt             = sharedPreferences.getInt(PIC_SP_NUM_I, -1);
            if (anInt == -1) {
                //说明第一次进来  存数据就OK
                SharedPreferences.Editor edit11 = sharedPreferences.edit();
                edit11.putInt(PIC_SP_NUM_I, mArrayList.size());
                ArrayList<File> picList = new ArrayList<>();
                int             size    = 0;
                for (int i = 0; i < mArrayList.size(); i++) {
                    //获取每个目录下的pic的路径 存放
                    File   file1  = new File(mArrayList.get(i));
                    File[] files1 = file1.listFiles();

                    for (int j = 0; j < files1.length; j++) {

                        if (MediaFile.isPicFileType(files1[j].getAbsolutePath())) {
                            picList.add(files1[j]);
                        }
                    }
                    for (int j = 0; j < picList.size(); j++) {
                        edit11.putString(PIC_SP_BJ + "_" + i + "_" + j,
                                         picList.get(j)
                                                .getAbsolutePath());
                        Log.d(TAG, "initData: 存" + picList.get(j));
                    }
                    edit11.putInt(PIC_SP_NUM_J + "_" + i, picList.size());
                    picList.clear();

                }
                edit11.commit();
            } else {
                //说明不是第一次进来 就  先把之前的数据全部娶过来
                int size_i, size_j;
                size_i = anInt;

                for (int i = 0; i < size_i; i++) {
                    //获取每个目录下的pic的路径 存放
                    size_j = sharedPreferences.getInt(PIC_SP_NUM_J + "_" + i, -1);
                    for (int j = 0; j < size_j; j++) {
                        String aa = sharedPreferences.getString(PIC_SP_BJ + "_" + i + "_" + j,
                                                                "aa");
                        Log.d(TAG, "initData: 取" + aa);
                        beforeArr.add(aa);
                    }
                }
                for (int i = 0; i < mArrayList.size(); i++) {
                    //获取每个目录下的pic的路径 存放
                    File   file1  = new File(mArrayList.get(i));
                    File[] files1 = file1.listFiles();

                    for (int j = 0; j < files1.length; j++) {

                        if (MediaFile.isPicFileType(files1[j].getAbsolutePath())) {
                            nowArr.add(files1[j].getAbsolutePath());
                        }
                    }
                }
                Log.d(TAG, "initData: size" + beforeArr.size());
                Log.d(TAG, "initData: size" + nowArr.size());
                for (int x = 0; x < nowArr.size(); x++) {
                    Log.d(TAG, "initData: 取 现在的" + nowArr.get(x));
                    if (!beforeArr.contains(nowArr.get(x))) {
                        Log.d(TAG, "initData: size 新增 " + nowArr.get(x) + "x= " + x);
                        File file1 = new File(nowArr.get(x));
                        file1.setLastModified(System.currentTimeMillis());
                        Log.d(TAG, "initData: time "+file1.lastModified());
                    }
                }
                //在添加一次
                SharedPreferences.Editor edit11 = sharedPreferences.edit();
                edit11.putInt(PIC_SP_NUM_I, mArrayList.size());
                ArrayList<File> picList = new ArrayList<>();
                int             size    = 0;
                for (int i = 0; i < mArrayList.size(); i++) {
                    //获取每个目录下的pic的路径 存放
                    File   file1  = new File(mArrayList.get(i));
                    File[] files1 = file1.listFiles();

                    for (int j = 0; j < files1.length; j++) {

                        if (MediaFile.isPicFileType(files1[j].getAbsolutePath())) {
                            picList.add(files1[j]);
                        }
                    }
                    for (int j = 0; j < picList.size(); j++) {
                        edit11.putString(PIC_SP_BJ + "_" + i + "_" + j,
                                         picList.get(j)
                                                .getAbsolutePath());
                        Log.d(TAG, "initData: 存" + picList.get(j));
                    }
                    edit11.putInt(PIC_SP_NUM_J + "_" + i, picList.size());
                    picList.clear();

                }
                edit11.commit();

            }


        } else if (video_pic.equals("video")) {
            Log.d(TAG, "initData: video");


            //说明是视频的
            addVideoPath(files);
            mVideo_Pic = VIDEO;
            SharedPreferences sharedPreferences = getSharedPreferences(VIDEO_SP, MODE_PRIVATE);
            int               anInt             = sharedPreferences.getInt(VIDEO_SP_NUM_I, -1);
            if (anInt == -1) {
                //说明第一次进来  存数据就OK
                SharedPreferences.Editor edit11 = sharedPreferences.edit();
                edit11.putInt(VIDEO_SP_NUM_I, mArrayList.size());
                ArrayList<File> picList = new ArrayList<>();
                int             size    = 0;
                for (int i = 0; i < mArrayList.size(); i++) {
                    //获取每个目录下的pic的路径 存放
                    File   file1  = new File(mArrayList.get(i));
                    File[] files1 = file1.listFiles();

                    for (int j = 0; j < files1.length; j++) {

                        if (MediaFile.isVideoFileType(files1[j].getAbsolutePath())) {
                            picList.add(files1[j]);
                        }
                    }
                    for (int j = 0; j < picList.size(); j++) {
                        edit11.putString(VIDEO_SP_BJ + "_" + i + "_" + j,
                                         picList.get(j)
                                                .getAbsolutePath());
                        Log.d(TAG, "initData: 存" + picList.get(j));
                    }
                    edit11.putInt(VIDEO_SP_NUM_J + "_" + i, picList.size());
                    picList.clear();

                }
                edit11.commit();
            } else {
                //说明不是第一次进来 就  先把之前的数据全部娶过来
                int size_i, size_j;
                size_i = anInt;

                for (int i = 0; i < size_i; i++) {
                    //获取每个目录下的pic的路径 存放
                    size_j = sharedPreferences.getInt(VIDEO_SP_NUM_J + "_" + i, -1);
                    for (int j = 0; j < size_j; j++) {
                        String aa = sharedPreferences.getString(VIDEO_SP_BJ + "_" + i + "_" + j,
                                                                "aa");
                        Log.d(TAG, "initData: 取" + aa);
                        beforeArr.add(aa);
                    }
                }
                for (int i = 0; i < mArrayList.size(); i++) {
                    //获取每个目录下的pic的路径 存放
                    File   file1  = new File(mArrayList.get(i));
                    File[] files1 = file1.listFiles();

                    for (int j = 0; j < files1.length; j++) {

                        if (MediaFile.isVideoFileType(files1[j].getAbsolutePath())) {
                            nowArr.add(files1[j].getAbsolutePath());
                        }
                    }
                }
                Log.d(TAG, "initData: size" + beforeArr.size());
                Log.d(TAG, "initData: size" + nowArr.size());
                for (int x = 0; x < nowArr.size(); x++) {
                    Log.d(TAG, "initData: 取 现在的" + nowArr.get(x));
                    if (!beforeArr.contains(nowArr.get(x))) {
                        Log.d(TAG, "initData: size 新增 " + nowArr.get(x) + "x= " + x);
                        File file1 = new File(nowArr.get(x));
                        file1.setLastModified(System.currentTimeMillis());
                        Log.d(TAG, "initData: time"+ getCurrentTime(System.nanoTime())+"::"+getCurrentTime(System.currentTimeMillis())+"::"+getCurrentTime(file1.lastModified()));
                    }
                }
                //在添加一次
                SharedPreferences.Editor edit11 = sharedPreferences.edit();
                edit11.putInt(VIDEO_SP_NUM_I, mArrayList.size());
                ArrayList<File> picList = new ArrayList<>();
                int             size    = 0;
                for (int i = 0; i < mArrayList.size(); i++) {
                    //获取每个目录下的pic的路径 存放
                    File   file1  = new File(mArrayList.get(i));
                    File[] files1 = file1.listFiles();

                    for (int j = 0; j < files1.length; j++) {

                        if (MediaFile.isVideoFileType(files1[j].getAbsolutePath())) {
                            picList.add(files1[j]);
                        }
                    }
                    for (int j = 0; j < picList.size(); j++) {
                        edit11.putString(VIDEO_SP_BJ + "_" + i + "_" + j,
                                         picList.get(j)
                                                .getAbsolutePath());
                        Log.d(TAG, "initData: 存" + picList.get(j));
                    }
                    edit11.putInt(VIDEO_SP_NUM_J + "_" + i, picList.size());
                    picList.clear();

                }
                edit11.commit();


            }
        }
        //重新排序一下

        Log.d(TAG, "initData: 之前的list" + mArrayList);
        Collections.sort(mArrayList, new FileComparator());//通过重写Comparator的实现类
        Log.d(TAG, "initData: 之后的list" + mArrayList);


        //加载数据
        initContainer();
    }

    private class FileComparator
            implements Comparator<String>
    {

        @Override
        public int compare(String s, String t1) {
            if (MediaFile.getLastFileTime(s) > MediaFile.getLastFileTime(t1)) {
                Log.d(TAG, "compare: s:" + getCurrentTime(MediaFile.getLastFileTime(s)));
                Log.d(TAG, "compare: t1:" + getCurrentTime(MediaFile.getLastFileTime(t1)));
                File fi = new File(s);

                return -1;
            } else {
                return 1;
            }
        }
    }

    public String getCurrentTime(long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
        String           str    = format.format(new Date(date));
        return str;
    }

    private void initContainer() {
        mLl_container.removeAllViews();
        mLl_Arr.clear();
        for (int i = 0; i < mArrayList.size(); i++) {
            LinearLayout inflate = (LinearLayout) View.inflate(mContext,
                                                               R.layout.custom_second_files_view,
                                                               null);
            final int final_i = i;
            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(final_i == mSelectedPostion.getPosition_i()){
                        Log.d(TAG, "onClick: 说明点击 的  和 选中的一样");

                        if (mVideo_Pic == PIC) {
                            Intent intent = new Intent(mContext, PicAcitivity.class);
                            intent.putStringArrayListExtra("Pics", mArrayList);
                            intent.putExtra("position", final_i);
                            startActivity(intent);

                        } else if (mVideo_Pic == VIDEO) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.putStringArrayListExtra("Videos", mArrayList);
                            intent.putExtra("position", final_i);
                            startActivity(intent);
                        }
                    }else{
                        //说明点击的 和选中的不一样
                        mSelectedPostion.setBefore_position_i(mSelectedPostion.getPosition_i());
                        mSelectedPostion.setPosition_i(final_i);

                        moveFocusMediaClick();
                    }


                }
            });
            ImageView iv_1 = (ImageView) inflate.findViewById(R.id.iv_left_the_one);
            ImageView iv_2 = (ImageView) inflate.findViewById(R.id.iv_left_the_two);
            ImageView iv_3 = (ImageView) inflate.findViewById(R.id.iv_right_the_one);
            ImageView iv_4 = (ImageView) inflate.findViewById(R.id.iv_right_the_two);
            TextView  tv_1 = (TextView) inflate.findViewById(R.id.tv_left_files_name);
            TextView  tv_2 = (TextView) inflate.findViewById(R.id.tv_left_file_num);
            TextView  tv_3 = (TextView) inflate.findViewById(R.id.tv_right_files_name);
            TextView  tv_4 = (TextView) inflate.findViewById(R.id.tv_right_file_num);
            File      file = new File(mArrayList.get(i));
            Log.d(TAG, "initContainer: " + mArrayList.get(i));
            ArrayList oneOrTwoPath = getOneOrTwoPath(mArrayList.get(i));
            //文件夹名称
            tv_1.setText(file.getName());
            tv_3.setText(file.getName());
            //项目个数

            int length = getMediaNum(mArrayList.get(i));
            tv_2.setText(length + "个项目");
            tv_4.setText(length + "个项目");
            Glide.with(mContext)
                 .load(oneOrTwoPath.get(0))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.android_btn)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);

            Glide.with(mContext)
                 .load(oneOrTwoPath.get(0))
                 .centerCrop()
                 .sizeMultiplier(0.5f)
                 .placeholder(R.mipmap.android_btn)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_3);
            if (oneOrTwoPath.size() == 2) {
                Glide.with(mContext)
                     .load(oneOrTwoPath.get(1))
                     .centerCrop()
                     .sizeMultiplier(0.5f)
                     .placeholder(R.mipmap.android_btn)

                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_2);
                Glide.with(mContext)
                     .load(oneOrTwoPath.get(1))
                     .centerCrop()
                     .sizeMultiplier(0.5f)
                     .placeholder(R.mipmap.android_btn)
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_4);
            } else {
                Glide.with(mContext)
                     .load(R.mipmap.android_btn)
                     .centerCrop()
                     .sizeMultiplier(0.5f)

                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_2);
                Glide.with(mContext)
                     .load(R.mipmap.android_btn)
                     .centerCrop()
                     .sizeMultiplier(0.5f)
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_4);


            }
            mLl_Arr.add(inflate);
            mLl_container.addView(inflate);
        }

    }

    private void addPicPath(File[] files) {

        for (File file : files) {
            if (file.isDirectory()) {
                addPicPath(file.listFiles());
            } else {
                if (MediaFile.isPicFileType(file.getAbsolutePath())) {
                    //说明这个文件夹里面有pic
                    String absoluteFile = file.getAbsolutePath();
                    int    i            = absoluteFile.lastIndexOf("/");
                    String substring    = absoluteFile.substring(0, i);
                    if (mArrayList.contains(substring)) {
                        Log.d(TAG, "addVideoPath: 已经包含了");
                    } else {
                        mArrayList.add(substring);
                        Log.d(TAG, "addVideoPath: 没有包含");
                    }
                }
            }
        }

    }

    private void addVideoPath(File[] files) {

        for (File file : files) {
            Log.d(TAG, "addVideoPath: 判断之前" + file.getAbsolutePath());
            if (file.isDirectory()) {
                Log.d(TAG, "addVideoPath: 是文件");
                addVideoPath(file.listFiles());
            } else {
                if (MediaFile.isVideoFileType(file.getAbsolutePath())) {
                    //说明这个文件夹里面有video
                    Log.d(TAG, "addVideoPath: 有video" + file.getAbsolutePath());
                    String absoluteFile = file.getAbsolutePath();
                    int    i            = absoluteFile.lastIndexOf("/");
                    String substring    = absoluteFile.substring(0, i);
                    if (mArrayList.contains(substring)) {
                        Log.d(TAG, "addVideoPath: 已经包含了");
                    } else {
                        mArrayList.add(substring);
                    }
                }
            }

        }

    }

    private void initEvent() {
        mScrollView.setOnScrollViewKeydownListener(this);
    }

    @Override
    public void onScrollViewKeydownChanged(int keyCode) {


        if (mSelectedPostion.getIsLongClick()) {
            Toast.makeText(mContext, "请先按Back键退出WIFI文件传输模式", Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        if (mLl_Arr == null || mLl_Arr.get(0) == null) {
            return;
        }
        int llItemHeight = mLl_Arr.get(0)
                                  .getMeasuredHeight();
        int scrollViewHegiht = 0;
        if (mScrollView != null) {
            scrollViewHegiht = mScrollView.getMeasuredHeight();
        }
        int remainder = scrollViewHegiht % llItemHeight;
        //看一共有多少行
        int line_num = mArrayList.size();

        int position_i;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                position_i = mSelectedPostion.getPosition_i();
                Log.d(TAG, "onScrollViewKeydownChanged: 向上" + position_i);
                if (position_i == 0) {
                    //已经到了 00 坐标
                    break;
                } else {
                    position_i--;
                }
                mSelectedPostion.setPosition_i(position_i);
                if ((position_i * llItemHeight) > mScrollView.getScrollY()) {

                    //说明在范围内
                    Log.d(TAG, "onScrollViewKeydownChanged: 说明在一个范围内");

                } else if ((position_i * llItemHeight) <= mScrollView.getScrollY()) {

                    //那就把这个移动在最底下
                    Log.d(TAG, "onScrollViewKeydownChanged: 说明要一定了");
                    int move_height = scrollViewHegiht - (llItemHeight - (mScrollView.getScrollY() - (position_i * llItemHeight)));
                    mScrollView.smoothScrollBy(0, -move_height);

                }
                moveFocusMedia("move_i", position_i, false);

                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                position_i = mSelectedPostion.getPosition_i();

                if (position_i == 0 && mSelectedPostion.getIsFirst()) {
                    mSelectedPostion.setIsFirst(false);
                } else if (position_i >= line_num - 1) {
                    Log.d(TAG, "onScrollViewKeydownChanged: position_i" + position_i);
                    //最底下 那里了 直接滑到最下面

                    break;
                } else {
                    position_i++;
                }


                if ((position_i * llItemHeight) >= mScrollView.getScrollY() && ((position_i + 1) * llItemHeight) <= (mScrollView.getScrollY() + scrollViewHegiht)) {
                    //在里面 并不用去 跳转  只需要把框框加一下


                } else if (((position_i + 1) * llItemHeight) >= (mScrollView.getScrollY() + scrollViewHegiht)) {
                    //这里就说明要重新移动了

                    mScrollView.smoothScrollBy(0,
                                               (position_i * llItemHeight) - mScrollView.getScrollY());

                }

                mSelectedPostion.setPosition_i(position_i);
                moveFocusMedia("move_i", position_i, true);
                Log.d(TAG, "onScrollViewKeydownChanged: position_i" + position_i);

                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:


                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:


                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG,
                      "onScrollViewKeydownChanged: " + mArrayList.get(mSelectedPostion.getPosition_i()));

                if (mVideo_Pic == PIC) {
                    Intent intent = new Intent(mContext, PicAcitivity.class);
                    intent.putStringArrayListExtra("Pics", mArrayList);
                    intent.putExtra("position", mSelectedPostion.getPosition_i());
                    startActivity(intent);

                } else if (mVideo_Pic == VIDEO) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putStringArrayListExtra("Videos", mArrayList);
                    intent.putExtra("position", mSelectedPostion.getPosition_i());
                    startActivity(intent);


                }

                //                Intent intent = new Intent(MainActivity.mContext, VrPlayerActivity.class);
                //                intent.putStringArrayListExtra("Videos", mArrayList);
                //                intent.putExtra("position",
                //                                2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
                //                startActivity(intent);
                break;
        }
    }


    private ArrayList getOneOrTwoPath(String path) {
        ArrayList<String> arrayList = new ArrayList<>();

        File   file  = new File(path);
        File[] files = file.listFiles();
        if (mVideo_Pic == PIC) {
            for (File f : files) {
                if (MediaFile.isPicFileType(f.getAbsolutePath())) {
                    arrayList.add(f.getAbsolutePath());
                }
                if (arrayList.size() >= 2) {
                    return arrayList;
                }
            }
        } else if (mVideo_Pic == VIDEO) {
            for (File f : files) {
                if (MediaFile.isVideoFileType(f.getAbsolutePath())) {
                    arrayList.add(f.getAbsolutePath());
                }
                if (arrayList.size() >= 2) {
                    return arrayList;
                }
            }

        }


        return arrayList;
    }

    private int getMediaNum(String path) {
        int    mediaNum = 0;
        File   file     = new File(path);
        File[] files    = file.listFiles();
        if (mVideo_Pic == PIC) {
            for (File f : files) {
                if (MediaFile.isPicFileType(f.getAbsolutePath())) {
                    mediaNum++;
                }

            }
        } else if (mVideo_Pic == VIDEO) {
            for (File f : files) {
                if (MediaFile.isVideoFileType(f.getAbsolutePath())) {
                    mediaNum++;
                }

            }

        }


        return mediaNum;
    }



    private void moveFocusMedia(String str, int position_i, boolean isDownRight) {
        if (str.equals("move_i")) {

            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j(),
                               Color.parseColor("#ffffff00"));
            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j() + 1,
                               Color.parseColor("#ffffff00"));

            if (position_i != 0) {
                //下
                if (isDownRight) {

                    moveSetImageViewBG(mSelectedPostion.getPosition_i() - 1,
                                       mSelectedPostion.getPosition_j(),
                                       Color.TRANSPARENT);
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() - 1,
                                       mSelectedPostion.getPosition_j() + 1,
                                       Color.TRANSPARENT);

                } else {
                    //上

                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j(),
                                       Color.TRANSPARENT);
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j() + 1,
                                       Color.TRANSPARENT);


                }
            } else if (position_i == 0) {
                //position_i == 0的时候  那么 up的时候 要消失掉之前的。因为之前有一个postion_i!=0的判断
                if (!isDownRight) {
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j(),
                                       Color.TRANSPARENT);
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j() + 1,
                                       Color.TRANSPARENT);

                }
            }
        } else if (str.equals("move_j")) {

            //移动j方向
            if (isDownRight) {
                //右边移动一下
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   Color.parseColor("#ffffff00"));
                //                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                //                                   mSelectedPostion.getPosition_j() + 2,
                //                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() - 1,
                                   Color.TRANSPARENT);
                //                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                //                                   mSelectedPostion.getPosition_j() + 2 - 1,
                //                                   Color.TRANSPARENT);

            } else {
                //左边移动一下
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   Color.parseColor("#ffffff00"));
                //                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                //                                   mSelectedPostion.getPosition_j() + 2,
                //                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 1,
                                   Color.TRANSPARENT);
                //                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                //                                   mSelectedPostion.getPosition_j() + 3,
                //                                   Color.TRANSPARENT);

            }

        }
    }
    private void moveFocusMediaClick( ) {

        moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                           mSelectedPostion.getPosition_j(),
                           Color.parseColor("#ffffff00"));
        moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                           mSelectedPostion.getPosition_j() + 1,
                           Color.parseColor("#ffffff00"));

        moveSetImageViewBG(mSelectedPostion.getBefore_position_i(),
                           mSelectedPostion.getPosition_j(),
                           Color.TRANSPARENT);
        moveSetImageViewBG(mSelectedPostion.getBefore_position_i(),
                           mSelectedPostion.getPosition_j() + 1,
                           Color.TRANSPARENT);





    }

    private void moveSetImageViewBG(int position_i, int position_j, int parseColor) {
        Log.d(TAG,
              "moveSetImageViewBG: " + mLl_Arr.size() + position_i + "::" + position_j + "::" + mLl_Arr.get(
                      position_i)
                                                                                                       .getChildAt(
                                                                                                               position_j));
        LinearLayout iv = (LinearLayout) mLl_Arr.get(position_i)
                                                .getChildAt(position_j);
        iv.setBackgroundColor(parseColor);

    }


}
