package com.example.lyc.vrexplayer.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.lyc.vrexplayer.service.CameraDataTrandsferService;
import com.example.lyc.vrexplayer.service.FileTransferService;
import com.example.lyc.vrexplayer.view.CameraPreview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action1;

/*
 *  @项目名：  TestWifiDerect 
 *  @包名：    com.example.lyc2.testwifiderect.activity
 *  @文件名:   ClientActivity
 *  @创建者:   HY
 *  @创建时间:  2016/11/24 11:38
 *  @描述：    主要作为客户端去发送信息
 */
public class ClientActivity
        extends AppCompatActivity
        implements View.OnClickListener
{
    private static final String TAG = "ClientActivity";
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
    private Button                                mBtn_send_file;
    private FrameLayout                           mFl_surface_view;
    private Camera                                mCamera;
    private CameraPreview                         mPreview;
    private Button                                mBtn_start_preview;
    private ImageView                             mIv_test_camera;
    private Subscription                          mSubscription;
    private TextView                              mTv_trans_file;
    private ProgressDialog discoverProgressDialog   = null;
    private ProgressDialog connectingProgressDialog = null;
    private boolean        mIsFromCreateConnect     = false;
    private boolean        isServer                 = false;
    private static String mBeClickedDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        getSupportActionBar().setTitle("客户端（发送数据）");
        Log.d(TAG, "onCreate: Jareld");

        initView();
        initFilter();
        initReceiver();
        initEvent();
        initRxBus();

    }

    private void initRxBus() {


        mSubscription = RxBus.getInstance()
                             .toObserverable(UserEvent.class)
                             .subscribe(new Action1<UserEvent>() {
                                 @Override
                                 public void call(UserEvent userEvent) {
                                     float fileLength = 0;
                                     switch (userEvent.getName()) {
                                         case "before":

                                             fileLength = userEvent.getProgress();
                                             final float finalFileLength = fileLength;
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     mTv_trans_file.setText("准备输送文件：文件大小：" + finalFileLength / 1024 / 1024);


                                                 }
                                             });
                                             break;
                                         case "after":
                                             final float fileleth = userEvent.getProgress();
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     mTv_trans_file.setText("传送文件完成:" + fileleth);
                                                 }
                                             });

                                             break;
                                         case "doing":
                                             //  float per = (float) userEvent.getProgress() / (float) fileLength;
                                             final float finalPer = userEvent.getProgress();
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     mTv_trans_file.setText("传送文件ing" + finalPer +"MB");

                                                 }
                                             });
                                             break;
                                         case "disconnect":
                                             if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
                                                 connectingProgressDialog.dismiss();

                                             }
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                    // mBtn_start_preview.setText("开始预览Camera:失去了连接");
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
                Collection<WifiP2pDevice> deviceList = peers.getDeviceList();
                for (WifiP2pDevice wifiP2pDevice : deviceList) {

                    HashMap<String, String> map = new HashMap<>();

                    map.put("name", wifiP2pDevice.deviceName);

                    map.put("address", wifiP2pDevice.deviceAddress);

                    mPeerLists.add(map);

                }

                DeviceAdapter adapter = new DeviceAdapter(mPeerLists, isServer);
                mRcyc_devices.setAdapter(adapter);
                mRcyc_devices.setLayoutManager(new LinearLayoutManager(ClientActivity.this));
                adapter.setOnItemButtonClickConnectListener(new DeviceAdapter.ItemButtonClickConnectListener() {


                    @Override
                    public void onItemButtonClickConnectListener(int position) {
                        //条目被点击的时候  需要连接
                        LogUtils.logInfo(TAG,
                                         "onItemButtonClickConnectListener",
                                         mPeerLists.get(position)
                                                   .get("name") + mPeerLists.get(position)
                                                                            .get("address"));

                        mBeClickedDeviceName = mPeerLists.get(position)
                                                         .get("name");


                        createConnet(mPeerLists.get(position)
                                               .get("name"),
                                     mPeerLists.get(position)
                                               .get("address"));


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
                                 mInfo.groupOwnerAddress + "info.isGroupOwner" + info.isGroupOwner + "::");
                ///192.168.49.1info.isGroupOwner/flase
                //这是申请连接后的回调  说明已经连接上了

                if (info.groupFormed && info.isGroupOwner) {
                    //说明是服务端
                    LogUtils.logInfo(TAG, "onConnectionInfoAvailable", "说明是服务器  接受数据");

                } else if (info.groupFormed) {
                    //说明是客户端
                    LogUtils.logInfo(TAG, "onConnectionInfoAvailable", "说明是客户端  发送数据");

                    if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
                        connectingProgressDialog.dismiss();
                    }
                    if (mIsFromCreateConnect) {
                        //如果是从头开始连接的 就提示一下的
                        Toast.makeText(ClientActivity.this, "连接成功", Toast.LENGTH_SHORT)
                             .show();
                        mIsFromCreateConnect = false;
                    }
                    //mBtn_start_preview.setText("开始预览Camera:连接到了P2P:连接到了：" + mBeClickedDeviceName);

                    //发送一条信息过去
                    // TODO: 2016/12/15 等待一个发送信息的socket告诉 服务器 连接的是什么


                }


            }
        };

        mReceiver = new WifiDerectBroadcastReceiver(mManager,
                                                    mChannel,
                                                    this,
                                                    mPeerListListener,
                                                    mConnectionInfoListener);
        mManager.setDnsSdResponseListeners(mChannel,
                                           new WifiP2pManager.DnsSdServiceResponseListener() {
                                               @Override
                                               public void onDnsSdServiceAvailable(String instanceName,
                                                                                   String registrationType,
                                                                                   WifiP2pDevice srcDevice)
                                               {
                                                   LogUtils.logInfo(TAG,
                                                                    "onDnsSdServiceAvailable",
                                                                    "instanceName=" + instanceName + "registrationType=" + registrationType + "WifiP2pDevice=" + srcDevice);
                                               }
                                           },
                                           new WifiP2pManager.DnsSdTxtRecordListener() {
                                               @Override
                                               public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                                                                     Map<String, String> txtRecordMap,
                                                                                     WifiP2pDevice srcDevice)
                                               {
                                                   LogUtils.logInfo(TAG,
                                                                    "onDnsSdServiceAvailable",
                                                                    "fullDomainName=" + fullDomainName + "txtRecordMap=" + txtRecordMap + "WifiP2pDevice=" + srcDevice);

                                               }
                                           });
        mManager.setServiceResponseListener(mChannel, new WifiP2pManager.ServiceResponseListener() {
            @Override
            public void onServiceAvailable(int protocolType,
                                           byte[] responseData,
                                           WifiP2pDevice srcDevice)
            {
                LogUtils.logInfo(TAG,
                                 "onServiceAvailable",
                                 "protocolType=" + protocolType + "responseData=" + responseData + "WifiP2pDevice=" + srcDevice);

            }
        });

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

        mIsFromCreateConnect = true;


        if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
            connectingProgressDialog.dismiss();
        }
        connectingProgressDialog = ProgressDialog.show(this, "连接设备", "连接中 :" + address, true, true,
                                                       // cancellable
                                                       new DialogInterface.OnCancelListener() {
                                                           @Override
                                                           public void onCancel(DialogInterface dialog)
                                                           {
                                                               mBtn_stop_connect.performClick();
                                                           }
                                                       });
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                LogUtils.logInfo(TAG, "onSuccess: ", "连接成功了");


            }

            @Override
            public void onFailure(int reason) {
                LogUtils.logInfo(TAG, "onSuccess: ", "连接失败了");

                Toast.makeText(ClientActivity.this, "连接失败", Toast.LENGTH_SHORT)
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
        mBtn_stop_search.setOnClickListener(this);
        mBtn_send_file.setOnClickListener(this);
       // mBtn_start_preview.setOnClickListener(this);
        //   handler.postDelayed(runnable, 1000 * 5);
    }

    private void initView() {
        mRcyc_devices = (RecyclerView) findViewById(R.id.devices);
        mBtn_search = (Button) findViewById(R.id.search_device);
        mBtn_stop_search = (Button) findViewById(R.id.stop_search_device);
        mBtn_stop_connect = (Button) findViewById(R.id.stop_connect);
        mBtn_send_file = (Button) findViewById(R.id.send_file);
       // mFl_surface_view = (FrameLayout) findViewById(R.id.fl_sufuce_view);
      //  mBtn_start_preview = (Button) findViewById(R.id.start_preview);
      //  mIv_test_camera = (ImageView) findViewById(R.id.iv_test_camera);
        mTv_trans_file = (TextView) findViewById(R.id.tv_trans_file_client);
        //mBtn_start_preview.setText("开始预览Camera:尚未连接到P2P");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
//
//        mCamera = Camera.open(0);
//
//        FileServerAsyncTask fileServerAsyncTask = new FileServerAsyncTask();
//        mPreview = new CameraPreview(this, mCamera, fileServerAsyncTask);
//
//        mFl_surface_view.addView(mPreview);
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mCamera != null) {
//            mFl_surface_view.removeView(mPreview);
//            mPreview = null;
//            mCamera.release();
//            mCamera = null;
//        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        CameraDataTrandsferService.closeSocket();
        mBtn_stop_connect.performClick();

        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_device:
                if (discoverProgressDialog != null && discoverProgressDialog.isShowing()) {
                    discoverProgressDialog.dismiss();
                }
                discoverProgressDialog = ProgressDialog.show(this, "搜索设备", "搜索中......:", true, true,
                                                             // cancellable
                                                             new DialogInterface.OnCancelListener() {
                                                                 @Override
                                                                 public void onCancel(
                                                                         DialogInterface dialog)
                                                                 {
                                                                     mBtn_stop_search.performClick();
                                                                 }
                                                             });

                //start search device it can call wifireceiver :WIFI_P2P_DISCOVERY_CHANGED_ACTION
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });

                break;
            case R.id.stop_search_device:
                mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });

                break;
            case R.id.stop_connect:
                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });

                break;

            case R.id.send_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, 20);
                break;
//            case R.id.start_preview:
////                mCamera.startPreview();
////                //启动发送的任务
////                Intent cameraDataTrandsferService = new Intent(ClientActivity.this,
////                                                               CameraDataTrandsferService.class);
////
////                cameraDataTrandsferService.putExtra(CameraDataTrandsferService.EXTRAS_GROUP_OWNER_ADDRESS,
////                                                    mInfo.groupOwnerAddress.getHostAddress());
////
////                cameraDataTrandsferService.putExtra(CameraDataTrandsferService.EXTRAS_GROUP_OWNER_PORT,
////                                                    10086);
////               // CameraDataTrandsferService.setImageView(mIv_test_camera);
////                ClientActivity.this.startService(cameraDataTrandsferService);
//                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtils.logInfo(TAG, "run", "进行到了这里requestCode");
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 20:
                super.onActivityResult(requestCode, resultCode, data);
//                new Thread() {
//                    @Override
//                    public void run() {
//                        super.run();
//                        LogUtils.logInfo(TAG,
//                                         "run",
//                                         mInfo.groupOwnerAddress.getHostName() + "::" + mInfo.groupOwnerAddress.getCanonicalHostName());
//                    }
//                }.start();

                LogUtils.logInfo(TAG, "run", "进行到了这里");

                Uri uri = data.getData();

                Intent serviceIntent = new Intent(ClientActivity.this, FileTransferService.class);

                String realFilePath = getRealFilePath(this, uri);



                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

                serviceIntent.putExtra(FileTransferService.REAL_FILE_PATH, realFilePath);
                Log.d(TAG, "onActivityResult:realFilePath "+realFilePath+"::"+uri.getPath());
                serviceIntent.putExtra(FileTransferService.BE_CLICKED_DEVICE_NAME,mBeClickedDeviceName);

                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                       mInfo.groupOwnerAddress.getHostAddress());

                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);

                ClientActivity.this.startService(serviceIntent);
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
                handler.postDelayed(this, 1000 * 5);

                mBtn_search.performClick();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                LogUtils.logInfo(TAG, "run()", "定时器出了问题");
            }
        }
    };

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) { return null; }
        final String scheme = uri.getScheme();
        String       data   = null;
        if (scheme == null) { data = uri.getPath(); } else if (ContentResolver.SCHEME_FILE.equals(
                scheme))
        {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver()
                                   .query(uri,
                                          new String[]{MediaStore.Video.Media.DISPLAY_NAME},
                                          null,
                                          null,
                                          null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
