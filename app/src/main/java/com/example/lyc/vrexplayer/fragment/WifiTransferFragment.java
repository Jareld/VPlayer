package com.example.lyc.vrexplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.activity.ClientActivity;
import com.example.lyc.vrexplayer.activity.MainActivity;
import com.example.lyc.vrexplayer.activity.ServerActivity;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.fragment
 *  @文件名:   WifiTransferFragment
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/13 15:08
 *  @描述：    TODO
 */
public class WifiTransferFragment extends Fragment
        implements View.OnClickListener
{
    private static final String TAG = "WifiTransferFragment";
    private   Context mContext;
    private View mView;
    private Button mBtnBecomeClien;
    private Button mBtnBecomeServer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_wifi_transfer, container, false);
        return mView;
    }

    public void configurationChange(int oritation){
        switch (oritation) {
            case MainActivity.ORIENTATION_PROTRAIT:
                Log.d(TAG, "wificonfigurationChange: 竖屏");
                break;
            case MainActivity.ORIENTATION_LANDSCAPE:
                Log.d(TAG, "wificonfigurationChange: 横屏");
                break;
            default:
                break;
        }
    }

    public void setInitOrietation(int initOrientation) {

    }
    @Override
    public void onStart() {
        Log.i(TAG, "==onStart()执行了");
        super.onStart();
        initView();
        initEvent();

    }

    private void initEvent() {
        mBtnBecomeClien.setOnClickListener(this);
        mBtnBecomeServer.setOnClickListener(this);

    }

    private void initView() {
        mBtnBecomeClien = (Button) mView.findViewById(R.id.btn_wifi_transfer_client);
        mBtnBecomeServer = (Button) mView.findViewById(R.id.wifi_transfer_server);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_wifi_transfer_client:
                startForActivity(ClientActivity.class);
                break;
            case R.id.wifi_transfer_server:
                startForActivity(ServerActivity.class);
                break;
        }
    }
    private void startForActivity(Class clazz) {
        Intent intent = new Intent(MainActivity.mContext, clazz);
        startActivity(intent);
    }
}
