package com.example.lyc.vrexplayer.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ScrollView;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.view
 *  @文件名:   ObservableScrollView
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/13 18:05
 *  @描述：    TODO
 */
public class ObservableScrollView
        extends ScrollView
{
    private static final String TAG = "ObservableScrollView";

    private ScrollViewListener scrollViewListener = null;
    private ScrollViewKeydownListener mScrollViewKeydownListener;
    private int mOrientation;
    public ObservableScrollView(Context context) {
        super(context);
    }

    public void setOrientation(int initOrientation) {
        mOrientation = initOrientation;

    }

    public interface ScrollViewListener {
        void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface ScrollViewKeydownListener {
        void onScrollViewKeydownChanged(int keyCode);
    }

    public void setOnScrollViewKeydownListener(ScrollViewKeydownListener scrollViewKeydownListener) {
        this.mScrollViewKeydownListener = scrollViewKeydownListener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: :::::::上"+keyCode);
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    Log.d(TAG, "onKeyDown: :::::::上");
                    if (mScrollViewKeydownListener != null) {
                        Log.d(TAG, "onKeyDown: diaoyong");
                        mScrollViewKeydownListener.onScrollViewKeydownChanged(keyCode);
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    Log.d(TAG, "onKeyDown: :::::::下");
                    if (mScrollViewKeydownListener != null) {
                        mScrollViewKeydownListener.onScrollViewKeydownChanged(keyCode);
                    }
                    return true;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    Log.d(TAG, "onKeyDown: :::::::左");
                    if (mScrollViewKeydownListener != null) {
                        mScrollViewKeydownListener.onScrollViewKeydownChanged(keyCode);
                    }
                    return true;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    Log.d(TAG, "onKeyDown: :::::::右");
                    if (mScrollViewKeydownListener != null) {
                        mScrollViewKeydownListener.onScrollViewKeydownChanged(keyCode);
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if (mScrollViewKeydownListener != null) {
                        mScrollViewKeydownListener.onScrollViewKeydownChanged(keyCode);
                    }
                    return true;
            }
        return super.onKeyDown(keyCode, event);
    }
}
