package com.example.lyc.vrexplayer.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.view.MyAnimationSurfaceView;

import java.util.ArrayList;

public class DetailPicAcitvity
        extends AppCompatActivity
        implements View.OnClickListener, SurfaceHolder.Callback
{

    private static final String TAG               = "DetailPicAcitvity";
    private static final int    ANIMATION_END     = 10;
    private static final int    NO_2D_3D_STATUS   = 1003;
    private static final int    SLMONITORAUTOMISS = 11;
    private int                    mCurrentPostion;
    private MyAnimationSurfaceView mImgView1;
    private ImageView              mImgView2;
    private ArrayList<String>      mPicUrl;
    private Animation              mAlphaAppearAnimation;
    private Animation              mAlphaDismissAnimation;
    private int                    mScreenLight;
    private              int animationState         = -1;
    private static final int animatingRightState    = 2;
    private static final int animationEndRightState = 3;

    private static final int INTER_NEAREST  = 0;
    private static final int INTER_LINEAR   = 1;
    private static final int INTER_CUBIC    = 2;
    private static final int INTER_AREA     = 3;
    private static final int INTER_LANCZOS4 = 4;


    private Button mLeft_2D;
    private Button mLeft_3D;
    private Button mRight_2D;
    private Button mRight_3D;
    private static final int     PHOTO_PLAYING_STATUS = 101;
    private static final int     PHOTO_2D_3D_SELECT   = 102;
    private              int     activity_status      = 101;
    private static final int     BUTTON_MISS          = 1002;
    //button正处于的状态：
    private              boolean isLeftButton2D       = true;
    private              boolean buttonIsSHow         = false;
    private Bitmap mBitmap_left;
    //Pic目前处于的状态
    private boolean picIs2D = true;
    //资源的来向
    private int res_status;
    private static final   int   RES_STATUS_EXTENER = 20;
    private static final   int   RES_STATUS_INTENER = 21;
    protected static final float FLIP_DISTANCE      = 50;
    private                int   status_huadong     = 0;
    private static final   int   START_HUADONGING   = 30;

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ANIMATION_END:
                    animationState = animationEndRightState;
                    break;
                case BUTTON_MISS:
                    //让buttonMiss掉
                    updateButtonStatus(false);
                    //并且 更新状态
                    activity_status = PHOTO_PLAYING_STATUS;
                    break;
                case NO_2D_3D_STATUS:
                    firstClickButtonA = 0;
                    break;

            }
            super.handleMessage(msg);
        }
    };
    private WindowManager.LayoutParams mAttributes;
    private RelativeLayout             mRl_left;
    private RelativeLayout             mRl_right;
    private Animation                  mFrom2DTo3DMissAnimation;
    private Animation                  mFrom2DTo3DAppearAnimation;
    private AlphaAnimation             mFrom3DTo2DAppearAnimation;
    private AlphaAnimation             mFrom3DTo2DMissAnimation;
    private Uri                        mData;
    private View                       mLayout;
    private TextView                   mTvLight_left;
    private TextView                   mTvLight_right;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SLMONITORAUTOMISS:
                    setScreenLightMonitorVisible(false);
                    break;
            }
            super.handleMessage(msg);

        }
    };
    private Cursor             mCursor;
    private int                mColumn_index;
    private GestureDetector    mDetector;
    private TranslateAnimation mLeftMissAnimation;
    private TranslateAnimation mLeftAppearAnimation;
    private TranslateAnimation mRightMissAnimation;
    private Display            mDefaultDisplay;
    private boolean            mHas4K;
    private SurfaceHolder      mSurfaceHolder;

    static {
        System.loadLibrary("native-lib");
    }

    private boolean mIsFirstEnter = false;
    private RelativeLayout     mRl_container;
    private TranslateAnimation mRightAppearAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_detail_pic_acitvity, null);
        getSupportActionBar().hide();
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initDisplay();
        setContentView(mLayout);

        initViews();
        initData();
        initEvent();
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
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onScroll(MotionEvent e1,
                                    MotionEvent e2,
                                    float distanceX,
                                    float distanceY)
            {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // TODO Auto-generated method stub

            }

            /**
             *
             * e1 The first down motion event that started the fling. e2 The
             * move motion event that triggered the current onFling.
             */
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
                if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向左滑...");

                    if (status_huadong == START_HUADONGING) {
                        return true;
                    }

                    Log.d(TAG, "onFling: 加载"+ mCurrentPostion);
                    if (mCurrentPostion == mPicUrl.size() - 1) {
                        mCurrentPostion = 0;
                    } else {
                        mCurrentPostion++;
                    }
                    Log.d(TAG, "onFling: 滑动之前" + mPicUrl.get(mCurrentPostion));
                    //                    setPathAndSurfaceView(mPicUrl.get(mCurrentPostion),
                    //                                          mSurfaceHolder.getSurface(),
                    //                                          3840,
                    //                                          1700,
                    //                                          INTER_AREA);
                    mImgView1.startAnimation(mLeftMissAnimation);
                    //mImgView1.translateAnimation();
                    return true;
                }
                if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向右滑...");
                    //                         .into(mImgView2);

                    Log.d(TAG, "onFling: 加载"+ mCurrentPostion);
                    if (status_huadong == START_HUADONGING) {
                        return true;
                    }

                    if (mCurrentPostion == 0) {
                        mCurrentPostion = mPicUrl.size() - 1;
                    } else {
                        mCurrentPostion--;
                    }
                    mImgView1.startAnimation(mRightMissAnimation);

                    //                    KeyEvent keyEvent = new KeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT , 1);
                    //                    onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT ,keyEvent);
                    return true;
                }
                if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向上滑...");
                    return true;
                }
                if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向下滑...");
                    return true;
                }

                Log.d("TAG", e2.getX() + " " + e2.getY());

                return false;


            }

            @Override
            public boolean onDown(MotionEvent e) {

                // TODO Auto-generated method stub

                return false;

            }
        });
    }

    private void initDisplay() {
        String radioVersion = Build.getRadioVersion();
        int    sdkInt       = Build.VERSION.SDK_INT;
        Log.d(TAG, "initDisplay: "+radioVersion + "::"+sdkInt);
       mDefaultDisplay = getWindowManager().getDefaultDisplay();
       Display.Mode[] supportedModes = mDefaultDisplay.getSupportedModes();

       for(int i = 0 ; i < supportedModes.length ; i++){
           int physicalWidth = supportedModes[i].getPhysicalWidth();
           int physicalHeight = supportedModes[i].getPhysicalHeight();
           Log.d(TAG, "initDisplay: "+"::" + physicalWidth + "::" + physicalHeight);
          //  Toast.makeText(this, physicalHeight + "::" + physicalWidth,Toast.LENGTH_LONG).show();
       }
       mHas4K = false;
       for (Display.Mode mode1 : supportedModes) {
           if (mode1.getPhysicalHeight() == 3840 || mode1.getPhysicalWidth() == 3840) {
               mHas4K = true;
           }
       }

       if (mHas4K) {
           Window                     window                 = getWindow();
           WindowManager.LayoutParams winParams              = window.getAttributes();
           int                        preferredDisplayModeId = winParams.preferredDisplayModeId;
           winParams.preferredDisplayModeId = 2;
           winParams.preferredRefreshRate = 60.0f;

           WindowManager.LayoutParams attributes = window.getAttributes();
           window.setAttributes(winParams);
       }
    }

    private void initEvent() {
        mLeft_2D.setOnClickListener(this);
        mLeft_3D.setOnClickListener(this);
        mRight_2D.setOnClickListener(this);
        mRight_3D.setOnClickListener(this);
    }

    private void initData() {

        mAlphaAppearAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                       0,
                                                       Animation.RELATIVE_TO_SELF,
                                                       0,
                                                       Animation.RELATIVE_TO_SELF,
                                                       -1,
                                                       Animation.RELATIVE_TO_SELF,
                                                       0);
        mAlphaAppearAnimation.setDuration(1000);
        mAlphaDismissAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                        0,
                                                        Animation.RELATIVE_TO_SELF,
                                                        0,
                                                        Animation.RELATIVE_TO_SELF,
                                                        0,
                                                        Animation.RELATIVE_TO_SELF,
                                                        -1);
        mAlphaDismissAnimation.setDuration(1000);
        mAlphaDismissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationState = animatingRightState;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: timestart" + System.currentTimeMillis());
                //
                //                setPathAndSurfaceView(mPicUrl.get(mCurrentPostion),
                //                                      mSurfaceHolder.getSurface(),
                //                                      3840,
                //                                      1700,
                //                                      INTER_AREA);
                setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
                mImgView1.startAnimation(mAlphaAppearAnimation);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mAlphaAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG, "onAnimationStart: 出现动画开始的时间" + System.currentTimeMillis());
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationStart: 出现动画结束的时间" + System.currentTimeMillis());

                animationState = animationEndRightState;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }


        });
        //获取屏幕的亮度配置
        mAttributes = getWindow().getAttributes();
        //获取当前屏幕的亮度   如何以前有设置过  就按照之前的来，如果没有  就获取前屏幕的亮度来显示
        SharedPreferences detaiPicScreenLight = getSharedPreferences("DetaiPicScreenLight",
                                                                     Activity.MODE_PRIVATE);
        int screenLight = detaiPicScreenLight.getInt("ScreenLight", -1);
        if (screenLight == -1) {
            //这里是之前没有保存过
            int             value = 0;
            ContentResolver cr    = getContentResolver();
            try {
                value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
            } catch (Exception e) {

            }
            mScreenLight = value;
        } else {
            //这里是  之前有保存过习惯的亮度
            mScreenLight = screenLight;
        }
        Log.d(TAG, "initData: mScreenLightmScreenLightchushihua" + mScreenLight);
        //先进性一下初始化
        // setBrightness(mScreenLight);

        setBrightness(255);

        Intent intent = getIntent();
        mPicUrl = (ArrayList) intent.getSerializableExtra("PicUrl");
        int picPosition = intent.getIntExtra("PicPosition", -1);
        mCurrentPostion = picPosition;
        Log.d(TAG, "initData: mCurrentPostion" + mCurrentPostion);
        if (mCurrentPostion == -1) {

            //说明是来自外部的资源
            mData = intent.getData();
            //            Glide.with(DetailPicAcitvity.this)
            //
            //                 .load(mData)
            //                 .crossFade(500)
            //                 .skipMemoryCache(true)
            //                 .diskCacheStrategy(DiskCacheStrategy.NONE)
            //                 .fitCenter()
            //                 .into(mImgView1);
            // TODO: 2016/11/3 这个需要渐渐消失的一个动画

            // can post image
            String[] proj = {MediaStore.Images.Media.DATA};
            Log.d(TAG, "initData: mData=" + mData.getEncodedPath());
            Log.d(TAG, "initData: mData=" + mData.getPath());

            // Which columns to return
            // WHERE clause; which rows to return (all rows)
            // WHERE clause selection arguments (none)
            // Order-by clause (ascending by name)
            mCursor = getContentResolver().query(mData, proj,
                                                 // Which columns to return
                                                 null,
                                                 // WHERE clause; which rows to return (all rows)
                                                 null,
                                                 // WHERE clause selection arguments (none)
                                                 null);
            // mCursor = managedQuery();
            Log.d(TAG, "initData: mCursor=" + mCursor);
            if (mCursor == null) {
                //                setPathAndSurfaceView(mData.getPath(),
                //                                      mSurfaceHolder.getSurface(),
                //                                      3840,
                //                                      1700,
                //                                      INTER_AREA);

                setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
            } else {
                mColumn_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                mCursor.moveToFirst();
                String string = mCursor.getString(mColumn_index);
                //                setPathAndSurfaceView(string,
                //                                      mSurfaceHolder.getSurface(),
                //                                      3840,
                //                                      1700,
                //                                      INTER_AREA);
                setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
            }
            res_status = RES_STATUS_EXTENER;
        } else {
            Log.d(TAG, "initData: 进入加载" + mPicUrl.get(mCurrentPostion));
            mIsFirstEnter = true;


            res_status = RES_STATUS_INTENER;
        }


        mFrom2DTo3DMissAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFrom2DTo3DMissAnimation.setDuration(500);
        mFrom2DTo3DMissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //   mImgView2.setVisibility(View.GONE);
                //这里最好重新加载一下
                if (res_status == RES_STATUS_EXTENER) {
                    //如果是外部的存储
                    //                    Glide.with(DetailPicAcitvity.this)
                    //                         .load(mData)
                    //                         .fitCenter()
                    //                         .animate(mFrom2DTo3DAppearAnimation)
                    //                         .skipMemoryCache(true)
                    //                         .diskCacheStrategy(DiskCacheStrategy.NONE)
                    //                         .into(mImgView1);
                    if (mCursor == null) {
                        //                        setPathAndSurfaceView(mData.getPath(),
                        //                                              mSurfaceHolder.getSurface(),
                        //                                              3840,
                        //                                              1700,
                        //                                              INTER_AREA);
                        setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
                    } else {
                        String string = mCursor.getString(mColumn_index);
                        //                        setPathAndSurfaceView(string,
                        //                                              mSurfaceHolder.getSurface(),
                        //                                              3840,
                        //                                              1700,
                        //                                              INTER_AREA);
                        setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
                    }
                    mImgView1.startAnimation(mFrom2DTo3DAppearAnimation);
                } else {
                    //                    setPathAndSurfaceView(mPicUrl.get(mCurrentPostion),
                    //                                          mSurfaceHolder.getSurface(),
                    //                                          3840,
                    //                                          1700,
                    //                                          INTER_AREA);
                    setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
                    mImgView1.startAnimation(mFrom2DTo3DAppearAnimation);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mFrom2DTo3DAppearAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFrom2DTo3DAppearAnimation.setDuration(500);
        mFrom2DTo3DAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                picIs2D = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFrom3DTo2DMissAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFrom3DTo2DMissAnimation.setDuration(500);
        mFrom3DTo2DMissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //mImage1 miss掉之后  12  要一起出现
                // mImgView2.setVisibility(View.VISIBLE);
                mImgView1.startAnimation(mFrom3DTo2DAppearAnimation);
                // mImgView2.startAnimation(mFrom3DTo2DAppearAnimation);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mFrom3DTo2DAppearAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFrom3DTo2DAppearAnimation.setDuration(500);
        mFrom3DTo2DAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                picIs2D = true;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLeftMissAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                    0,
                                                    Animation.RELATIVE_TO_SELF,
                                                    -1,
                                                    Animation.RELATIVE_TO_SELF,
                                                    0,
                                                    Animation.RELATIVE_TO_SELF,
                                                    0);
        mLeftMissAnimation.setDuration(1000);
        mLeftMissAnimation.setFillAfter(false);
        mLeftMissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                status_huadong = START_HUADONGING;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd:  ");
                //                setPathAndSurfaceView(mPicUrl.get(mCurrentPostion),
                //                                      mSurfaceHolder.getSurface(),
                //                                      3840,
                //                                      1700,
                //                                      INTER_AREA);
                setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
                mImgView1.startAnimation(mLeftAppearAnimation);
                status_huadong = 0;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLeftAppearAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                      1,
                                                      Animation.RELATIVE_TO_SELF,
                                                      0,
                                                      Animation.RELATIVE_TO_SELF,
                                                      0,
                                                      Animation.RELATIVE_TO_SELF,
                                                      0);
        mLeftAppearAnimation.setDuration(1000);
        mLeftAppearAnimation.setFillAfter(true);

        mRightMissAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                     0,
                                                     Animation.RELATIVE_TO_SELF,
                                                     1,
                                                     Animation.RELATIVE_TO_SELF,
                                                     0,
                                                     Animation.RELATIVE_TO_SELF,
                                                     0);
        mRightMissAnimation.setDuration(1000);
        mRightMissAnimation.setFillAfter(false);
        mRightMissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                status_huadong = START_HUADONGING;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //                setPathAndSurfaceView(mPicUrl.get(mCurrentPostion),
                //                                      mSurfaceHolder.getSurface(),
                //                                      3840,
                //                                      1700,
                //                                      INTER_AREA);
                setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
                mImgView1.startAnimation(mRightAppearAnimation);
                status_huadong = 0;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRightAppearAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                       -1,
                                                       Animation.RELATIVE_TO_SELF,
                                                       0,
                                                       Animation.RELATIVE_TO_SELF,
                                                       0,
                                                       Animation.RELATIVE_TO_SELF,
                                                       0);
        mRightAppearAnimation.setDuration(1000);
        mRightAppearAnimation.setFillAfter(true);

        setSurfaceLayoutParams();

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume:onBackPressed " + mScreenLight);

        if (mImgView1.getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
            mImgView1.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }


        super.onResume();
    }

    private void initViews() {
        mRl_container = (RelativeLayout) findViewById(R.id.rl_container_surface);
        mImgView1 = (MyAnimationSurfaceView) findViewById(R.id.imgView1);
        mImgView2 = (ImageView) findViewById(R.id.imgView2);
        mImgView1.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mImgView1.setOnClickListener(this);
        mImgView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return mDetector.onTouchEvent(motionEvent);

            }
        });
        // mImgView2.setOnClickListener(this);

        //        mImgView1.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //        mImgView1.setOnClickListener(new View.OnClickListener() {
        //
        //            @Override
        //            public void onClick(View v) {
        //                v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //
        //            }
        //        });
        //        mImgView2.setOnClickListener(new View.OnClickListener() {
        //
        //            @Override
        //            public void onClick(View v) {
        //                v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //
        //            }
        //        });


        mLeft_2D = (Button) findViewById(R.id.pic_left_2d);
        mLeft_3D = (Button) findViewById(R.id.pic_left_3d);
        mRight_2D = (Button) findViewById(R.id.pic_right_2d);
        mRight_3D = (Button) findViewById(R.id.pic_right_3d);
        setButtonSelected(true);
        updateButtonStatus(false);
        mTvLight_left = (TextView) findViewById(R.id.light_monitor_left);
        mTvLight_right = (TextView) findViewById(R.id.light_monitor_right);
        mSurfaceHolder = mImgView1.getHolder();
        mSurfaceHolder.addCallback(this);

    }

    int  xiangyinTime       = 0;
    long firstClickButtonA  = 0;
    long secondClickButtonA = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: 看点击一下是什么事件" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                //当他不是在2D/3D切换的时候才进入这里面的判断
                //                if (xiangyinTime % 2 == 0 && activity_status == PHOTO_PLAYING_STATUS) {
                //                    Log.d(TAG, "onKeyDown: A键的最快的时间的计算" + System.currentTimeMillis());
                //                    xiangyinTime = 0;
                //                    if (firstClickButtonA == 0) {
                //                        Log.d(TAG, "onKeyDown: 第一次进来");
                //                        //第一次进来
                //                        firstClickButtonA = System.currentTimeMillis();
                //                        myHandler.sendEmptyMessageDelayed(NO_2D_3D_STATUS, 500);
                //                    } else {
                //                        secondClickButtonA = System.currentTimeMillis();
                //                        if (secondClickButtonA - firstClickButtonA <= 500) {
                //                            //说明想要进入选择2D/3D的状态
                //                            Log.d(TAG, "onKeyDown: 说明想进入到2D/3D的状态");
                //                            //更新状态：从播放状态 ----》2D/3D切换的状态    5S发送 buttom消失的信息
                //                            activity_status = PHOTO_2D_3D_SELECT;
                //                            //更新button的状态
                //                            updateButtonStatus(true);
                //                            //5S后发送消息  让其MISS掉
                //                            Message message = myHandler.obtainMessage(BUTTON_MISS);
                //                            myHandler.sendMessageDelayed(message, 5000);
                //                            return true;
                //                        } else {
                //                            //说明不是想进入到这状态
                //                            Log.d(TAG, "onKeyDown: 说明不是进入到2D/3D的状态");
                //                            //重置一下  下次再比较
                //                            firstClickButtonA = 0;
                //                        }
                //                    }
                //                }
                //                if (xiangyinTime % 2 == 0 && activity_status == PHOTO_2D_3D_SELECT) {
                //                    Log.d(TAG, "onKeyDown: 进入到2D/3D切换状态点击事件");
                //                    xiangyinTime = 0;
                //                    //如果处于这个状态2D_3D_SELECT的话   就要响应点击事件
                //                    if (isLeftButton2D) {
                //                        Log.d(TAG, "onKeyDown: 点击事件左边");
                //                        //如果是左边的时间
                //                        mLeft_2D.performClick();
                //                        return true;
                //                    } else {
                //                        Log.d(TAG, "onKeyDown: 点击事件右边");
                //                        //如果是右边的时间
                //                        mLeft_3D.performClick();
                //                        return true;
                //                    }
                //
                //                }

                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                Log.d(TAG, "initData: mScreenLight" + mScreenLight);
                //                mScreenLight += 5;
                //                if (mScreenLight > 255) {
                //                    mScreenLight = 255;
                //                } else {
                //                    setBrightness(mScreenLight);
                //                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG, "initData: mScreenLight" + mScreenLight);
                //                setScreenLightMonitorVisible(true);
                //                mScreenLight -= 5;
                //                if (mScreenLight < 0) {
                //                    mScreenLight = 0;
                //                } else {
                //                    setBrightness(mScreenLight);
                //                }
                //                delaySendMessager(SLMONITORAUTOMISS, 3000);


                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:

                if (activity_status == PHOTO_2D_3D_SELECT) {

                    setButtonSelected(true);

                } else {
                    if (res_status == RES_STATUS_EXTENER) {
                        //如果是外部资源
                        return true;
                    }
                    if (animationState == animatingRightState) {
                        break;
                    }
                    Log.d(TAG, "onKeyDown: " + mCurrentPostion);
                    if (mCurrentPostion == 0) {
                        mCurrentPostion = mPicUrl.size() - 1;
                    } else {
                        mCurrentPostion--;
                    }

                    mImgView1.startAnimation(mAlphaDismissAnimation);


                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:


                Log.d(TAG, "onKeyDown: 向右的事件" + animationState);
                //在这里  看看是不是正在动画的过程中   如果 正在动画的过程中就不要去响应++事件
                if (activity_status == PHOTO_2D_3D_SELECT) {
                    setButtonSelected(false);

                } else {
                    if (res_status == RES_STATUS_EXTENER) {
                        //如果是外部资源
                        return true;
                    }
                    if (animationState == animatingRightState) {

                        break;
                    }
                    if (mCurrentPostion == mPicUrl.size() - 1) {
                        mCurrentPostion = 0;
                    } else {
                        mCurrentPostion++;
                    }
                    mImgView1.startAnimation(mAlphaDismissAnimation);

                }

                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (mImgView1.getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    mImgView1.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
                //                if (mImgView2.getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                //                    mImgView2.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                //                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateButtonStatus(boolean b) {
        if (b) {
            //让button出现
            mLeft_2D.setVisibility(View.VISIBLE);
            mLeft_3D.setVisibility(View.VISIBLE);
            mRight_2D.setVisibility(View.VISIBLE);
            mRight_3D.setVisibility(View.VISIBLE);
            buttonIsSHow = true;
        } else {
            //让buttonmiss
            mLeft_2D.setVisibility(View.GONE);
            mLeft_3D.setVisibility(View.GONE);
            mRight_2D.setVisibility(View.GONE);
            mRight_3D.setVisibility(View.GONE);
            buttonIsSHow = false;
        }
    }

    private void setBrightness(int screenLight) {
        Log.d(TAG, "setBrightness: screenLight" + screenLight);
        mAttributes.screenBrightness = 1;
        //
        //        getWindow().setAttributes(mAttributes);
        //        mTvLight_left.setText("亮度：" + (int) (screenLight * 100 / 255) + "%");
        //        mTvLight_right.setText("亮度：" + (int) (screenLight * 100 / 255) + "%");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
        Log.d(TAG, "onBackPressed: onDestroy");
        SharedPreferences        sp   = getSharedPreferences("DetaiPicScreenLight",
                                                             Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("ScreenLight", mScreenLight);
        edit.commit();
        if (mBitmap_left != null) {
            mBitmap_left.recycle();
            mBitmap_left = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: ");

        return mDetector.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.pic_left_2d:
                hideSystemUI();
                if (buttonIsSHow) {
                    //                    Toast.makeText(this, "2D被点击", Toast.LENGTH_LONG)
                    //                         .show();

                    //                    if (mImgView2.getVisibility() == View.VISIBLE) {
                    //                        //如果本身就是2D的了  就什么都要做
                    //                        Log.d(TAG, "onClick: picIs2D" + picIs2D);
                    //                    } else if (mImgView2.getVisibility() == View.GONE) {
                    //                        //如果本身是3D的了  mImgView1 要miss掉  然后  miss掉 12一起出现
                    //                        Log.d(TAG, "onClick: !picIs2D" + !picIs2D);
                    //                        mImgView1.startAnimation(mFrom3DTo2DMissAnimation);
                    //
                    //                    }

                    updateButtonStatus(false);
                    myHandler.removeMessages(BUTTON_MISS);
                    //自己来改变状态
                    activity_status = PHOTO_PLAYING_STATUS;
                    firstClickButtonA = 0;
                }
                break;
            case R.id.pic_right_2d:
                hideSystemUI();
                if (buttonIsSHow) {
                    //                    Toast.makeText(this, "2D被点击", Toast.LENGTH_LONG)
                    //                         .show();

                    //                    if (mImgView2.getVisibility() == View.VISIBLE) {
                    //                        //如果本身就是2D的了  就什么都要做
                    //                        Log.d(TAG, "onClick: picIs2D" + picIs2D);
                    //                    } else if (mImgView2.getVisibility() == View.GONE) {
                    //                        //如果本身是3D的了  mImgView1 要miss掉  然后  miss掉 12一起出现
                    //                        Log.d(TAG, "onClick: !picIs2D" + !picIs2D);
                    //                        mImgView1.startAnimation(mFrom3DTo2DMissAnimation);
                    //
                    //                    }

                    updateButtonStatus(false);
                    myHandler.removeMessages(BUTTON_MISS);
                    //自己来改变状态
                    activity_status = PHOTO_PLAYING_STATUS;
                    firstClickButtonA = 0;
                }
                break;
            case R.id.pic_left_3d:
                hideSystemUI();

                if (buttonIsSHow) {
                    //                    Toast.makeText(this, "3D被点击", Toast.LENGTH_LONG)
                    //                         .show();
                    //
                    //                    if (mImgView2.getVisibility() == View.VISIBLE) {
                    //                        // 就要变3D
                    //                        //1 2 要miss掉  后gone 然后 1 出现重新加载
                    //                        mImgView1.startAnimation(mFrom2DTo3DMissAnimation);
                    //                        mImgView2.startAnimation(mFrom2DTo3DMissAnimation);
                    //
                    //
                    //                    } else if (mImgView2.getVisibility() == View.GONE) {
                    //                        //什么都不做
                    //                    }
                    updateButtonStatus(false);
                    myHandler.removeMessages(BUTTON_MISS);
                    activity_status = PHOTO_PLAYING_STATUS;
                    firstClickButtonA = 0;

                }
                break;
            case R.id.pic_right_3d:
                hideSystemUI();
                if (buttonIsSHow) {
                    //                    Toast.makeText(this, "3D被点击", Toast.LENGTH_LONG)
                    //                         .show();
                    //                    if (mImgView2.getVisibility() == View.VISIBLE) {
                    //                        // 就要变3D
                    //                        //1 2 要miss掉  后gone 然后 1 出现重新加载
                    //                        mImgView1.startAnimation(mFrom2DTo3DMissAnimation);
                    //                        mImgView2.startAnimation(mFrom2DTo3DMissAnimation);
                    //
                    //
                    //                    } else if (mImgView2.getVisibility() == View.GONE) {
                    //                        //什么都不做
                    //                    }
                    updateButtonStatus(false);
                    myHandler.removeMessages(BUTTON_MISS);
                    activity_status = PHOTO_PLAYING_STATUS;
                    firstClickButtonA = 0;

                }
                break;
            case R.id.imgView1:
                if (mLayout.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE) {
                    mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                } else {
                    mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
                //
                //                if (mLeft_3D.getVisibility() == View.VISIBLE) {
                //                    //如果是可见的  就让他不可见
                //                    mLeft_2D.setVisibility(View.INVISIBLE);
                //                    mLeft_3D.setVisibility(View.INVISIBLE);
                //                    mRight_2D.setVisibility(View.INVISIBLE);
                //                    mRight_3D.setVisibility(View.INVISIBLE);
                //                    buttonIsSHow = false;
                //                    hideSystemUI();
                //                } else {
                //                    updateSystemUI();
                //                    mLeft_2D.setVisibility(View.VISIBLE);
                //                    mLeft_3D.setVisibility(View.VISIBLE);
                //                    mRight_2D.setVisibility(View.VISIBLE);
                //                    mRight_3D.setVisibility(View.VISIBLE);
                //                    buttonIsSHow = true;
                ////                    if (mImgView2.getVisibility() == View.VISIBLE) {
                ////                        //如果是可见的说明是双福
                ////                        setButtonSelected(true);
                ////
                ////                    } else {
                ////                        //如果是不可见的
                ////                        setButtonSelected(false);
                ////                    }
                //
                //                }
                //
                break;
            //            case R.id.imgView2:
            //                if (mLeft_3D.getVisibility() == View.VISIBLE) {
            //                    //如果是可见的  就让他不可见
            //                    mLeft_2D.setVisibility(View.INVISIBLE);
            //                    mLeft_3D.setVisibility(View.INVISIBLE);
            //                    mRight_2D.setVisibility(View.INVISIBLE);
            //                    mRight_3D.setVisibility(View.INVISIBLE);
            //                    buttonIsSHow = false;
            //                    hideSystemUI();
            //                } else {
            //                    updateSystemUI();
            //
            //                    mLeft_2D.setVisibility(View.VISIBLE);
            //                    mLeft_3D.setVisibility(View.VISIBLE);
            //                    mRight_2D.setVisibility(View.VISIBLE);
            //                    mRight_3D.setVisibility(View.VISIBLE);
            //                    buttonIsSHow = true;
            ////                    if (mImgView2.getVisibility() == View.VISIBLE) {
            ////                        //如果是可见的说明是双福
            ////                        setButtonSelected(true);
            ////
            ////                    } else {
            ////                        //如果是不可见的
            ////                        setButtonSelected(false);
            ////                    }
            //                }
            //                break;


        }
    }

    private void hideSystemUI() {
        if (mLayout.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE) {
            mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }


    }

    private void updateSystemUI() {
        if (mLayout.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE) {
            mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }


    // 这个方法  是更新 在2D/3D状态器出来的时候button的背景色   即被选中的时候的为红色的更新
    //这里只负责背景颜色的更新  就是人机互动的更新
    private void setButtonSelected(boolean isLeft2D) {
        if (isLeft2D) {
            //如果左边被选中

            mLeft_2D.setBackgroundColor(Color.parseColor("#ff0000"));
            mLeft_3D.setBackgroundColor(Color.parseColor("#50000000"));

            mRight_2D.setBackgroundColor(Color.parseColor("#ff0000"));
            mRight_3D.setBackgroundColor(Color.parseColor("#50000000"));

        } else {
            //如果右边被选中
            mLeft_2D.setBackgroundColor(Color.parseColor("#50000000"));
            mLeft_3D.setBackgroundColor(Color.parseColor("#ff0000"));

            mRight_2D.setBackgroundColor(Color.parseColor("#50000000"));
            mRight_3D.setBackgroundColor(Color.parseColor("#ff0000"));
        }
        isLeftButton2D = isLeft2D;
    }

    private void setScreenLightMonitorVisible(boolean b) {
        if (b) {
            //让他出现
            mTvLight_left.setVisibility(View.VISIBLE);
            mTvLight_right.setVisibility(View.VISIBLE);
        } else {
            //让他消失
            mTvLight_left.setVisibility(View.GONE);
            mTvLight_right.setVisibility(View.GONE);

        }

    }

    //由于一些操作 需要状态吃一点消失  主要是看消息的what标记 和延迟的时间来确定
    private void delaySendMessager(int message, int time) {
        if (mHandler.hasMessages(message)) {
            mHandler.removeMessages(message);
        }
        //这里如果有操作 就延迟消失
        mHandler.sendEmptyMessageDelayed(message, time);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");

        //        setPathAndSurfaceView(mPicUrl.get(mCurrentPostion),
        //                              mSurfaceHolder.getSurface(),
        //                              3840,
        //                              1700,
        //                              INTER_AREA);
        setBitampResizeAndSurface(mPicUrl.get(mCurrentPostion));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public native void setPathAndSurfaceView(String path,
                                             Surface surface,
                                             int width,
                                             int height,
                                             int type);

    public void setSurfaceLayoutParams() {

        int                    height       = getHeight(mPicUrl.get(mCurrentPostion));
        ViewGroup.LayoutParams layoutParams = mImgView1.getLayoutParams();
        Log.d(TAG, "surfaceCreated: ;:" + layoutParams.width + "::::" + layoutParams.height);
     // Toast.makeText(this, height+"::", Toast.LENGTH_LONG).show();
        layoutParams.height = height;
        mImgView1.setLayoutParams(layoutParams);
    }

    private int getHeight(String path) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        //inJustDecodeBounds
        //If set to true, the decoder will return null (no bitmap), but the out…
        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, op); //获取尺寸信息
        //获取图片的比例大小
        Log.d(TAG, "decodeBitmap: " + op.outWidth + "::" + op.outHeight + "::" + op.inDensity);
        WindowManager  manager    = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay()
               .getMetrics(outMetrics);
        int width1 = manager.getDefaultDisplay()
                            .getWidth();
        int height1 = manager.getDefaultDisplay()
                             .getHeight();
        //获取系统的比例大小
        Log.d(TAG, "getHeight: "+width1 + "::" + height1);
        int   width  = outMetrics.widthPixels;
        int   height = outMetrics.heightPixels;

    //Toast.makeText(this , "++"+width + "::" + height,Toast.LENGTH_LONG).show();
        float width_f;
        if (width > 1700 && width <= 1920) {
            width_f = 1920;
        } else if(width >= 2300 && width <= 3000){
            width_f = 2560;
        }else{
            width_f = width;
        }

        Log.d(TAG, "decodeBitmap: " + width + "::" + height + "::" + width1 + "::" + height1);

        //图片的宽高比
        float ratio = (float) op.outWidth / (float) op.outHeight;

        //显示的高
        float dst_height = width_f / ratio;


        Log.d(TAG, "getHeight: " + dst_height);
        //1280.0

        return (int) dst_height;
    }


    public byte[] bitmapResize(String path) {

        int    height1    = getHeight(path);

        long   start_time = System.currentTimeMillis();
        //应该放开
       // Bitmap bm         = getBitmapData(path);
        Bitmap bm = BitmapFactory.decodeFile(path);
        // 获得图片的宽高  
        int width  = bm.getWidth();
        int height = bm.getHeight();

        // 设置想要的大小  
        int newHeight = height1;


        // 计算缩放比例  
        float scaleWidth  = ((float) 3840) / width;
        float scaleHeight = ((float) 1700) / height;

        // 取得想要缩放的matrix参数  
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片  
        Bitmap biamt = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        int   bitmapWidth = biamt.getWidth();
        int   bitmapHight = biamt.getHeight();
        int[] pixels      = new int[bitmapWidth * bitmapHight];
        biamt.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHight);

        Log.d(TAG, "bitmapResize: "+bitmapWidth + bitmapHight);
        biamt.recycle();
        bm.recycle();
//               byte[] newPixel = new byte[pixels.length * 4];
//               for (int i = 0; i < pixels.length; i++) {
//                   newPixel[i * 4] = (byte) Color.red(pixels[i]);
//                   newPixel[i * 4 + 1] = (byte) Color.green(pixels[i]);
//                   newPixel[i * 4 + 2] = (byte)  Color.blue(pixels[i]);
//                   newPixel[i * 4 + 3] = (byte) Color.alpha(pixels[i]);
//               }
       //应该放开
         byte[] newPixel = getNewPixels(pixels, bitmapWidth, bitmapHight);
        long   end_time = System.currentTimeMillis();
        Log.d(TAG, "bitmapResize: " + (end_time - start_time));
        return newPixel;
    }

    private Bitmap getBitmapData(String path) {
        Log.d(TAG, "加载 进入到加载到哪里" + mCurrentPostion +"::"+ mProPositionLeft +"::" + mProPositionRight);

        //先看是++ 还是 --
        if (mCurrentPostion == mProPositionLeft) {
            //说明是--
            if (mIsProLoadingLeft) {
                //说明--加载完毕
                return proLoadedBitmap[0];

            }else{
                return BitmapFactory.decodeFile(path);
            }
        } else if (mCurrentPostion == mProPositionRight) {
            //说明是++
            if (mIsProLoadingRight) {
                Log.d(TAG, "加载 进入到加载的右边");
                //说明++ 加载完毕
                return proLoadedBitmap[1];

            }else{
                return BitmapFactory.decodeFile(path);

            }
        } else {
            //说明什么都不是
            return BitmapFactory.decodeFile(path);
        }

    }

    //预加载 ：

    private boolean mIsProLoadingLeft  = false;
    private boolean mIsProLoadingRight = false;
    private int      mProPositionLeft;
    private int      mProPositionRight;
    private Bitmap[] proLoadedBitmap = new Bitmap[2];
    Thread proLoadingData = new Thread() {
        @Override
        public void run() {
            Log.d(TAG, "run: 加载"+ mCurrentPostion);
            if (mCurrentPostion == mPicUrl.size() - 1) {
                mProPositionRight = 0;
            } else {
                mProPositionRight = mCurrentPostion+1;
            }

            if (mCurrentPostion == 0) {
                mProPositionLeft = mPicUrl.size() - 1;
            } else {
                mProPositionLeft = mCurrentPostion-1;
            }

            Bitmap bitmapLeft = BitmapFactory.decodeFile(mPicUrl.get(mProPositionLeft));
            proLoadedBitmap[0] = bitmapLeft;
            Log.d(TAG, "run: 加载左边完成");
            mIsProLoadingLeft = true;
            Bitmap bitmapRight = BitmapFactory.decodeFile(mPicUrl.get(mProPositionRight));
            proLoadedBitmap[1] = bitmapRight;
            Log.d(TAG, "run: 加载右边完成");
            mIsProLoadingRight = true;


        }
    };

    public void proLoadingData() {
        if(mIsProLoadingLeft){
            proLoadedBitmap[0].recycle();
        }
        if(mIsProLoadingRight){
            proLoadedBitmap[1].recycle();
        }
        mIsProLoadingLeft = false;
        mIsProLoadingRight = false;
         if(proLoadingData!=null && proLoadingData.getState() == Thread.State.NEW){
             Log.d(TAG, "run: 加载: 第一次进来");
             proLoadingData.start();
         }else{
             proLoadingData = null;

               proLoadingData = new Thread() {
                 @Override
                 public void run() {
                     Log.d(TAG, "run: 加载"+ mCurrentPostion +"::"+(mPicUrl.size() - 1));
                     if (mCurrentPostion == mPicUrl.size() - 1) {
                         mProPositionRight = 0;
                     } else {
                         mProPositionRight = mCurrentPostion+1;
                     }

                     if (mCurrentPostion == 0) {
                         mProPositionLeft = mPicUrl.size() - 1;
                     } else {
                         mProPositionLeft = mCurrentPostion-1;
                     }

                     Bitmap bitmapLeft = BitmapFactory.decodeFile(mPicUrl.get(mProPositionLeft));
                     proLoadedBitmap[0] = bitmapLeft;
                     Log.d(TAG, "run: 加载左边完成");
                     mIsProLoadingLeft = true;
                     Bitmap bitmapRight = BitmapFactory.decodeFile(mPicUrl.get(mProPositionRight));
                     proLoadedBitmap[1] = bitmapRight;
                     Log.d(TAG, "run: 加载右边完成");
                     mIsProLoadingRight = true;


                 }
             };
                    proLoadingData.start();

         }

    }

    public void setBitampResizeAndSurface(String path) {
        //开始加载了
//应该放开
//        if(proLoadingData!=null && proLoadingData.getState() != Thread.State.NEW){
//            //正在运行
//            proLoadingData.interrupt();
//            Log.d(TAG, "setBitampResizeAndSurface:run: 加载 "+proLoadingData.getState());
//
//        }
        long start_time = System.currentTimeMillis();
        Log.d(TAG, "setBitampResizeAndSurface: ");
        byte[] newPixel = bitmapResize(path);

        setSurfaceView(mSurfaceHolder.getSurface(), newPixel, 3840, 1700);
        long end_time = System.currentTimeMillis();
        newPixel = null;
        Log.d(TAG, "setBitampResizeAndSurface: time = " + (end_time - start_time));
        //重置状态  重新让proLoading走起来
        //应该放开
       // proLoadingData();
    }

    public native void setSurfaceView(Surface surface, byte[] pixels, int width, int height);

    public native byte[] getNewPixels(int[] pixels, int width, int height);
}
