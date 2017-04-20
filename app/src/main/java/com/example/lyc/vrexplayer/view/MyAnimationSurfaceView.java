package com.example.lyc.vrexplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.view
 *  @文件名:   MyAnimationSurfaceView
 *  @创建者:   LYC2
 *  @创建时间:  2017/4/12 15:42
 *  @描述：    TODO
 */
public class MyAnimationSurfaceView extends SurfaceView implements SurfaceHolder.Callback   {
    private static final String TAG = "MyAnimationSurfaceView";
             private SurfaceHolder sfh;
             private Canvas canvas;
             private Paint paint;
             private Bitmap bmp;
             ///   
    private Animation mAlphaAnimation;
    private Animation mScaleAnimation;
    private Animation mTranslateAnimation;
    private Animation mRotateAnimation;
    private TranslateAnimation mLeftMissAnimation;

    public MyAnimationSurfaceView(Context context) {
         super(context);
          Log.v("Himi", "MySurfaceView");
          this.setKeepScreenOn(true);
          sfh = this.getHolder();
          sfh.addCallback(this);
          paint = new Paint();
          paint.setAntiAlias(true);
          setFocusable(true);
          setFocusableInTouchMode(true);
        //  this.setBackgroundResource(R.drawable.icon);//备注
    }

    public MyAnimationSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

     public void translateAnimation(){
         mLeftMissAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                                     0,
                                                     Animation.RELATIVE_TO_SELF,
                                                     0,
                                                     Animation.RELATIVE_TO_SELF,
                                                     0,
                                                     Animation.RELATIVE_TO_SELF,
                                                     -1);
         mLeftMissAnimation.setDuration(1000);
         mLeftMissAnimation.setFillAfter(false);
           this.startAnimation(mLeftMissAnimation);
     }
}
