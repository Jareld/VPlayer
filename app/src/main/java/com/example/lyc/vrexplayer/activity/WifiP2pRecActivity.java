package com.example.lyc.vrexplayer.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.Utils.RxBus;
import com.example.lyc.vrexplayer.Utils.UserEvent;
import com.example.lyc.vrexplayer.broadcastreceiver.WifiDerectBroadcastReceiver;
import com.example.lyc.vrexplayer.task.FileServerAsyncTask;
import com.example.lyc.vrexplayer.view.FlikerProgressBar;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

public class WifiP2pRecActivity
        extends AppCompatActivity
        implements View.OnClickListener
{

    private static final String TAG = "wifip2precactivity";
    private View                                  mLayout;
    private IntentFilter                          mFilter;
    private WifiP2pManager                        mManager;
    private WifiP2pManager.Channel                mChannel;
    private WifiDerectBroadcastReceiver           mReceiver;
    private WifiP2pManager.PeerListListener       mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private List<HashMap<String, String>>         mPeerLists;
    private WifiP2pInfo                           mInfo;
    private Button                                mBtn_reset;
    private TextView                              mTv_waiting_for_server;
    private TextView                              mTv_rec_file;
    Handler handler = new Handler();
    private FileServerAsyncTask       mServerTask;
    private Subscription              mSubscription;
    private Collection<WifiP2pDevice> mDeviceList;
    private HashMap<String, WifiP2pDevice> mAllDeviceList   = new HashMap<>();
    private String                         preTransFileName = null;
    private        String            beforeConnectDevice;
    private static String            mAbsolutePath;
    private        String            mBeforeConnectName;
    private        FlikerProgressBar mRec_progress;
    private static final int     RESTART_REC          = 1;
    private static final int     NOT_FINISHED_RESTART = 2;
    private static final int     MISS_PROGRESS        = 3;
    private              Handler mHandler             = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESTART_REC:
                    if (mServerTask != null) {
                        if (!mServerTask.getIsAccept()) {
                            Log.d(TAG, "startServerTask: 任务还没accept 说明已经启动了任务");

                        } else if (mServerTask.getStatus() == AsyncTask.Status.FINISHED) {
                            Log.d(TAG, "startServerTask: 任务 finished" + mServerTask.getStatus());
                            mServerTask = new FileServerAsyncTask();
                            mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                    }

                    break;
                case NOT_FINISHED_RESTART:
                    mServerTask = new FileServerAsyncTask();
                    mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case MISS_PROGRESS:
                    mRec_progress.setVisibility(View.GONE);
                    break;
            }

            super.handleMessage(msg);
        }
    };
    private boolean mIsFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().getDecorView()
                   .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_wifi_p2p_rec, null);
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
        initWifiP2p();
        initRxBus();

    }

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
                                                     mTv_waiting_for_server.setText(
                                                             "接收端已经准备好（等待发送端连接）:");
                                                 }
                                             });
                                             break;
                                         case "hasReceived":
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     if (mTv_rec_file.getVisibility() == View.GONE) {
                                                         mTv_rec_file.setVisibility(View.VISIBLE);
                                                     }
                                                     if (mRec_progress.getVisibility() == View.VISIBLE) {
                                                         mRec_progress.reset();
                                                         Log.d(TAG, "run: reset之后在gone");
                                                         mRec_progress.setVisibility(View.GONE);
                                                     }

                                                     if (userEvent.getFileName() != null) {
                                                         mTv_rec_file.setText("传输文件的状态：传输完成:" + "文件为" + userEvent.getFileName());
                                                         preTransFileName = userEvent.getFileName();
                                                     } else {
                                                         mTv_rec_file.setText("传输文件的状态：传输完成:");
                                                     }
                                                     //重新执行一次等待的任务

                                                     Log.d(TAG,
                                                           "run: 任务" + mServerTask.getStatus());

                                                     mHandler.sendEmptyMessageDelayed(RESTART_REC,
                                                                                      30);

                                                     String filename = userEvent.getFileName();
                                                     sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                                                              Uri.fromFile(new File(
                                                                                      Environment.getExternalStorageDirectory() + "/" + "Samson/Wifi-Tranfer/" + filename))));


                                                 }
                                             });


                                             break;
                                         case "serverReceiving":

                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {

                                                     mTv_rec_file.setText("传输文件的状态：接受文件中");

                                                 }
                                             });


                                             break;
                                         case "waitingConnect":

                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     if (mTv_rec_file.getVisibility() == View.GONE) {
                                                         mTv_rec_file.setVisibility(View.VISIBLE);
                                                     }
                                                     if (mRec_progress.getVisibility() == View.VISIBLE) {
                                                         mRec_progress.setVisibility(View.GONE);
                                                     }
                                                     if (preTransFileName != null) {
                                                         mTv_rec_file.setText(
                                                                 "传输文件的状态：等待传输中(上一次传输完成文件为：" + preTransFileName + ")");
                                                     } else {
                                                         mTv_rec_file.setText("传输文件的状态：等待传输中");

                                                     }
                                                 }
                                             });
                                             break;
                                         case "doing":
                                             handler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     if (mTv_rec_file.getVisibility() == View.VISIBLE) {
                                                         mTv_rec_file.setVisibility(View.GONE);
                                                     }
                                                     if (mRec_progress.getVisibility() == View.GONE) {
                                                         mRec_progress.setVisibility(View.VISIBLE);
                                                     }
                                                     float progress = (userEvent.getProgress() * 100 / userEvent.getFileLengthMB());
                                                     Log.d(TAG, "run: " + progress);
                                                     BigDecimal b = new BigDecimal(progress);
                                                     progress = b.setScale(1,
                                                                           BigDecimal.ROUND_HALF_UP)
                                                                 .floatValue();
                                                     mRec_progress.setProgress(progress);

                                                 }
                                             });
                                         case "WIFI_P2P_PEER_CHANGE":


                                             break;
                                         case "connect_fail":
                                             Toast.makeText(getApplicationContext(),
                                                            "启动文件传输失败 ，请重新发送文件",
                                                            Toast.LENGTH_SHORT)
                                                  .show();
                                             break;

                                     }
                                 }
                             });
    }

    private void initWifiP2p() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mPeerLists = new ArrayList<HashMap<String, String>>();
        //这个是申请到列表后的回调
        mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Log.d(TAG, "onPeersAvailable: 步骤1 搜索到设备");
                //搜索到的设备
                if (mPeerLists != null) {
                    mPeerLists.clear();
                }

                mDeviceList = peers.getDeviceList();
                if (mIsFromServer) {
                    Log.d(TAG, "onPeersAvailable: 步骤  进入到服务器进入的搜索");
                    if (mDeviceList == null) {
                        mTv_waiting_for_server.setText("接收端已经准备好（等待发送端连接）");
                    } else {

                        for (WifiP2pDevice wifiP2pDevice : mDeviceList) {
                            Log.d("Jareld ", "onConnectionInfoAvailable: " + wifiP2pDevice.status);
                            if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                                beforeConnectDevice = wifiP2pDevice.deviceName;
                                //说明这个连接到了：
                                mTv_waiting_for_server.setText("连接成功------" + "已连接至设备：" + wifiP2pDevice.deviceName);
                                startServerTask();
                                return;
                            }
                        }
                        //到了这里说明  一个也没有连接上
                        if (mRec_progress.getVisibility() == View.VISIBLE) {
                            mRec_progress.reset();
                            mRec_progress.setVisibility(View.GONE);
                        } else if (mTv_rec_file.getVisibility() == View.GONE) {
                            mTv_rec_file.setVisibility(View.VISIBLE);
                            mTv_rec_file.setText("等待接受文件");
                        }
                        mTv_waiting_for_server.setText("接收端已经准备好（等待发送端连接）:");
                    }
                    mIsFromServer = false;
                }

            }
        };
        //这是申请连接后的回调
        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                mInfo = info;


                ///192.168.49.1info.isGroupOwnertrue

                if (info.groupFormed && info.isGroupOwner) {
                    Log.d(TAG, "Jareld : 服务端");
                    mIsFromServer = true;

                    //说明是服务端
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(int i) {

                        }
                    });

                    if (mDeviceList == null) {
                        mTv_waiting_for_server.setText("接收端已经准备好（等待发送端连接）");
                    } else {
                        for (WifiP2pDevice wifiP2pDevice : mDeviceList) {
                            Log.d("Jareld ", "onConnectionInfoAvailable: " + wifiP2pDevice.status);
                            if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                                beforeConnectDevice = wifiP2pDevice.deviceName;
                                //说明这个连接到了：
                                mTv_waiting_for_server.setText("连接成功------" + "已连接至设备：" + wifiP2pDevice.deviceName);

                                startServerTask();
                                return;
                            }
                        }
                        //到了这里说明  一个也没有连接上
                        mTv_waiting_for_server.setText("接收端已经准备好（等待发送端连接）:");
                    }

                } else if (info.groupFormed) {

                    //说明是客户端


                }

            }
        };
        mReceiver = new WifiDerectBroadcastReceiver(mManager,
                                                    mChannel,
                                                    this,
                                                    mPeerListListener,
                                                    mConnectionInfoListener);

        //创建服务器
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    private void startServerTask() {
        Log.d(TAG, "startServerTask: 進入到鏈接后的開始任務");
        if (mServerTask == null) {
            mServerTask = new FileServerAsyncTask();
            mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (!mServerTask.getIsAccept()) {
            //还没开始接受 说明程序在
            Log.d(TAG, "startServerTask: 还没开始接受");
        } else if (!mServerTask.getIsFinished()) {
            //重新连接服务器但是还没有完成，那么是不是要  重新启动一下这个服务器
            Log.d(TAG, "startServerTask: 重连服务器 但是还没有完成，重新关闭一下server");
            mServerTask.closeSocket();
            mHandler.sendEmptyMessageDelayed(NOT_FINISHED_RESTART, 100);

        } else {
            Log.d(TAG, "startServerTask: 其他情况");
        }

    }

    private void initView() {
        mBtn_reset = (Button) findViewById(R.id.reset_status_btn);
        mTv_waiting_for_server = (TextView) findViewById(R.id.wating_device_connect);
        mTv_rec_file = (TextView) findViewById(R.id.file_rec);
        mRec_progress = (FlikerProgressBar) findViewById(R.id.rec_file_progress);
        mHandler.sendEmptyMessageDelayed(MISS_PROGRESS , 50);
    }

    private void initData() {
        String path   = Environment.getExternalStorageDirectory() + "/" + "Samson/Wifi-Tranfer/";
        File   file   = new File(path);
        File[] files  = file.listFiles();
        long   l      = System.currentTimeMillis();
        String string = getCurrentTime(l);
        if (files.length == 0) {
            //说明第一次进入  创建 第一个文件夹
            File newFile = new File(path + string + "/");
            newFile.mkdirs();
            mAbsolutePath = newFile.getAbsolutePath();
        } else {
            //看今天是星期几
            String s = DateToWeek(new Date(System.currentTimeMillis()));
            if (s.equals(WEEK[1])) {
                Log.d(TAG, "initData: " + WEEK[1]);
                //说明是星期一 创建一个新的目录
                File newFile = new File(path + string + "/");
                if (!newFile.exists()) {
                    //如果这个目录不存在 那么久创建
                    newFile.mkdirs();
                }
                //存在 就直接取地址
                mAbsolutePath = newFile.getAbsolutePath();
            } else {

                Log.d(TAG, "initData: " + s);
                //看今天是星期几
                int week_i = 0;
                for (int i = 0; i < WEEK.length; i++) {
                    if (s.equals(WEEK[i])) {
                        week_i = i;
                        break;
                    }
                }

                int    i         = string.lastIndexOf("_");
                String substring = string.substring(i + 1, string.length());
                int    i1        = Integer.parseInt(substring);
                if (week_i == 0) {
                    i1 = i1 - 6;
                } else {
                    i1 = i1 - (week_i - 1);
                }
                String substring1 = string.substring(0, i);
                String newString  = substring1 + "_" + i1;

                Log.d(TAG, "initData: " + newString);
                //不是星期一
                mAbsolutePath = path + newString + "/";
            }


        }


        //随便添加
        Log.d(TAG, "initData: 最终的路径" + mAbsolutePath);

    }

    public static String getAbsolutePath() {
        return mAbsolutePath;
    }

    public String getCurrentTime(long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd");
        String           str    = format.format(new Date(date));
        return str;
    }

    Calendar c    = Calendar.getInstance();
    Date     date = c.getTime();
    String   s    = DateToWeek(date);

    public static       String[] WEEK     = {"星期天",
                                             "星期一",
                                             "星期二",
                                             "星期三",
                                             "星期四",
                                             "星期五",
                                             "星期六"};
    public static final int      WEEKDAYS = 7;

    public static String DateToWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayIndex < 1 || dayIndex > WEEKDAYS) {
            return null;
        }
        return WEEK[dayIndex - 1];
    }

    private void initEvent() {
        mBtn_reset.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
        if (mServerTask != null) {
            mServerTask.cancel(false);

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reset_status_btn:
                // TODO: 2017/3/26 断开连接重新创建服务器

                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("Jareld", "onSuccess:remove group ");
                        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int i) {
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "onFailure: 失败了");

                    }
                });
                if (mServerTask != null) {
                    Log.d(TAG, "onClick: servertask status= " + mServerTask.getStatus());

                }

                break;
            default:
                break;
        }
    }
}
