package com.example.lyc.vrexplayer.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.Utils.LogUtils;
import com.example.lyc.vrexplayer.Utils.RxBus;
import com.example.lyc.vrexplayer.Utils.UserEvent;
import com.example.lyc.vrexplayer.adapter.DeviceAdapter;
import com.example.lyc.vrexplayer.broadcastreceiver.WifiDerectBroadcastReceiver;
import com.example.lyc.vrexplayer.glsurface.GLFrameRenderer;
import com.example.lyc.vrexplayer.glsurface.GLFrameSurface;
import com.example.lyc.vrexplayer.task.CameraDataReciverTask;
import com.example.lyc.vrexplayer.task.FileServerAsyncTask;
import com.example.lyc.vrexplayer.view.CameraPreview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

/*
 *  @项目名：  TestWifiDerect 
 *  @包名：    com.example.lyc2.testwifiderect.activity
 *  @文件名:   ServerActivity
 *  @创建者:   LYC2
 *  @创建时间:  2016/11/24 11:36
 *  @描述：    TODO
 */
public class ServerActivity
        extends AppCompatActivity
        implements View.OnClickListener
{


    private FileServerAsyncTask mServerTask;
    private Button              mBtn_recevier_data;
    private FrameLayout         mFl_surface_view;
    private CameraPreview       mPreview;
    private Camera              mCamera;
    private ImageView           mIv_camera;
    private ProgressDialog discoverProgressDialog   = null;
    private ProgressDialog connectingProgressDialog = null;
    private Subscription mSubscription;
    private TextView     mTv_recevie_file;

    private GLFrameSurface  mGLSurface;
    private GLFrameRenderer mGLFRenderer;
    Collection<WifiP2pDevice> mDeviceList;

    private void initView() {
        mRcyc_devices = (RecyclerView) findViewById(R.id.devices_server);
        mBtn_search = (Button) findViewById(R.id.search_device_server);
        mBtn_stop_connect = (Button) findViewById(R.id.stop_connect_server);
        mBtn_recevier_data = (Button) findViewById(R.id.recevie_data);

        mTv_recevie_file = (TextView) findViewById(R.id.tv_trans_file_server);


    }

    private static final String TAG = "ServerActivity";
    private RecyclerView                          mRcyc_devices;
    private Button                                mBtn_stop_connect;
    private Button                                mBtn_stop_search;
    private Button                                mBtn_search;
    private IntentFilter                          mFilter;
    private WifiP2pManager                        mManager;
    private WifiP2pManager.Channel                mChannel;
    private WifiDerectBroadcastReceiver           mReceiver;
    private WifiP2pManager.PeerListListener       mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private List<HashMap<String, String>>         mPeerLists;
    private WifiP2pInfo                           mInfo;
    private Button                                mBtn_become_server;
    private boolean isServer = true;
    private String beforeConnectDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        getSupportActionBar().setTitle("服务器（接受数据）");

        initView();
        initFilter();
        initReceiver();
        initEvent();
        initRxBus();
        initSufaceView();
        //初始化的时候吧服务器

        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
        //每一次都去 断开连接一下


    }

    private void initSufaceView() {
        // surfaceView的设置
        //        this.mSv_reviver_data.getHolder()
        //                             .setKeepScreenOn(true);
        //        this.mSv_reviver_data.getHolder()
        //                             .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //        this.mSv_reviver_data.getHolder()
        //                             .addCallback(new SurfaceCallback());
        //
//        mGLSurface = (GLFrameSurface) findViewById(R.id.gl_surface);
//
//        mGLSurface.setEGLContextClientVersion(2);
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//
//        getWindowManager().getDefaultDisplay()
//                          .getMetrics(displayMetrics);
//
//        mGLFRenderer = new GLFrameRenderer(mGLSurface, displayMetrics);
//
//        mGLSurface.setRenderer(mGLFRenderer);
//
//
//        mGLFRenderer.update(CameraUtil.CAMERA_WIDTH, CameraUtil.CAMERA_HEIGHT);

        String name = Thread.currentThread().getName();
        LogUtils.logInfo(TAG, "线程的名字", name+"。。");

    }

    String preTransFileName = null;

    private void initRxBus() {

        mSubscription = RxBus.getInstance()
                             .toObserverable(UserEvent.class)
                             .subscribe(new Action1<UserEvent>() {
                                 @Override
                                 public void call(final UserEvent userEvent) {
                                     long fileLength = 0;
                                     switch (userEvent.getName()) {
                                         case "disconnect":
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {

                                                     mBtn_recevier_data.setText("服务器状态:服务器已经被移除");
                                                     mBtn_search.setText("点击创建服务器");
                                                     mBtn_search.setClickable(true);
                                                 }
                                             });
                                             break;
                                         case "hasReceived":
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     if (userEvent.getFileName() != null) {
                                                         mTv_recevie_file.setText("传输文件的状态：传输完成:" + "文件为" + userEvent.getFileName());
                                                         preTransFileName = userEvent.getFileName();
                                                     } else {
                                                         mTv_recevie_file.setText("传输文件的状态：传输完成:");
                                                     }
                                                     //重新执行一次等待的任务
                                                     mServerTask = new FileServerAsyncTask();

                                                     mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                                                 }
                                             });


                                             break;
                                         case "serverReceiving":

                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {

                                                     mTv_recevie_file.setText("传输文件的状态：接受文件中");
                                                     //重新执行一次等待的任务
                                                 }
                                             });


                                             break;
                                         case "waitingConnect":

                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     if (preTransFileName != null) {
                                                         mTv_recevie_file.setText(
                                                                 "传输文件的状态：等待连接中(上一次传输完成文件为：" + preTransFileName + ")");
                                                     } else {
                                                         mTv_recevie_file.setText("传输文件的状态：等待连接中");

                                                     }
                                                 }
                                             });


                                             break;

                                     }
                                 }
                             });
    }


    private void initReceiver() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mPeerLists = new ArrayList<HashMap<String, String>>();
        //这个是申请到列表后的回调
        mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                LogUtils.logInfo(TAG,
                                 "onPeersAvailable",
                                 "::" + peers.getDeviceList()
                                             .size());
                if (mPeerLists != null) {
                    mPeerLists.clear();
                }

                if (discoverProgressDialog != null && discoverProgressDialog.isShowing()) {
                    discoverProgressDialog.dismiss();
                }

                //这个是申请到列表后的回调
                mDeviceList = peers.getDeviceList();
                for (WifiP2pDevice wifiP2pDevice : mDeviceList) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", wifiP2pDevice.deviceName);
                    map.put("address", wifiP2pDevice.deviceAddress);
                    mPeerLists.add(map);
                }

                DeviceAdapter adapter = new DeviceAdapter(mPeerLists, isServer);
                mRcyc_devices.setAdapter(adapter);
                mRcyc_devices.setLayoutManager(new LinearLayoutManager(ServerActivity.this));
                adapter.setOnItemButtonClickConnectListener(new DeviceAdapter.ItemButtonClickConnectListener() {
                    @Override
                    public void onItemButtonClickConnectListener(int position) {
                        //条目被点击的时候  需要连接
                        //                        LogUtils.logInfo(TAG,
                        //                                         "onItemButtonClickConnectListener",
                        //                                         mPeerLists.get(position)
                        //                                                   .get("name"));
                        //                        createConnet(mPeerLists.get(position)
                        //                                               .get("name"),
                        //                                     mPeerLists.get(position)
                        //                                               .get("address"));
                    }
                });
            }
        };

        //这是申请连接后的回调
        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                //这是申请连接后的回调
                mInfo = info;

                LogUtils.logInfo(TAG,
                                 "onConnectionInfoAvailable",
                                 mInfo.groupOwnerAddress + "info.isGroupOwner" + info.isGroupOwner);

                ///192.168.49.1info.isGroupOwnertrue

                if (info.groupFormed && info.isGroupOwner) {

                    //说明是服务端
                    LogUtils.logInfo(TAG, "onConnectionInfoAvailable", "说明是服务器  接受数据");

                    mServerTask = new FileServerAsyncTask();

                    mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    // TODO: 2016/12/15 等待做一个接受  告诉服务器本身 申请连接的是哪一个


                    CameraDataReciverTask cameraDataReciverTask = new CameraDataReciverTask(
                            mGLFRenderer);
                    //// TODO: 2016/12/21

                    cameraDataReciverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    if (mDeviceList == null) {
                        mBtn_recevier_data.setText("服务器状态:已经创建好接收端（等待连接）:" + info.groupOwnerAddress);
                    } else {
                        for (WifiP2pDevice wifiP2pDevice : mDeviceList) {

                            if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                                beforeConnectDevice = wifiP2pDevice.deviceName;
                                //说明这个连接到了：
                                mBtn_recevier_data.setText("服务器状态:连接成功，服务器（IP）:" + info.groupOwnerAddress + "申请连接设备：" + wifiP2pDevice.deviceName);
                                Log.d(TAG,
                                      "onConnectionInfoAvailable: " + wifiP2pDevice.deviceName + "::" + wifiP2pDevice.status);


                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBtn_recevier_data.performClick();
                                    }
                                }, 500);

                                return;
                            }
                        }
                        //到了这里说明  一个也没有连接上
                        mBtn_recevier_data.setText("服务器状态:已经创建好服务器（等待连接）:" + info.groupOwnerAddress);


                    }
                mBtn_search.setText("服务器已经创建");
                    mBtn_search.setClickable(false);

                } else if (info.groupFormed) {

                    //说明是客户端


                    LogUtils.logInfo(TAG, "onConnectionInfoAvailable", "说明是客户端  发送数据");

                }


            }
        };
        mReceiver = new WifiDerectBroadcastReceiver(mManager,
                                                    mChannel,
                                                    this,
                                                    mPeerListListener,
                                                    mConnectionInfoListener);

    }

    private void createConnet(String name, final String address) {
        //点击 要创建连接
        //        WifiP2pDevice device;
        //        WifiP2pConfig       config = new WifiP2pConfig();
        //        Log.i("xyz", address);
        //
        //        config.deviceAddress = address;
        //        /*mac地址*/
        //
        //        config.wps.setup = WpsInfo.PBC;
        //


        if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
            connectingProgressDialog.dismiss();
        }
        connectingProgressDialog = ProgressDialog.show(this, "连接设备", "连接中:" + address, true, true,
                                                       // cancellable
                                                       new DialogInterface.OnCancelListener() {
                                                           @Override
                                                           public void onCancel(DialogInterface dialog)
                                                           {
                                                               mBtn_stop_connect.performClick();
                                                           }
                                                       });

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 15;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
                    connectingProgressDialog.dismiss();
                }
                Toast.makeText(ServerActivity.this, "连接成功", Toast.LENGTH_SHORT)
                     .show();

                mBtn_recevier_data.setText("主动接受数据:连接到了P2P:" + address);

                Log.d(TAG, "onSuccess: ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: ");
                Toast.makeText(ServerActivity.this, "连接失败", Toast.LENGTH_SHORT)
                     .show();

            }
        });


    }

    private void initFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    private void initEvent() {
        mBtn_stop_connect.setOnClickListener(this);
        mBtn_search.setOnClickListener(this);


        mBtn_recevier_data.setOnClickListener(this);


        // handler.postDelayed(runnable, 1000 * 60);
        mBtn_recevier_data.setText("主动接受数据:尚未连接到P2P");
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);

    }

    @Override
    public void onPause() {
        super.onPause();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: zheli youmeiyou ");


        unregisterReceiver(mReceiver);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
 
            case R.id.search_device_server:
                if (discoverProgressDialog != null && discoverProgressDialog.isShowing()) {
                    discoverProgressDialog.dismiss();

                }
                Log.d(TAG, "onClick: 创建服务器");

mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
    @Override
    public void onSuccess() {
        Log.d(TAG, "onSuccess: chenggong");

    }

    @Override
    public void onFailure(int reason) {
        Log.d(TAG, "onFailure: 失败"+reason);

    }
});



                break;

            case R.id.stop_connect_server:
                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });
                break;

            case R.id.recevie_data:
                //                CameraDataReciverTask cameraDataReciverTask = new CameraDataReciverTask(mGLFRenderer);
                //                cameraDataReciverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                //检查一下状态

                for (WifiP2pDevice wifiP2pDevice : mDeviceList) {
                    if (wifiP2pDevice.deviceName.equals(beforeConnectDevice)) {

                        if(wifiP2pDevice.status == WifiP2pDevice.CONNECTED){
                            return;
                        }else{
                            mBtn_recevier_data.setText("服务器状态:已经创建好服务器（等待连接）:" + mInfo.groupOwnerAddress);



                            return;


                        }



                    }
                }


                break;
            default:
                break;
        }
    }

    Handler  handler  = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {

                handler.postDelayed(this, 1000 * 60);

                mBtn_search.performClick();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                LogUtils.logInfo(TAG, "run()", "定时器出了问题");
            }
        }
    };

    MediaPlayer mp = new MediaPlayer();

    private class SurfaceCallback
            implements SurfaceHolder.Callback
    {
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {

        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // 界面销毁，即将跳转到另外一个界面
            if (mp.isPlaying()) {
                mp.stop();
            }
        }

    }

    public boolean onError(MediaPlayer mp, int what, int extra) {

        return false;
    }


    public class SureDevicesStatus
            implements Runnable
    {
        private WifiP2pDevice mWifiDevice;
        private WifiP2pInfo   mWifiInfo;

        public SureDevicesStatus() {}

        public SureDevicesStatus(WifiP2pDevice wifidevice, WifiP2pInfo wifiInfo) {
            this.mWifiDevice = wifidevice;
            this.mWifiInfo = wifiInfo;
        }

        @Override
        public void run() {
            LogUtils.logInfo(TAG, "看看这个设备的状态有没有变换", "status=" + mWifiDevice.status);
            if (mWifiDevice.status == WifiP2pDevice.CONNECTED) {
                return;
            } else {
                mBtn_recevier_data.setText("服务器状态:已经创建好服务器（等待连接）:" + mWifiInfo.groupOwnerAddress);
           //这个状态如果做了改变就是已经离开了




            }


        }
    }

}