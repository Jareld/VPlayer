package com.example.lyc.vrexplayer.Utils;

import android.content.Context;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.Utils
 *  @文件名:   DpiPxUtil
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/14 13:45
 *  @描述：    TODO
 */
public class DpiPxUtil {
    private static final String TAG = "DpiPxUtil";
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources()
                                   .getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /** 
          * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
          */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources()
                                   .getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
