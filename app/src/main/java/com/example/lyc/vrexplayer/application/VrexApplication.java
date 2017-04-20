package com.example.lyc.vrexplayer.application;

import android.app.Application;
import android.content.Context;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.application
 *  @文件名:   VrexApplication
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/13 17:22
 *  @描述：    TODO
 */
public class VrexApplication
        extends Application
{
    private static final String TAG = "VrexApplication";
    private static Context mContext;
    public static int SERVER_CLIENT_PORT = 8898;
    public static int POINT = 1;
    public static int mInitOrientation;

    public static Context getContext() {
        return getContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }
}
