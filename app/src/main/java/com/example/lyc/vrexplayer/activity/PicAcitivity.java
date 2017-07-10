package com.example.lyc.vrexplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.Utils.LogUtils;
import com.example.lyc.vrexplayer.Utils.RxBus;
import com.example.lyc.vrexplayer.Utils.SelectedPosition;
import com.example.lyc.vrexplayer.Utils.UserEvent;
import com.example.lyc.vrexplayer.view.ObservableScrollView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PicAcitivity
        extends AppCompatActivity
        implements ObservableScrollView.ScrollViewKeydownListener, View.OnClickListener
{
    public static final  int    ORIENTATION_PROTRAIT  = 1;
    public static final  int    ORIENTATION_LANDSCAPE = 2;
    private static final String TAG                   = "PicAcitivity";
    private ObservableScrollView mObservableScrollview;
    private LinearLayout         mLlContainer;
    private int                  mInitOrientation;
    public SelectedPosition mSelectedPostion = new SelectedPosition();
    private Context           mContext;
    private ArrayList<String> mPics;
    private ArrayList<String> mPicsName;
    private ArrayList<LinearLayout> mLl_Arr = new ArrayList<>();
    private View      mLayout;
    private ImageView mIv_left;
    private ImageView mIv_right;
    private static final int CLICK_STATE_LONG_CLICK = 3;
    private static final int CLICK_STATE_SHORT_CLICK =4;
    private int click_state = CLICK_STATE_SHORT_CLICK;
    private SelectedPosition mLongClickBeforeSlection = new SelectedPosition();
    private ArrayList<int []> hasLongSelected = new ArrayList<>();
    private Button mBtn_cancle_del;
    private boolean isResetLongClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_pic_acitivity, null);
        mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(mLayout);
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        initView();
        initData();
        initEvent();
        ListView listView;

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
        new Thread() {
            @Override
            public void run() {

                while (mLl_Arr.get(0)
                              .getMeasuredHeight() == 0) { ; }
                PicAcitivity.this.onScrollViewKeydownChanged(KeyEvent.KEYCODE_DPAD_DOWN);
            }
        }.start();
    }

    private void initView() {
        mObservableScrollview = (ObservableScrollView) findViewById(R.id.scroll_view_pic);
        mLlContainer = (LinearLayout) findViewById(R.id.ll_scrollview_container_pic);
        mBtn_cancle_del = (Button) findViewById(R.id.btn_cancle_del);
        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mIv_right = (ImageView) findViewById(R.id.iv_right);
    }

    private void initData() {

        Glide.with(PicAcitivity.this)
             .load(R.mipmap.pic_video_bg)
             .sizeMultiplier(0.5f)
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_left);
        Glide.with(PicAcitivity.this)
             .load(R.mipmap.pic_video_bg)
             .sizeMultiplier(0.5f)
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_right);

        mContext = getApplicationContext();
        Configuration configuration = getResources().getConfiguration();
        mInitOrientation = configuration.orientation;

        mPics = new ArrayList<>();
        mPicsName = new ArrayList<>();
        Intent            intent   = getIntent();
        ArrayList<String> videoses = intent.getStringArrayListExtra("Pics");
        int               position = intent.getIntExtra("position", -1);
        Log.d(TAG, "initData: " + videoses + "::" + position);

        String          videose   = videoses.get(position);
        File            file      = new File(videose);
        File[]          files     = file.listFiles();
        ArrayList<File> arrayList = new ArrayList<>();
        for (File f : files) {
            if (MediaFile.isPicFileType(f.getAbsolutePath())) {
                //                mPic.add(f.getAbsolutePath());
                //                mPicName.add(f.getName());

                arrayList.add(f);
            }
        }

        Collections.sort(arrayList, new FileComparator());//通过重写Comparator的实现类
        FileCreatedDemo fileCreatedDemo = new FileCreatedDemo();

        for (int i = 0; i < arrayList.size(); i++) {
            mPics.add(arrayList.get(i)
                               .getAbsolutePath());
            mPicsName.add(arrayList.get(i)
                                   .getName());
        }



        mInitOrientation = getResources().getConfiguration().orientation;

        if (mInitOrientation == ORIENTATION_PROTRAIT) {
            //加载一个
            Log.d(TAG, "initView: 加载一个");
            setProtraitOrientation();
        } else if (mInitOrientation == ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "initView: 加载两个");
            setLandscapeOrientation();
        }

        mObservableScrollview.setOnScrollViewKeydownListener(this);
        mObservableScrollview.setOrientation(mInitOrientation);

        if (!mSelectedPostion.getIsFirst() && !isResetLongClick) {
            //那么久要进行某一个 方框的xuanze
            if (mInitOrientation == ORIENTATION_LANDSCAPE ) {
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 2,
                                   Color.parseColor("#ffffff00"));
            }
        }


        Configuration configuration1 = getResources().getConfiguration();
        mInitOrientation = configuration1.orientation;
        mSelectedPostion.setPosition_i(0);
        mSelectedPostion.setPosition_j(0);
    }

    private void initEvent() {
mBtn_cancle_del.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {

        if(click_state == CLICK_STATE_LONG_CLICK){
            isResetLongClick = true;


            //无论怎么样都要重置
            //重新更新数据
            initData();
            //重置一些状态


            //被选中的长吉 要清空
            hasLongSelected.clear();
            //不管是不是第一次点击要清空
            mIsSelctedClick = false;
            //点击的状态要清空
            click_state = CLICK_STATE_SHORT_CLICK;

            mBtn_cancle_del.setVisibility(View.GONE);

            mSelectedPostion.setLongClick(false);

            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j(),
                               Color.parseColor("#ffffff00"));
            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j() + 2,
                               Color.parseColor("#ffffff00"));

            isResetLongClick = false;

return;
        }

        super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle_del:




                isResetLongClick = true;

                if(hasLongSelected!=null && hasLongSelected.size()!=0){
                    //进行删除工作
                    for(int i = 0 ; i < hasLongSelected.size() ; i ++){

                        Log.d(TAG, "onClick: "+hasLongSelected.get(i)[0] + "-"+hasLongSelected.get(i)[1] +"::"+ mPics.get(2 * hasLongSelected.get(i)[0] + hasLongSelected.get(i)[1]-1));

                        File file = new File(mPics.get(2 * hasLongSelected.get(i)[0] + hasLongSelected.get(i)[1]-1));
                        if(file!=null && file.exists()){
                            file.delete();

                        }
                    }

                }else{
                    //进行取消工作


                }
        //无论怎么样都要重置
                //重新更新数据
                initData();
                //重置一些状态
                if(mPics.size() == 0){
                    Log.d(TAG, "initData:111 "+mPics.size());
                   FilesActivity.isFilesPicFinished = true;
                    finish();

                }else {

                    //被选中的长吉 要清空
                    hasLongSelected.clear();
                    //不管是不是第一次点击要清空
                    mIsSelctedClick = false;
                    //点击的状态要清空
                    click_state = CLICK_STATE_SHORT_CLICK;

                    mBtn_cancle_del.setVisibility(View.GONE);

                    mSelectedPostion.setLongClick(false);

                    moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                       mSelectedPostion.getPosition_j(),
                                       Color.parseColor("#ffffff00"));
                    moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                       mSelectedPostion.getPosition_j() + 2,
                                       Color.parseColor("#ffffff00"));

                    isResetLongClick = false;
                }
                break;
            default:
                break;
        }


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
    private class FileComparator
            implements Comparator<File>
    {
        @Override
        public int compare(File file, File t1) {
            if (file.lastModified() > t1.lastModified()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private void setProtraitOrientation() {
        RxBus.getInstance()
             .post(new UserEvent(ORIENTATION_PROTRAIT, "orientation"));

        mLlContainer.removeAllViews();
        mLl_Arr.clear();

        for (int i = 0; i < mPics.size() / 2; i++) {
            LinearLayout inflate = (LinearLayout) View.inflate(mContext,
                                                               R.layout.custum_scroll_item_view_single,
                                                               null);
            mLl_Arr.add(inflate);

            ImageView iv_1 = (ImageView) inflate.findViewById(R.id.item_scroll_view_single_1);
            ImageView iv_2 = (ImageView) inflate.findViewById(R.id.item_scroll_view_single_2);

            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_1)).setText(mPicsName.get(
                    2 * i));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_2)).setText(mPicsName.get(
                    2 * i + 1));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_1)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_2)).setSelected(true);

            final int final_i = i;
            Glide.with(mContext)
                 .load(mPics.get(2 * i))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);
            Glide.with(mContext)
                 .load(mPics.get(2 * i + 1))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_2);

            iv_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclSingle(final_i, 1);

                }
            });
            iv_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclSingle(final_i, 2);

                }
            });
            iv_1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclSingle(final_i, 1);
                    return true;
                }
            });
            iv_2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclSingle(final_i, 2);
                    return true;

                }
            });
            mLlContainer.addView(inflate);
            if (mPics.size() % 2 != 0 && i == mPics.size() / 2 - 1) {

                //说明 单了一个 ， 要最后添加一个
                LinearLayout inflate1 = (LinearLayout) View.inflate(mContext,
                                                                    R.layout.custum_scroll_item_view_single,
                                                                    null);
                mLl_Arr.add(inflate1);
                ImageView iv_double_1 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_single_1);


                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_single_1)).setText(
                        mPicsName.get(mPicsName.size() - 1));

                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_single_1)).setSelected(true);
                final int final_double_i = i + 1;
                Glide.with(mContext)
                     .load(mPics.get(mPics.size() - 1))
                     .sizeMultiplier(0.5f)
                     .centerCrop()
                     .placeholder(R.mipmap.ic_launcher)
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_double_1);
                ((ImageView) inflate1.findViewById(R.id.item_scroll_view_single_2)).setBackgroundColor(
                        Color.TRANSPARENT);

                iv_double_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scrollItemCliclSingle(final_double_i, 1);
                    }
                });
                iv_double_1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        scrollItemLongCliclSingle(final_double_i, 1);

                        return true;
                    }
                });
                mLlContainer.addView(inflate1);
            }
        }
    }

    private void setLandscapeOrientation() {
        RxBus.getInstance()
             .post(new UserEvent(ORIENTATION_LANDSCAPE, "orientation"));
        mLlContainer.removeAllViews();
        mLl_Arr.clear();

        for (int i = 0; i < mPics.size() / 2; i++) {
            LinearLayout inflate = (LinearLayout) View.inflate(mContext,
                                                               R.layout.custum_scroll_item_view_double,
                                                               null);
            mLl_Arr.add(inflate);
            ImageView iv_1 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_1);
            ImageView iv_2 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_2);
            ImageView iv_3 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_3);
            ImageView iv_4 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_4);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_1)).setText(mPicsName.get(
                    2 * i));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_2)).setText(mPicsName.get(
                    2 * i + 1));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_3)).setText(mPicsName.get(
                    2 * i));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_4)).setText(mPicsName.get(
                    2 * i + 1));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_1)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_2)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_3)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_4)).setSelected(true);

            Log.d(TAG,
                  "setLandscapeOrientation: " + mPicsName.get(i * 2) + "::" + ((TextView) inflate.findViewById(
                          R.id.item_tx_scroll_view_double_1)).getText());
            final int final_i = i;
            Glide.with(mContext)
                 .load(mPics.get(2 * i))
                 .centerCrop()
                 .sizeMultiplier(0.5f)
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);
            Glide.with(mContext)
                 .load(mPics.get(2 * i + 1))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)

                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_2);
            Glide.with(mContext)
                 .load(mPics.get(2 * i))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_3);
            Glide.with(mContext)
                 .load(mPics.get(2 * i + 1))
                 .centerCrop()
                 .sizeMultiplier(0.5f)
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_4);
            iv_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_i, 1);
                }
            });
            iv_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_i, 2);
                }
            });
            iv_3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_i, 1);
                }
            });
            iv_4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_i, 2);
                }
            });

            iv_1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_i, 1);

                    return true;
                }
            });
            iv_2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_i, 2);

                    return true;
                }
            });
            iv_3.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_i, 1);

                    return true;
                }
            });
            iv_4.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_i, 2);


                    return true;
                }
            });


            mLlContainer.addView(inflate);
            if (mPics.size() % 2 != 0 && i == mPics.size() / 2 - 1) {

                //说明 单了一个 ， 要最后添加一个
                LinearLayout inflate1 = (LinearLayout) View.inflate(mContext,
                                                                    R.layout.custum_scroll_item_view_double,
                                                                    null);
                int height = inflate.getHeight();
                mLl_Arr.add(inflate1);
                ImageView iv_double_1 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_1);
                ImageView iv_double_2 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_2);
                ImageView iv_double_3 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_3);
                ImageView iv_double_4 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_4);

                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_1)).setText(
                        mPicsName.get(mPicsName.size() - 1));
                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_3)).setText(
                        mPicsName.get(mPicsName.size() - 1));
                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_1)).setSelected(true);
                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_3)).setSelected(true);
                final int final_double_i = i + 1;
                iv_double_2.setBackgroundColor(Color.TRANSPARENT);
                iv_double_4.setBackgroundColor(Color.TRANSPARENT);
                Glide.with(mContext)
                     .load(mPics.get(mPics.size() - 1))
                     .sizeMultiplier(0.5f)

                     .centerCrop()
                     .placeholder(R.mipmap.ic_launcher)
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_double_1);

                Glide.with(mContext)
                     .load(mPics.get(mPics.size() - 1))
                     .sizeMultiplier(0.5f)
                     .placeholder(R.mipmap.ic_launcher)
                     .centerCrop()
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_double_3);

                iv_double_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scrollItemCliclDouble(final_double_i, 1);
                    }
                });
                iv_double_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scrollItemCliclDouble(final_double_i, 2);

                    }
                });
                iv_double_3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scrollItemCliclDouble(final_double_i, 1);
                    }
                });
                iv_double_4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scrollItemCliclDouble(final_double_i, 2);

                    }
                });
                iv_double_1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        scrollItemLongCliclDouble(final_double_i, 1);
                        return true;
                    }
                });
                iv_double_2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        scrollItemLongCliclDouble(final_double_i, 2);

                        return true;
                    }
                });
                iv_double_3.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        scrollItemLongCliclDouble(final_double_i, 1);

                        return true;
                    }
                });
                iv_double_4.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        scrollItemLongCliclDouble(final_double_i, 2);

                        return true;
                    }
                });
                iv_double_2.setVisibility(View.INVISIBLE);
                iv_double_4.setVisibility(View.INVISIBLE);
                mLlContainer.addView(inflate1);
            }
        }

        if (mPics.size() == 1) {


            //说明 单了一个 ， 要最后添加一个
            LinearLayout inflate1 = (LinearLayout) View.inflate(mContext,
                                                                R.layout.custum_scroll_item_view_double,
                                                                null);
            mLl_Arr.add(inflate1);
            ImageView iv_double_1 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_1);
            ImageView iv_double_2 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_2);
            ImageView iv_double_3 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_3);
            ImageView iv_double_4 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_4);

            ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_1)).setText(mPicsName.get(
                    mPicsName.size() - 1));
            ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_3)).setText(mPicsName.get(
                    mPicsName.size() - 1));

            ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_1)).setSelected(true);
            ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_3)).setSelected(true);

            final int final_double_i = 0;
            iv_double_2.setBackgroundColor(Color.TRANSPARENT);
            iv_double_4.setBackgroundColor(Color.TRANSPARENT);
            Glide.with(mContext)
                 .load(mPics.get(mPics.size() - 1))
                 .sizeMultiplier(0.5f)

                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_double_1);

            Glide.with(mContext)
                 .load(mPics.get(mPics.size() - 1))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_double_3);

            iv_double_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_double_i, 1);
                }
            });
            iv_double_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_double_i, 2);

                }
            });
            iv_double_3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_double_i, 1);
                }
            });
            iv_double_4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollItemCliclDouble(final_double_i, 2);

                }
            });
            iv_double_1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_double_i, 1);
                    return true;
                }
            });
            iv_double_2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_double_i, 2);

                    return true;
                }
            });
            iv_double_3.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_double_i, 1);

                    return true;
                }
            });
            iv_double_4.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scrollItemLongCliclDouble(final_double_i, 2);
                    return true;
                }
            });
            iv_double_2.setVisibility(View.INVISIBLE);
            iv_double_4.setVisibility(View.INVISIBLE);
            mLlContainer.addView(inflate1);
        }
    }

    @Override
    public void onScrollViewKeydownChanged(int keyCode) {


        if (mInitOrientation == ORIENTATION_PROTRAIT) {
            return;
        }

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
        if (mObservableScrollview != null) {
            scrollViewHegiht = mObservableScrollview.getMeasuredHeight();
        }
        int remainder = scrollViewHegiht % llItemHeight;
        //看一共有多少行
        int line_num = 0;
        if (mPics.size() % 2 == 0) {
            //说明是偶数
            line_num = mPics.size() / 2;
        } else {
            line_num = mPics.size() / 2 + 1;
        }
        int position_i;
        Log.d(TAG, "onScrollViewKeydownChanged: " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                //先做postion_i的计算
                position_i = mSelectedPostion.getPosition_i();
                Log.d(TAG, "onScrollViewKeydownChanged: 向上" + position_i);
                if (position_i == 0) {
                    //已经到了 00 坐标
                    break;
                } else {
                    position_i--;
                }
                mSelectedPostion.setPosition_i(position_i);
                if ((position_i * llItemHeight) > mObservableScrollview.getScrollY()) {

                    //说明在范围内
                    Log.d(TAG, "onScrollViewKeydownChanged: 说明在一个范围内");

                } else if ((position_i * llItemHeight) <= mObservableScrollview.getScrollY()) {

                    //那就把这个移动在最底下
                    Log.d(TAG, "onScrollViewKeydownChanged: 说明要一定了");
                    int move_height = scrollViewHegiht - (llItemHeight - (mObservableScrollview.getScrollY() - (position_i * llItemHeight)));
                    mObservableScrollview.smoothScrollBy(0, -move_height);

                }
                moveFocusMedia("move_i", position_i, false);

                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG,
                      "onKeyDown: :::::::1下" + "::" + mSelectedPostion.getPosition_i() + "::" + mSelectedPostion.getPosition_j() + "::" + mPicsName.size());

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

                if (mPics.size() % 2 != 0) {
                    //说明多出一个
                    if (mSelectedPostion.getPosition_i() == mPics.size() / 2 - 1) {
                        //到了最后一个
                        if (mSelectedPostion.getPosition_j() == 1) {
                            return;
                        }
                    }
                }


                if ((position_i * llItemHeight) >= mObservableScrollview.getScrollY() && ((position_i + 1) * llItemHeight) <= (mObservableScrollview.getScrollY() + scrollViewHegiht)) {
                    //在里面 并不用去 跳转  只需要把框框加一下


                } else if (((position_i + 1) * llItemHeight) >= (mObservableScrollview.getScrollY() + scrollViewHegiht)) {
                    //这里就说明要重新移动了

                    mObservableScrollview.smoothScrollBy(0,
                                                         (position_i * llItemHeight) - mObservableScrollview.getScrollY());

                }

                mSelectedPostion.setPosition_i(position_i);
                moveFocusMedia("move_i", position_i, true);
                Log.d(TAG, "onScrollViewKeydownChanged: position_i" + position_i);

                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mSelectedPostion.getPosition_j() == 0) {
                    //说明已经在左边 不用变
                } else {

                    mSelectedPostion.setPosition_j(0);
                    int position_j = mSelectedPostion.getPosition_j();
                    moveFocusMedia("move_j", position_j, false);
                }


                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(TAG,
                      "onScrollViewKeydownChanged: " + mSelectedPostion.getPosition_i() + (mPics.size() / 2));
                if (mPics.size() / 2 != 0) {

                    if (mSelectedPostion.getPosition_i() == (mPics.size() / 2)) {

                        if (mSelectedPostion.getPosition_j() == 0) {
                            return;
                        }
                    }

                }
                if (mSelectedPostion.getPosition_j() == 1) {
                    //说明已经在右边 不用变

                } else {

                    //说明在左边，需要变到右边
                    mSelectedPostion.setPosition_j(1);
                    int position_j = mSelectedPostion.getPosition_j();
                    moveFocusMedia("move_j", position_j, true);

                }

                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:



                LogUtils.prinfLog("size = " + mPics.size() + "输出的selected" + "::i = " + mSelectedPostion.getPosition_i() + "::j = " + mSelectedPostion.getPosition_j());

                Intent intent = new Intent(mContext, DetailPicAcitvity.class);
                intent.putStringArrayListExtra("PicUrl", mPics);
                intent.putExtra("PicPosition",
                                2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
                startActivity(intent);

                //                File file = new File(mPics.get(2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j()));
                //                //下方是是通过Intent调用系统的图片查看器的关键代码
                //                Intent intent = new Intent();
                //                intent.setAction(Intent.ACTION_VIEW);
                //                intent.setDataAndType(Uri.fromFile(file), "image/*");
                //                startActivity(intent);


                break;
        }
    }

    private void moveSetImageViewBG(int position_i, int position_j, int parseColor) {
        Log.d(TAG,
              "moveSetImageViewBG: " + mLl_Arr.size() + position_i + "::" + position_j + "::" + mLl_Arr.get(
                      position_i)
                                                                                                       .getChildAt(
                                                                                                               position_j));
        ImageView iv = (ImageView) ((LinearLayout) mLl_Arr.get(position_i)
                                                          .getChildAt(position_j)).getChildAt(0);
        iv.setBackgroundColor(parseColor);

    }

    private void scrollItemCliclSingle(int i, int j) {
        if (mSelectedPostion.getIsLongClick()) {
            //如果处于长安状态   那么
            //单机时间就不要处理
            return;
        }
        Log.d(TAG,
              "scrollItemCliclSingle: i=" + i + "::j = " + j + "geti" + mSelectedPostion.getPosition_i() + "::" + mSelectedPostion.getPosition_j());
        mSelectedPostion.setBefore_position_i(mSelectedPostion.getPosition_i());
        mSelectedPostion.setBefore_position_j(mSelectedPostion.getPosition_j());
        mSelectedPostion.setPosition_i(i);
        mSelectedPostion.setPosition_j(j - 1);
        Intent intent = new Intent(mContext, DetailPicAcitvity.class);
        intent.putStringArrayListExtra("Videos", mPics);
        intent.putExtra("position",
                        2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
        startActivity(intent);

    }

    public void scrollItemLongCliclSingle(int i, int j) {

        Log.d(TAG, "scrollItemLongCliclDouble: 长安 single状态" + i + "::" + j);
        if (mSelectedPostion.getIsLongClick()) {
            //如果已经处于长安状态
            return;
        }
        if (mSelectedPostion.isDoing()) {
            Toast.makeText(mContext, " 正在传输中，等待任务完成再次传输", Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffff0000"));

        UserEvent userEvent = new UserEvent(0, "transfer_file");
        userEvent.setFileName(mPics.get(2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j()));
        RxBus.getInstance()
             .post(userEvent);
        mSelectedPostion.setLongClick(true);

    }

    public void scrollItemCliclDouble(int i, int j) {
        if (mSelectedPostion.getIsLongClick() || click_state == CLICK_STATE_LONG_CLICK) {
            //如果处于长安状态   那么
            //单机时间就不要处理
            Log.d(TAG, "scrollItemCliclDouble: getIsLongClick");

            if(hasLongSelected!=null && hasLongSelected.size() != 0){
                for(int num = 0 ; num < hasLongSelected.size() ; num++){
                    int[] ints = hasLongSelected.get(num);
                    if(ints[0] == i && ints[1] == j){
                        moveFocusNowColor(i , j ,Color.TRANSPARENT);
                        hasLongSelected.remove(num);
                        if(hasLongSelected.size() == 0){
                            mBtn_cancle_del.setText("取消");
                        }
                    return;
                    }
                }
            }


            int[] hasLongClick = new int[2];
            hasLongClick[0] = i ;
            hasLongClick[1] = j;
            hasLongSelected.add(hasLongClick);
            moveFocusNowColor(i , j ,Color.parseColor("#ffff0000"));
            if(hasLongSelected.size() != 0){
                mBtn_cancle_del.setText("删除");
            }

            return;
        }
        moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffffff00"));
        if (mSelectedPostion.getIsFirst()) {
            mSelectedPostion.setIsFirst(false);
        }
        if(mIsSelctedClick) {

            Intent intent = new Intent(mContext, DetailPicAcitvity.class);
            intent.putStringArrayListExtra("PicUrl", mPics);
            intent.putExtra("PicPosition", 2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
            startActivity(intent);

        }

    }

    private void moveFocusNowColor(int i, int j , int color) {

        moveSetImageViewBG(i,
                           j-1,
                           color);
        moveSetImageViewBG(i,
                           j-1 + 2,
                          color);

    }

    private boolean mIsSelctedClick = false;
    public void moveBeforeFocusNowColor(int i, int j, int transparent, int color) {


        if (i == mSelectedPostion.getPosition_i() && (j - 1) == mSelectedPostion.getPosition_j() && click_state != CLICK_STATE_LONG_CLICK) {
            Log.d(TAG, "moveBeforeFocusNowColor: 目前被点击的  和 目前被选中的一样");
            //目前被点击的  和选中的一样  那么 仅仅只要跳转界面。

            mIsSelctedClick = true;
        }else{
            Log.d(TAG, "moveBeforeFocusNowColor: 目前被点击的  和目前选中的不一样");
            //目前被点击的 和选中的不一样  那么  要更新 背景色 和   参数
            mSelectedPostion.setBefore_position_i(mSelectedPostion.getPosition_i());
            mSelectedPostion.setBefore_position_j(mSelectedPostion.getPosition_j());
            mSelectedPostion.setPosition_i(i);
            mSelectedPostion.setPosition_j(j - 1);

            if (mInitOrientation == ORIENTATION_LANDSCAPE) {
                moveSetImageViewBG(mSelectedPostion.getBefore_position_i(),
                                   mSelectedPostion.getBefore_position_j(),
                                   transparent);
                moveSetImageViewBG(mSelectedPostion.getBefore_position_i(),
                                   mSelectedPostion.getBefore_position_j() + 2,
                                   transparent);
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   color);
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 2,
                                   color);
            } else if (mInitOrientation == ORIENTATION_PROTRAIT) {

                moveSetImageViewBG(mSelectedPostion.getBefore_position_i(),
                                   mSelectedPostion.getBefore_position_j(),
                                   transparent);
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   color);

            }
            mIsSelctedClick = false;
        }
        //把之前的变成 无边框1
        //把选中的变成 有边框
    }

    public void scrollItemLongCliclDouble(int i, int j) {
        //点击长安 就变成了长安的状态
        //退出长安的操作 有 ： 删除了文件 ，  没有选中一个文件 就点击去掉   或者退出键
        //长安之后  记录之下 之前的被选中的i ,j
        //更新状态
        if(click_state == CLICK_STATE_LONG_CLICK){
            return;
        }

        click_state = CLICK_STATE_LONG_CLICK;
        Log.d(TAG, "scrollItemLongCliclDouble: 长安 double状态");

        int[] hasLongClick = new int[2];
        hasLongClick[0] = i;
        hasLongClick[1] = j;
        hasLongSelected.add(hasLongClick);
        mBtn_cancle_del.setVisibility(View.VISIBLE);
        mBtn_cancle_del.setText("删除");
        //记录之前被选中的ij
        mLongClickBeforeSlection.setPosition_i(mSelectedPostion.getPosition_i());
        mLongClickBeforeSlection.setPosition_j(mSelectedPostion.getPosition_j() +1 );

        //传递一个信息
     moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffff0000"));



        mSelectedPostion.setLongClick(true);



        //如果是横屏的状态 一开始是不存在那个进度条的

    }


    private void moveFocusMedia(String str, int position_i, boolean isDownRight) {
        if (str.equals("move_i")) {

            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j(),
                               Color.parseColor("#ffffff00"));
            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j() + 2,
                               Color.parseColor("#ffffff00"));

            if (position_i != 0) {
                //下
                if (isDownRight) {

                    moveSetImageViewBG(mSelectedPostion.getPosition_i() - 1,
                                       mSelectedPostion.getPosition_j(),
                                       Color.TRANSPARENT);
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() - 1,
                                       mSelectedPostion.getPosition_j() + 2,
                                       Color.TRANSPARENT);

                } else {
                    //上

                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j(),
                                       Color.TRANSPARENT);
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j() + 2,
                                       Color.TRANSPARENT);


                }
            } else if (position_i == 0) {
                //position_i == 0的时候  那么 up的时候 要消失掉之前的。因为之前有一个postion_i!=0的判断
                if (!isDownRight) {
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j(),
                                       Color.TRANSPARENT);
                    moveSetImageViewBG(mSelectedPostion.getPosition_i() + 1,
                                       mSelectedPostion.getPosition_j() + 2,
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
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 2,
                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() - 1,
                                   Color.TRANSPARENT);
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 2 - 1,
                                   Color.TRANSPARENT);

            } else {
                //左边移动一下
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 2,
                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 1,
                                   Color.TRANSPARENT);
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 3,
                                   Color.TRANSPARENT);

            }

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        int ori = newConfig.orientation; //获取屏幕方向
        if (ori == newConfig.ORIENTATION_LANDSCAPE) {
            //如果是横屏
            mIv_right.setVisibility(View.VISIBLE);
        } else if (ori == newConfig.ORIENTATION_PORTRAIT) {
            mIv_right.setVisibility(View.GONE);

        }

        super.onConfigurationChanged(newConfig);
    }

    public class FileCreatedDemo {
        //        public static void main(String[] args){
        //            try
        //            {
        //                String fileCreated = getFileCreated("D:\\xiyou.jpg");
        //                System.out.println(fileCreated);
        //            }
        //            catch(Exception e)
        //            {
        //                e.printStackTrace();
        //            }
        //        }
        public FileCreatedDemo() {}

        public String getFileCreated(String path)
        {
            if (null == path) {
                return null;
            }
            return getFileCreated(new File(path));
        }

        public String getFileCreated(final File file)
        {
            if (null == file) {
                return null;
            }
            String              rs = null;
            final StringBuilder sb = new StringBuilder();
            Process             p  = null;
            try {
                p = Runtime.getRuntime()
                           .exec(String.format("cmd /C dir %s /tc", file.getAbsolutePath()));
            } catch (IOException e) {
                return rs;
            }
            final InputStream       is = p.getInputStream();
            final InputStreamReader ir = new InputStreamReader(is);
            final BufferedReader    br = new BufferedReader(ir);
            Runnable runnable = new Runnable() {
                @Override
                public void run()
                {
                    String data = null;
                    try {
                        while (null != (data = br.readLine())) {
                            if (-1 != data.toLowerCase()
                                          .indexOf(file.getName()
                                                       .toLowerCase()))
                            {
                                String[] temp = data.split(" +");
                                if (2 <= temp.length) {
                                    String time = String.format("%s %s", temp[0], temp[1]);
                                    sb.append(time);
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (null != br) {
                                br.close();
                            }
                            if (null != ir) {
                                ir.close();
                            }
                            if (null != is) {
                                is.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (0 != sb.length()) {
                rs = sb.toString();
            }
            return rs;
        }
    }
}
