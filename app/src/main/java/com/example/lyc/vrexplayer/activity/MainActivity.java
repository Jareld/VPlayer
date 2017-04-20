package com.example.lyc.vrexplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener, ObservableScrollView.ScrollViewKeydownListener
{

    private static final String TAG = "MainActivity";

    public static final int ORIENTATION_PROTRAIT  = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;

    //目前fragment的状态
    public static final  int FRAGMENT_MEDIA            = 10;
    public static final  int FRAGMENT_WIFI_TANSFER     = 11;
    //权限要求
    private static final int MY_WRITE_EXTERNAL_STORAGE = 20;
    private static final int REMOVE_AND_CREATE         = 21;
    private static final int RE_CREATE_GROUP           = 22;
    private static final int HASRECEIVED_INFOMISS      = 30;
    private static final int TITLE_LANDSCAPE_MISS      = 31;
    private              int FRAGMENT_STATUS           = FRAGMENT_MEDIA;

    private boolean                 isFirstEnter     = true;
    //红边框的位置  即选中视频的位置
    private SelectedPosition        mSelectedPostion = new SelectedPosition();
    private ArrayList<LinearLayout> mLl_Arr          = new ArrayList<>();

    // Used to load the 'native-lib' library on application startup.


    //    private TextView mTx_wifi_tranfer;
    //    private TextView mTx_media;

    private       String       Video_Path;
    private       String       Wifi_Tranfer_Path;
    public static Context      mContext;
    private       int          mInitOrientation;
    private       IntentFilter mFilter;
    //private       WifiP2pManager                        mWifiManager;
    //private       WifiP2pManager.Channel                mChannel;
    //private       ArrayList<HashMap<String, String>>    mPeerLists;
    //private       WifiP2pManager.PeerListListener       mPeerListListener;
    //private       Collection<WifiP2pDevice>             mDeviceList;
    //private       WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    //private       FileServerAsyncTask                   mServerTask;
    ///private       WifiDerectBroadcastReceiver           mReceiver;
    //private       Subscription                          mSubscription;
    // private       UserEvent                             mTransferUserEvent;
    private       String       mBeClickedDeviceName;
    //private       WifiP2pInfo                           mInfo;
    private        boolean mHasLIANJIE         = false;
    private        boolean mIsFramTransferFile = false;
    private static int     POINT               = 1;
    public static ImageView mIv_fenxiang;
    public static TextView  mTv_fenxiang_info;
    private ProgressDialog discoverProgressDialog = null;
    private ListView             mLv_devices;
    private ProgressDialog       connectingProgressDialog;
    private Timer                mTimer;
    private TimerTask            mTimerTask;
    private ArrayList<String>    mVideos;
    private ArrayList<String>    mVideosName;
    private ObservableScrollView mScrollView;
    private LinearLayout         mLl_container;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RE_CREATE_GROUP:

                    //                    mWifiManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    //                        @Override
                    //                        public void onSuccess() {
                    //                            Log.d(TAG, "onSuccess: 我 重新创建成功");
                    //                        }
                    //
                    //                        @Override
                    //                        public void onFailure(int i) {
                    //                            Log.d(TAG, "onSuccess: 我 重新创建失败");
                    //
                    //                        }
                    //                    });
                    break;
                case HASRECEIVED_INFOMISS:
                    if (mSelectedPostion.isDoing()) {

                    } else {
                        //说明就要消失掉

                    }
                    break;
                case TITLE_LANDSCAPE_MISS:
                    if (mInitOrientation == ORIENTATION_LANDSCAPE) {
                        mRl_title_container.setVisibility(View.GONE);
                    }
                    break;
            }

        }
    };
    private RelativeLayout mRl_title_container;
    private ImageView      mIv_left;
    private ImageView      mIv_right;
    private View           mLayout;
    private boolean mIsSelctedClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_main, null);
        setContentView(mLayout);
        getSupportActionBar().hide();
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
                                                                                  .build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.d(TAG, "onCreate: ");
        initView();
        //检查权限
        /// checKPermission();
        initData();
        initEvent();
        init();
        mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
        Log.d(TAG, "init: 调用之前");
        new Thread() {
            @Override
            public void run() {

                while (mLl_Arr.get(0)
                              .getMeasuredHeight() == 0) { }
                MainActivity.this.onScrollViewKeydownChanged(KeyEvent.KEYCODE_DPAD_DOWN);
            }
        }.start();
    }

    //start-检查权限
    private void checKPermission() {

        //看返回的内容    如果是返回PackageManager.PERMISSION_GRANTED就是已经申请过权限了
        //               如果是返回PackageManager.PERMISSION_DENIED那么就是需要申请权限
        Log.d(TAG, "checKStoragePermission: ");
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                           Manifest.permission.CAMERA},
                                              MY_WRITE_EXTERNAL_STORAGE);

        } else {

            initData();
            initEvent();
            init();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //处理回调
        switch (requestCode) {
            case MY_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //这里是允许的时候 第一次就要创建文件夹
                    //如果允许这样的权限的话
                    //查看是否有外部存储卡   如果有  返回外部路径
                    //如果没有返回自己的路径
                    String PVRootPath = getRootPath();
                    Log.d(TAG, "initData:PVRootPath/" + PVRootPath);
                    //在这个路径下    看有没有VrexMedia这个文件夹  如果没有的话就创建  如果有的话
                    boolean isExist = checkoutVrexMeidaIsExist(PVRootPath);
                    //如果有这个文件夹  再看看有没有 pic和video两个文件夹  如果有 为这两个赋值 如果没有的话就要进行创建
                    Log.d(TAG, "initData: isExist" + isExist);
                    if (isExist) {
                        Log.d(TAG, "initData: isExist");
                        //如果有    再看
                        checkoutPicAndVideIsExist(PVRootPath);
                    } else {
                        //如果没有  就要先创建VrexMedia这个
                        String path       = PVRootPath + "/Samson";
                        File   VMPathFile = new File(path);
                        VMPathFile.mkdirs();
                        //再创建 其余两个
                        File picsFile = new File(path + "/Videos");
                        picsFile.mkdirs();
                        File videosFile = new File(path + "/Wifi-Tranfer");
                        videosFile.mkdirs();
                    }
                    //第一次申请后放进sp里面去
                    SharedPreferences        sp   = getSharedPreferences("Path",
                                                                         Activity.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sp.edit();
                    Video_Path = PVRootPath + "/Samson/Videos";
                    Wifi_Tranfer_Path = PVRootPath + "/Samson/Wifi-Tranfer";
                    edit.putString("Pic_Path", Video_Path);
                    edit.putString("Video_Path", Wifi_Tranfer_Path);
                    edit.commit();

                    initData();
                    initEvent();
                    init();
                } else {
                    //这里是不允许的时候
                    finish();
                }
                break;
        }

    }

    //end-检查权限
    private void init() {
        Configuration configuration = getResources().getConfiguration();
        mInitOrientation = configuration.orientation;
        mSelectedPostion.setPosition_i(0);
        mSelectedPostion.setPosition_j(0);
        initFilter();
        //initReceiver();
        initRxJava();


    }

    private void initRxJava() {
        Log.d(TAG, "initRxJava: 初始化");
        //        mSubscription = RxBus.getInstance()
        //                             .toObserverable(UserEvent.class)
        //                             .subscribe(new Action1<UserEvent>() {
        //                                 @Override
        //                                 public void call(final UserEvent userEvent) {
        //                                     Log.d(TAG, "call: 到了call来");
        //
        //
        //                                     switch (userEvent.getName()) {
        //                                         case "transfer_file":
        //
        //                                             mIsFramTransferFile = true;
        //                                             mTransferUserEvent = userEvent;
        //                                             break;
        //                                         case "after":
        //
        //                                             Log.d(TAG, "call: 创建服务器");
        //                                             final long fileleth = userEvent.getProgress();
        //                                             // 这里 先休息一下  然后再断开
        //                                             mWifiManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
        //                                                 @Override
        //                                                 public void onSuccess() {
        //                                                     Log.d(TAG,
        //                                                           "onSuccess: 说明客户单传输完成后主动取消客户端 成功");
        //                                                 }
        //
        //                                                 @Override
        //                                                 public void onFailure(int i) {
        //                                                     Log.d(TAG,
        //                                                           "onSuccess: 说明客户单传输完成后主动取消客户端 失败");
        //
        //                                                 }
        //                                             });
        //
        //                                             handler.postDelayed(new Runnable() {
        //                                                 @Override
        //                                                 public void run() {
        //
        //
        //
        //                                                     mWifiManager.removeGroup(mChannel,
        //                                                                              new WifiP2pManager.ActionListener() {
        //                                                                                  @Override
        //                                                                                  public void onSuccess() {
        //                                                                                      Log.d(TAG,
        //                                                                                            "onSuccessremoveGroup:再次 创建服务器2");
        //
        //                                                                                  }
        //
        //                                                                                  @Override
        //                                                                                  public void onFailure(
        //                                                                                          int i)
        //                                                                                  {
        //                                                                                      Log.d(TAG,
        //                                                                                            "onFailureremoveGroup:再次 创建服务器");
        //
        //                                                                                  }
        //                                                                              });
        //                                                     handler.post(new Runnable() {
        //                                                         @Override
        //                                                         public void run() {
        //
        //                                                             mTv_fenxiang_info.setText("传送文件完成:" + fileleth + "MB");
        //
        //                                                            handler.sendEmptyMessageDelayed(HASRECEIVED_INFOMISS, 3000);
        //                                                             Toast.makeText(mContext,
        //                                                                            "文件传输完成",
        //                                                                            Toast.LENGTH_SHORT)
        //                                                                  .show();
        //                                                             SystemClock.sleep(1000);
        //
        //                                                             mIv_fenxiang.setVisibility(View.GONE);
        //                                                             //  mTv_fenxiang_info.setVisibility(View.GONE);
        //                                                             //状态的更新
        //                                                             mSelectedPostion.setDoing(false);
        //
        //                                                         }
        //                                                     });
        //
        //                                                     handler.sendEmptyMessageDelayed(RE_CREATE_GROUP,1000);
        //
        //                                                 }
        //                                             }, 2000);
        //
        //                                             break;
        //                                         case "hasReceived":
        //                                             mSelectedPostion.setSeverReceving(false);
        //                                             Log.d(TAG, "copyFileServer: 重新创建");
        //                                           String filename =  userEvent.getFileName();
        //                                             Log.d(TAG, "call: filename" + filename);
        //                                             sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + "VrecxPlayer/Wifi-Tranfer/"+filename))));
        //
        //
        //
        //
        //                                             handler.post(new Runnable() {
        //                                                 @Override
        //                                                 public void run() {
        //
        //
        //                                                     if(mTv_fenxiang_info.getVisibility() == View.GONE){
        //                                                         mTv_fenxiang_info.setVisibility(View.VISIBLE);
        //                                                     }
        //                                                     mTv_fenxiang_info.setText("文件已接受"+userEvent.getFileName());
        //
        //
        //                                                 }
        //                                             });
        //
        //                                             //重新执行一次等待的任务
        //                                             //这里不应该开始一个任务的
        //
        ////                                             mServerTask = new FileServerAsyncTask();
        ////
        ////                                             mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //                                             break;
        //                                         case "doing":
        //                                        //文件传输过程中不要
        //
        //                                             final float finalPer = userEvent.getProgress();
        //                                             handler.post(new Runnable() {
        //                                                 @Override
        //                                                 public void run() {
        //                                                     if(mLv_devices.getVisibility() == View.VISIBLE){
        //                                                         mLv_devices.setVisibility(View.GONE);
        //                                                     }
        //
        //                                                     if (mTv_fenxiang_info.getVisibility() == View.GONE) {
        //                                                         mTv_fenxiang_info.setVisibility(View.VISIBLE);
        //                                                     }
        //                                                     mTv_fenxiang_info.setText("传送文件中：" + finalPer + "MB" + "/" + userEvent.getFileLengthMB() +"MB");
        //                                                     //这里就要退出长按的状态
        //                                                     mSelectedPostion.setLongClick(false);
        //                                                     if (!mSelectedPostion.isDoing()) {
        //                                                         Log.d(TAG, "run: 是不是正在doing中");
        //                                                         if (mInitOrientation == ORIENTATION_LANDSCAPE) {
        //                                                             moveBeforeFocusNowColor(
        //                                                                     mSelectedPostion.getBefore_position_i(),
        //                                                                     mSelectedPostion.getBefore_position_j() + 1,
        //                                                                     Color.TRANSPARENT,
        //                                                                     Color.parseColor("#ffffff00"));
        //                                                         } else {
        //                                                             moveBeforeFocusNowColor(
        //                                                                     mSelectedPostion.getBefore_position_i(),
        //                                                                     mSelectedPostion.getBefore_position_j() + 1,
        //                                                                     Color.TRANSPARENT,
        //                                                                     Color.TRANSPARENT);
        //                                                         }
        //                                                         mSelectedPostion.setDoing(true);
        //                                                     }
        //                                                 }
        //                                             });
        //
        //                                             break;
        //                                         case "orientation":
        //
        //                                             if (userEvent.getProgress() == MainActivity.ORIENTATION_PROTRAIT) {
        //                                                 setOrietationText(MainActivity.ORIENTATION_PROTRAIT);
        //                                             } else {
        //                                                 setOrietationText(MainActivity.ORIENTATION_LANDSCAPE);
        //                                             }
        //                                             break;
        //                                         case "transfer_file_start":
        //                                             handler.post(new Runnable() {
        //                                                 @Override
        //                                                 public void run() {
        //                                                     mTv_fenxiang_info.setText("开始传送文件");
        //                            mSelectedPostion.setDoing(false);
        //                                                //在开始传送文件的时候
        //                                                     if(mInitOrientation == ORIENTATION_LANDSCAPE){
        //                                                         //横屏的时候
        //                                                         if(mRl_title_container.getVisibility() == View.VISIBLE){
        //                                                             mRl_title_container.setVisibility(View.GONE);
        //                                                         }
        //
        //                                                     }
        //                                                 }
        //                                             });
        //                                             break;
        //                                         case "serverReceiving":
        //                                             //正在接受文件
        //                                             mSelectedPostion.setSeverReceving(true);
        //                                             handler.post(new Runnable() {
        //                                                 @Override
        //                                                 public void run() {
        //                                                     if(mLv_devices.getVisibility() == View.VISIBLE){
        //                                                         mLv_devices.setVisibility(View.GONE);
        //                                                     }
        //
        //                                                     if(mTv_fenxiang_info.getVisibility() == View.GONE){
        //                                                         mTv_fenxiang_info.setVisibility(View.VISIBLE);
        //                                                     }
        //                                                     mTv_fenxiang_info.setText("正在接受文件");
        //
        //
        //                                                 }
        //                                             });
        //
        //                                             break;
        //
        //
        //                                     }
        //
        //
        //                                 }
        //                             });
    }

    //    private void sendFile(String str) {
    //        Log.d(TAG, "sendFile: " + str);
    //
    //        Intent serviceIntent = new Intent(MainActivity.this, FileTransferService.class);
    //        int    i             = str.lastIndexOf("/");
    //
    //        String realFilePath = str.substring(i + 1);
    //
    //        File file = new File(str);
    //
    //        Log.d(TAG, "sendFile: " + file.exists());
    //
    //        Uri parse = Uri.parse("file://" + str);
    //
    //
    //        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
    //
    //        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, parse.toString());
    //
    //        serviceIntent.putExtra(FileTransferService.REAL_FILE_PATH, realFilePath);
    //
    //        serviceIntent.putExtra(FileTransferService.BE_CLICKED_DEVICE_NAME, mBeClickedDeviceName);
    //
    //        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
    //                               mInfo.groupOwnerAddress.getHostAddress());
    //
    //        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
    //
    //        MainActivity.this.startService(serviceIntent);
    //
    //
    //    }

    private void initFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    private void initEvent() {
        //        mTx_media.setOnClickListener(this);
        //        mTx_wifi_tranfer.setOnClickListener(this);
        mIv_fenxiang.setOnClickListener(this);
    }

    private void initData() {

        mContext = getApplicationContext();
        Configuration configuration = getResources().getConfiguration();
        mInitOrientation = configuration.orientation;

        mVideos = new ArrayList<>();
        mVideosName = new ArrayList<>();
        Intent            intent   = getIntent();
        ArrayList<String> videoses = intent.getStringArrayListExtra("Videos");
        int               position = intent.getIntExtra("position", -1);
        Log.d(TAG, "initData: " + videoses + "::" + position);

        String          videose   = videoses.get(position);
        File            file      = new File(videose);
        File[]          files     = file.listFiles();
        Log.d(TAG, "initData: "+files.length);
        ArrayList<File> arrayList = new ArrayList<>();
        for (File f : files) {
            if (MediaFile.isVideoFileType(f.getAbsolutePath())) {
                //                mVideos.add(f.getAbsolutePath());
                //                mVideosName.add(f.getName());
                arrayList.add(f);
            }
        }

        Collections.sort(arrayList, new FileComparator());//通过重写Comparator的实现类

        for (int i = 0; i < arrayList.size(); i++) {
            mVideos.add(arrayList.get(i).getAbsolutePath());
            mVideosName.add(arrayList.get(i).getName());
        }


        //        ContentResolver contentResolver = mContext.getContentResolver();
        //
        //        Uri           uri   = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        //        StringBuilder where = new StringBuilder();
        //        where.append(MediaStore.Video.Media.TITLE + " != ''");
        //        String[] projection = new String[]{MediaStore.Video.Media.TITLE,
        //                                           MediaStore.Video.Media.DATA};
        //        where.append(" AND " + MediaStore.Video.Media.DATA + " LIKE '%" + "Samson" + "%'");
        //        final Cursor cursor = contentResolver.query(uri,
        //                                                    projection,
        //                                                    where.toString(),
        //                                                    null,
        //                                                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        //        if (cursor == null) {
        //            Toast.makeText(MainActivity.mContext, "No video file is found !", Toast.LENGTH_SHORT)
        //                 .show();
        //            return;
        //        }
        //        if (cursor.moveToFirst()) {
        //            do {
        //                String path  = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        //                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
        //                Log.v(TAG, "[getImagess]path = " + path);
        //                mVideos.add(path);
        //                mVideosName.add(title);
        //            } while (cursor.moveToNext());
        //        }
        //
        //        Uri           uri_dcim   = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        //        StringBuilder where_dcim = new StringBuilder();
        //        where_dcim.append(MediaStore.Video.Media.TITLE + " != ''");
        //        String[] projection_dcim = new String[]{MediaStore.Video.Media.TITLE,
        //                                           MediaStore.Video.Media.DATA};
        //        where_dcim.append(" AND " + MediaStore.Video.Media.DATA + " LIKE '%" + "DCIM" + "%'");
        //        final Cursor cursor_dcim = contentResolver.query(uri_dcim,
        //                                                    projection_dcim,
        //                                                    where_dcim.toString(),
        //                                                    null,
        //                                                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        //        if (cursor_dcim == null) {
        //            Toast.makeText(MainActivity.mContext, "No video file is found !", Toast.LENGTH_SHORT)
        //                 .show();
        //            return;
        //        }
        //        if (cursor_dcim.moveToFirst()) {
        //            do {
        //                String path  = cursor_dcim.getString(cursor_dcim.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        //                String title = cursor_dcim.getString(cursor_dcim.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
        //                Log.v(TAG, "[getImagess]path = " + path);
        //                mVideos.add(path);
        //                mVideosName.add(title);
        //            } while (cursor_dcim.moveToNext());
        //        }

        Log.d(TAG, "initData: 数量" + mVideos.size());


        mInitOrientation = getResources().getConfiguration().orientation;

        if (mInitOrientation == MainActivity.ORIENTATION_PROTRAIT) {
            //加载一个
            Log.d(TAG, "initView: 加载一个");
            setProtraitOrientation();
        } else if (mInitOrientation == MainActivity.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "initView: 加载两个");
            setLandscapeOrientation();
        }

        mScrollView.setOnScrollViewKeydownListener(this);
        mScrollView.setOrientation(mInitOrientation);

        if (!mSelectedPostion.getIsFirst()) {
            //那么久要进行某一个 方框的xuanze
            if (mInitOrientation == ORIENTATION_LANDSCAPE) {
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j(),
                                   Color.parseColor("#ffffff00"));
                moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                                   mSelectedPostion.getPosition_j() + 2,
                                   Color.parseColor("#ffffff00"));
            }
        }

        if (mInitOrientation == Configuration.ORIENTATION_PORTRAIT) {

            //目前是media
            //   mTx_media.setText("媒体库");
            //  mTx_wifi_tranfer.setVisibility(View.GONE);
            mRl_title_container.setVisibility(View.VISIBLE);
        } else if (mInitOrientation == Configuration.ORIENTATION_LANDSCAPE) {

            //  mTx_media.setText("媒体库：VR模式");
            //  mTx_wifi_tranfer.setVisibility(View.VISIBLE);
            // mTx_wifi_tranfer.setText("媒体库：VR模式");
            mRl_title_container.setVisibility(View.GONE);
        }
    }

    private class FileComparator
            implements Comparator<File>
    {
        @Override
        public int compare(File file, File t1) {
            if (file.lastModified() >= t1.lastModified()) {
                return -1;
            } else {
                return 1;
            }
        }

        //        @Override
        //        public int compare(String s, String t1) {
        //            if(MediaFile.getLastFileTime(s) > MediaFile.getLastFileTime(t1)){
        //                return -1;
        //            }else{
        //                return  1;
        //            }
        //        }
    }

    private void setLandscapeOrientation() {
        RxBus.getInstance()
             .post(new UserEvent(MainActivity.ORIENTATION_LANDSCAPE, "orientation"));
        mLl_container.removeAllViews();
        mLl_Arr.clear();

        for (int i = 0; i < mVideos.size() / 2; i++) {
            LinearLayout inflate = (LinearLayout) View.inflate(MainActivity.mContext,
                                                               R.layout.custum_scroll_item_view_double,
                                                               null);
            mLl_Arr.add(inflate);
            ImageView iv_1 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_1);
            ImageView iv_2 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_2);
            ImageView iv_3 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_3);
            ImageView iv_4 = (ImageView) inflate.findViewById(R.id.item_scroll_view_double_4);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_1)).setText(mVideosName.get(
                    2 * i));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_2)).setText(mVideosName.get(
                    2 * i + 1));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_3)).setText(mVideosName.get(
                    2 * i));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_4)).setText(mVideosName.get(
                    2 * i + 1));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_1)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_2)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_3)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_double_4)).setSelected(true);

            Log.d(TAG,
                  "setLandscapeOrientation: " + mVideosName.get(i * 2) + "::" + ((TextView) inflate.findViewById(
                          R.id.item_tx_scroll_view_double_1)).getText());
            final int final_i = i;
            Log.d(TAG, "setLandscapeOrientation: final_i" + final_i);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i))
                 .sizeMultiplier(0.5f)
                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i + 1))
                 .centerCrop()
                 .sizeMultiplier(0.5f)
                 .placeholder(R.mipmap.ic_launcher)

                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_2);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i))
                 .centerCrop()
                 .sizeMultiplier(0.5f)
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_3);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i + 1))
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


            mLl_container.addView(inflate);
            if (mVideos.size() % 2 != 0 && i == mVideos.size() / 2 - 1) {

                //说明 单了一个 ， 要最后添加一个
                LinearLayout inflate1 = (LinearLayout) View.inflate(MainActivity.mContext,
                                                                    R.layout.custum_scroll_item_view_double,
                                                                    null);
                int height = inflate.getHeight();
                mLl_Arr.add(inflate1);
                ImageView iv_double_1 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_1);
                ImageView iv_double_2 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_2);
                ImageView iv_double_3 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_3);
                ImageView iv_double_4 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_4);

                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_1)).setText(
                        mVideosName.get(mVideosName.size() - 1));
                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_3)).setText(
                        mVideosName.get(mVideosName.size() - 1));
                final int final_double_i = i + 1;
                iv_double_2.setBackgroundColor(Color.TRANSPARENT);
                iv_double_4.setBackgroundColor(Color.TRANSPARENT);
                Glide.with(MainActivity.mContext)
                     .load(mVideos.get(mVideos.size() - 1))
                     .sizeMultiplier(0.5f)

                     .centerCrop()
                     .placeholder(R.mipmap.ic_launcher)
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_double_1);

                Glide.with(MainActivity.mContext)
                     .load(mVideos.get(mVideos.size() - 1))
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
                mLl_container.addView(inflate1);
            }
        }

        if (mVideos.size() == 1) {


            //说明 单了一个 ， 要最后添加一个
            LinearLayout inflate1 = (LinearLayout) View.inflate(MainActivity.mContext,
                                                                R.layout.custum_scroll_item_view_double,
                                                                null);
            mLl_Arr.add(inflate1);
            ImageView iv_double_1 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_1);
            ImageView iv_double_2 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_2);
            ImageView iv_double_3 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_3);
            ImageView iv_double_4 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_double_4);

            ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_1)).setText(
                    mVideosName.get(mVideosName.size() - 1));
            ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_double_3)).setText(
                    mVideosName.get(mVideosName.size() - 1));
            final int final_double_i = 0 ;
            iv_double_2.setBackgroundColor(Color.TRANSPARENT);
            iv_double_4.setBackgroundColor(Color.TRANSPARENT);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(mVideos.size() - 1))
                 .sizeMultiplier(0.5f)

                 .centerCrop()
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_double_1);

            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(mVideos.size() - 1))
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
            mLl_container.addView(inflate1);
        }

    }

    private void setProtraitOrientation() {
        RxBus.getInstance()
             .post(new UserEvent(MainActivity.ORIENTATION_PROTRAIT, "orientation"));

        mLl_container.removeAllViews();
        mLl_Arr.clear();

        for (int i = 0; i < mVideos.size() / 2; i++) {
            LinearLayout inflate = (LinearLayout) View.inflate(MainActivity.mContext,
                                                               R.layout.custum_scroll_item_view_single,
                                                               null);
            mLl_Arr.add(inflate);

            ImageView iv_1 = (ImageView) inflate.findViewById(R.id.item_scroll_view_single_1);
            ImageView iv_2 = (ImageView) inflate.findViewById(R.id.item_scroll_view_single_2);

            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_1)).setText(mVideosName.get(
                    2 * i));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_2)).setText(mVideosName.get(
                    2 * i + 1));
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_1)).setSelected(true);
            ((TextView) inflate.findViewById(R.id.item_tx_scroll_view_single_2)).setSelected(true);

            final int final_i = i;
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i))
                 .sizeMultiplier(0.5f)
                 .placeholder(R.mipmap.ic_launcher)
                 .centerCrop()
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i + 1))
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
            mLl_container.addView(inflate);
            if (mVideos.size() % 2 != 0 && i == mVideos.size() / 2 - 1) {

                //说明 单了一个 ， 要最后添加一个
                LinearLayout inflate1 = (LinearLayout) View.inflate(MainActivity.mContext,
                                                                    R.layout.custum_scroll_item_view_single,
                                                                    null);
                mLl_Arr.add(inflate1);
                ImageView iv_double_1 = (ImageView) inflate1.findViewById(R.id.item_scroll_view_single_1);


                ((TextView) inflate1.findViewById(R.id.item_tx_scroll_view_single_1)).setText(
                        mVideosName.get(mVideosName.size() - 1));
                final int final_double_i = i + 1;
                Glide.with(MainActivity.mContext)

                     .load(mVideos.get(mVideos.size() - 1))
                     .centerCrop()
                     .sizeMultiplier(0.5f)
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
                mLl_container.addView(inflate1);
            }
        }
    }


    private void initView() {
        mRl_title_container = (RelativeLayout) findViewById(R.id.rl_title_container);
        //mTx_media = (TextView) findViewById(R.id.main_media);
        // mTx_wifi_tranfer = (TextView) findViewById(R.id.main_wifi_transfer);
        mIv_fenxiang = (ImageView) findViewById(R.id.iv_fenxiang);
        mTv_fenxiang_info = (TextView) findViewById(R.id.tv_fenxiang_info);
        mLv_devices = (ListView) findViewById(R.id.lv_discover_devices);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mLl_container = (LinearLayout) findViewById(R.id.ll_scrollview_container);

        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mIv_right = (ImageView) findViewById(R.id.iv_right);
        Glide.with(MainActivity.this)
             .load(R.mipmap.pic_video_bg)
             .sizeMultiplier(0.5f)
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_left);
        Glide.with(MainActivity.this)
             .load(R.mipmap.pic_video_bg)
             .sizeMultiplier(0.5f)
             .centerCrop()
             .diskCacheStrategy(DiskCacheStrategy.RESULT)
             .into(mIv_right);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //            case R.id.iv_fenxiang:
            //                //分享给别人：
            //                //点击分享的时候 才会进行创建客户端的操作
            //                Log.d(TAG, "onClick: 分享给别人");
            //                Log.d(TAG, "call: 到这边来处理");
            //                //如果横屏的情况下 即
            //
            //                //remove
            ////                mWifiManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            ////                    @Override
            ////                    public void onSuccess() {
            ////                        Log.d(TAG, "onSuccess: 移除服务成功");
            ////                        //discover
            ////
            ////
            ////                    }
            ////
            ////                    @Override
            ////                    public void onFailure(int reason) {
            ////                        Log.d(TAG, "onFailure: 移除服务失败");
            ////                    }
            ////                });
            //                //remove之后  再进行 搜索设备
            //
            //                mLv_devices.setVisibility(View.VISIBLE);
            //
            //                if (discoverProgressDialog != null && discoverProgressDialog.isShowing()) {
            //                    discoverProgressDialog.dismiss();
            //                }
            //                discoverProgressDialog = ProgressDialog.show(this, "搜索设备", "搜索中......:", true, true,
            //                                                             // cancellable
            //                                                             new DialogInterface.OnCancelListener() {
            //                                                                 @Override
            //                                                                 public void onCancel(
            //                                                                         DialogInterface dialog)
            //                                                                 {
            //                                                                     mWifiManager.stopPeerDiscovery(
            //                                                                             mChannel,
            //                                                                             new WifiP2pManager.ActionListener() {
            //                                                                                 @Override
            //                                                                                 public void onSuccess() {
            //
            //                                                                                 }
            //
            //                                                                                 @Override
            //                                                                                 public void onFailure(
            //                                                                                         int reason)
            //                                                                                 {
            //
            //                                                                                 }
            //                                                                             });
            //                                                                     if (mSelectedPostion.getIsLongClick()) {
            //                                                                         //如果是处于长安状态  那么 就先退出长安状态
            //                                                                         mSelectedPostion.setLongClick(false);
            //                                                                         //设置view的消失
            //                                                                         mIv_fenxiang.setVisibility(View.GONE);
            //                                                                         mTv_fenxiang_info.setVisibility(View.GONE);
            //                                                                         if (mInitOrientation == ORIENTATION_LANDSCAPE) {
            //                                                                             moveBeforeFocusNowColor(mSelectedPostion.getBefore_position_i(),
            //                                                                                                     mSelectedPostion.getBefore_position_j() + 1,
            //                                                                                                     Color.TRANSPARENT,
            //                                                                                                     Color.parseColor("#ffffff00"));
            //                                                                             mRl_title_container.setVisibility(View.GONE);
            //
            //                                                                         } else {
            //                                                                             moveBeforeFocusNowColor(mSelectedPostion.getBefore_position_i(),
            //                                                                                                     mSelectedPostion.getBefore_position_j() + 1,
            //                                                                                                     Color.TRANSPARENT,
            //                                                                                                     Color.TRANSPARENT);
            //                                                                         }
            //                                                                         Toast.makeText(mContext,"取消发送文件",Toast.LENGTH_SHORT).show();
            //
            //                                                                        if(mLv_devices.getVisibility() == View.VISIBLE){
            //                                                                            mLv_devices.setVisibility(View.GONE);
            //                                                                        }
            //
            //                                                                     }
            //
            //                                                                     //这里肯定是Client主动连接设备失效了
            //                                                                     mWifiManager.removeGroup(mChannel,
            //                                                                                              new WifiP2pManager.ActionListener() {
            //                                                                                                  @Override
            //                                                                                                  public void onSuccess() {
            //                                                                                                      Log.d(TAG,
            //                                                                                                            "onSuccessremoveGroup:再次 创建服务器2");
            //
            //                                                                                                  }
            //
            //                                                                                                  @Override
            //                                                                                                  public void onFailure(
            //                                                                                                          int i)
            //                                                                                                  {
            //                                                                                                      Log.d(TAG,
            //                                                                                                            "onFailureremoveGroup:再次 创建服务器");
            //
            //                                                                                                  }
            //                                                                                              });
            //                                                                     handler.postDelayed(new Runnable() {
            //                                                                         @Override
            //                                                                         public void run() {
            //                                                                             mWifiManager.createGroup(
            //                                                                                     mChannel,
            //                                                                                     new WifiP2pManager.ActionListener() {
            //                                                                                         @Override
            //                                                                                         public void onSuccess() {
            //
            //                                                                                         }
            //
            //                                                                                         @Override
            //                                                                                         public void onFailure(
            //                                                                                                 int i)
            //                                                                                         {
            //
            //                                                                                         }
            //                                                                                     });
            //                                                                         }
            //                                                                     } , 2000);
            //                                                                 }
            //                                                             });
            //
            //                mWifiManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            //                    @Override
            //                    public void onSuccess() {
            //                        Log.d(TAG, "onSuccess: 发现服务");
            //
            //
            //                    }
            //
            //                    @Override
            //                    public void onFailure(int reason)
            //                    {
            //                        Log.d(TAG, "onFailure: 发现服务失败");
            //                    }
            //                });
            //
            //                //执行搜索的回调mPeerListListener
            //                //test 直接连接第一个
            //
            //                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        //这个orientation是目前的 角度
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mInitOrientation = ORIENTATION_PROTRAIT;
            //现在是竖屏 ， 横屏变成了竖屏

            //目前是media
            //mTx_media.setText("媒体库");
            //   mTx_wifi_tranfer.setVisibility(View.GONE);
            //重新加载一次view
            setProtraitOrientation();


            mRl_title_container.setVisibility(View.VISIBLE);

            mIv_right.setVisibility(View.GONE);


        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mInitOrientation = ORIENTATION_LANDSCAPE;
            //现在是横屏 ， 竖屏变成了横屏
            //目前是media
            //横屏就进入VR模式
            //  mTx_media.setText("媒体库：VR模式");
            //  mTx_wifi_tranfer.setVisibility(View.VISIBLE);
            //  mTx_wifi_tranfer.setText("媒体库：VR模式");
            mRl_title_container.setVisibility(View.GONE);
            setLandscapeOrientation();
            mIv_right.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "onConfigurationChanged: 方向改变" + orientation);


    }

    private void setTexeviewBG(int select) {
        switch (select) {
            case FRAGMENT_MEDIA:
                //  mTx_media.setBackgroundColor(Color.parseColor("#99A78780"));
                //  mTx_wifi_tranfer.setBackgroundColor(Color.parseColor("#99A78780"));
                break;
            case FRAGMENT_WIFI_TANSFER:
                //mTx_media.setBackgroundColor(Color.parseColor("#99A78780"));
                //  mTx_wifi_tranfer.setBackgroundColor(Color.parseColor("#99A78780"));
                break;
            default:
                break;
        }


    }

    @Override
    public void onBackPressed() {
        if (mSelectedPostion.getIsLongClick()) {
            //mPeerLists.clear();

            //如果是处于长安状态  那么 就先退出长安状态
            mSelectedPostion.setLongClick(false);
            //设置view的消失
            mIv_fenxiang.setVisibility(View.GONE);
            mTv_fenxiang_info.setVisibility(View.GONE);
            if (mInitOrientation == ORIENTATION_LANDSCAPE) {
                moveBeforeFocusNowColor(mSelectedPostion.getBefore_position_i(),
                                        mSelectedPostion.getBefore_position_j() + 1,
                                        Color.TRANSPARENT,
                                        Color.parseColor("#ffffff00"));
                //如果是横屏 并且处于长按状态  那么  tl——tile要missDiao
                if (mInitOrientation == ORIENTATION_LANDSCAPE) {
                    mRl_title_container.setVisibility(View.GONE);
                }


            } else {
                moveBeforeFocusNowColor(mSelectedPostion.getBefore_position_i(),
                                        mSelectedPostion.getBefore_position_j() + 1,
                                        Color.TRANSPARENT,
                                        Color.TRANSPARENT);
            }
            //重置一下状态
            //            mWifiManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            //                @Override
            //                public void onSuccess() {
            //
            //                }
            //
            //                @Override
            //                public void onFailure(int i) {
            //
            //                }
            //            });
            //            mWifiManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            //                @Override
            //                public void onSuccess() {
            //
            //                }
            //
            //                @Override
            //                public void onFailure(int i) {
            //
            //                }
            //            });


            return;
        } else if (mSelectedPostion.isDoing()) {
            Log.d(TAG, "onBackPressed: 正在传输文件");
            handler.post(new Runnable() {
                @Override
                public void run() {

                    new AlertDialog.Builder(MainActivity.this).setTitle("文件正在传输，确认中止传输并退出？")
                                                              .setPositiveButton("确认",
                                                                                 new DialogInterface.OnClickListener() {
                                                                                     @Override
                                                                                     public void onClick(
                                                                                             DialogInterface dialogInterface,
                                                                                             int i)
                                                                                     {
                                                                                         MainActivity.super.onBackPressed();
                                                                                     }
                                                                                 })
                                                              .setNegativeButton("取消",
                                                                                 new DialogInterface.OnClickListener() {
                                                                                     @Override
                                                                                     public void onClick(
                                                                                             DialogInterface dialogInterface,
                                                                                             int i)
                                                                                     {

                                                                                     }
                                                                                 })
                                                              .show();

                }

            });
            return;
        } else if (mSelectedPostion.isSeverReceving()) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    new AlertDialog.Builder(MainActivity.this).setTitle("正在接受文件，确认停止接受并退出？")
                                                              .setPositiveButton("确认",
                                                                                 new DialogInterface.OnClickListener() {
                                                                                     @Override
                                                                                     public void onClick(
                                                                                             DialogInterface dialogInterface,
                                                                                             int i)
                                                                                     {
                                                                                         MainActivity.super.onBackPressed();
                                                                                     }
                                                                                 })
                                                              .setNegativeButton("取消",
                                                                                 new DialogInterface.OnClickListener() {
                                                                                     @Override
                                                                                     public void onClick(
                                                                                             DialogInterface dialogInterface,
                                                                                             int i)
                                                                                     {

                                                                                     }
                                                                                 })
                                                              .show();
                }

            });
            return;

        }
        super.onBackPressed();
    }

    //start-创建文件
    private String getRootPath() {
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

    private boolean checkoutVrexMeidaIsExist(String PVRootPath) {
        File files = new File(PVRootPath);

        if (files != null && files.list().length != 0) {
            for (String file : files.list()) {
                if (file.equals("VrecxPlayer")) {
                    //存在这个文件夹
                    return true;
                }
            }
        }
        //如果到这里就是不存在这个文件夹了
        return false;
    }

    private void checkoutPicAndVideIsExist(String sPath) {
        String path = sPath + "/VrecxPlayer";
        File   file = new File(path);

        boolean hasVideo        = false;
        boolean hasWifiTransfer = false;
        if (file != null & file.list().length != 0) {
            for (String fileName : file.list()) {
                Log.d(TAG, "checkoutPicAndVideIsExist: " + fileName);
                if (fileName.equals("Videos")) {
                    hasVideo = true;
                }
                if (fileName.equals("Wifi-Tranfer")) {
                    hasWifiTransfer = true;
                }
            }
        }
        if (!hasVideo) {
            //如果没有pic这个文件夹 创建
            File picsFile = new File(path + "/Videos");
            picsFile.mkdirs();
        }
        if (!hasWifiTransfer) {
            //如果没有pic这个文件夹 创建
            File videosFile = new File(path + "/Wifi-Tranfer");
            videosFile.mkdirs();
        }

    }

    //end-创建文件
    //    private void initReceiver() {
    //        mWifiManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
    //        mChannel = mWifiManager.initialize(this, getMainLooper(), null);
    //        mPeerLists = new ArrayList<HashMap<String, String>>();
    //        //这个是申请到列表后的回调
    //        mPeerListListener = new WifiP2pManager.PeerListListener() {
    //            @Override
    //            public void onPeersAvailable(WifiP2pDeviceList peers) {
    //                LogUtils.logInfo(TAG,
    //                                 "onPeersAvailable",
    //                                 "::" + peers.getDeviceList()
    //                                             .size());
    //                if (mPeerLists != null) {
    //                    mPeerLists.clear();
    //                }
    //
    //                if (discoverProgressDialog != null && discoverProgressDialog.isShowing()) {
    //                    discoverProgressDialog.dismiss();
    //                }
    //
    //
    //                //这个是申请到列表后的回调
    //                mDeviceList = peers.getDeviceList();
    //                for (WifiP2pDevice wifiP2pDevice : mDeviceList) {
    //                    HashMap<String, String> map = new HashMap<>();
    //                    map.put("name", wifiP2pDevice.deviceName);
    //                    map.put("address", wifiP2pDevice.deviceAddress);
    //                    mPeerLists.add(map);
    //                }
    //                MyAdatpter myAdatpter = new MyAdatpter();
    //                mLv_devices.setAdapter(myAdatpter);
    //
    //                if(isFirstEnter){
    //                    mLv_devices.setVisibility(View.GONE);
    //                    isFirstEnter=false;
    //                }
    //
    //                Log.d(TAG, "onPeersAvailable: mHasLIANJIE= " + mHasLIANJIE);
    //                //                if (mPeerLists.size() != 0 && mIsFramTransferFile) {
    //                //
    //                //                    Log.d(TAG, "onSuccess: 成功找到 再次连接");
    //                //                    createConnet(mPeerLists.get(0)
    //                //                                           .get("name"),
    //                //                                 mPeerLists.get(0)
    //                //                                           .get("address"));
    //                //                    mBeClickedDeviceName = mPeerLists.get(0)
    //                //                                                     .get("name");
    //                //                    mWifiManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
    //                //                        @Override
    //                //                        public void onSuccess() {
    //                //
    //                //                        }
    //                //
    //                //                        @Override
    //                //                        public void onFailure(int i) {
    //                //
    //                //                        }
    //                //                    });
    //                //                }
    //
    //
    //            }
    //        };
    //        //这是申请连接后的回调
    //        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
    //            @Override
    //            public void onConnectionInfoAvailable(WifiP2pInfo info) {
    //                //这是申请连接后的回调
    //                mInfo = info;
    //
    //
    //                ///192.168.49.1info.isGroupOwnertrue
    //
    //                if (info.groupFormed && info.isGroupOwner) {
    //
    //                    //说明是服务端
    //                    LogUtils.logInfo(TAG, "onConnectionInfoAvailable", "说明是服务器  接受数据" +  mSelectedPostion.isDoing() +"::"+mSelectedPostion.isSeverReceving());
    //
    //                    mWifiManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
    //                        @Override
    //                        public void onSuccess() {
    //
    //                        }
    //
    //                        @Override
    //                        public void onFailure(int i) {
    //
    //                        }
    //                    });
    //
    //
    //                   //}
    //
    //                    // TODO: 2016/12/15 等待做一个接受  告诉服务器本身 申请连接的是哪一个
    //                    if (mDeviceList == null) {
    //                    } else {
    //                        for (WifiP2pDevice wifiP2pDevice : mDeviceList) {
    //
    //                            if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
    //                                Log.d(TAG, "onConnectionInfoAvailable: 这里才说明有了连接");
    //                                Log.d(TAG,
    //                                      "onConnectionInfoAvailable: " + wifiP2pDevice.deviceName + "::" + wifiP2pDevice.status);
    //                              //只有连接了设备才启动任务
    //
    //                                mServerTask = new FileServerAsyncTask();
    //
    //                                mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    //
    //                                return;
    //                            }
    //                        }
    //                        Log.d(TAG, "onConnectionInfoAvailable: 说明还只是服务器 ， 没有连接设备");
    //
    //                    }
    //
    //
    //                } else if (info.groupFormed) {
    //
    //                    if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
    //                        connectingProgressDialog.dismiss();
    //                    }
    //
    //                    //说明是客户端
    //                    LogUtils.logInfo(TAG, "onConnectionInfoAvailable", "说明是客户端  发送数据");
    //                    if (mSelectedPostion.isDoing()) {
    //
    //                    } else {
    //                        //这里才是真正创建成功了
    //                        sendFile(mTransferUserEvent.getFileName());
    //                    }
    //                }
    //
    //
    //            }
    //        };
    //        mReceiver = new WifiDerectBroadcastReceiver(mWifiManager,
    //                                                    mChannel,
    //                                                    MainActivity.this,
    //                                                    mPeerListListener,
    //                                                    mConnectionInfoListener);
    //        registerReceiver(mReceiver, mFilter);
    //
    //    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: zheli youmeiyou ");
        //        mWifiManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
        //            @Override
        //            public void onSuccess() {
        //                Log.d(TAG, "onDestroy:cancelConnect: ");
        //
        //
        //            }
        //
        //            @Override
        //            public void onFailure(int i) {
        //                Log.d(TAG, "onDestroy:cancelConnect: onFailure");
        //
        //            }
        //        });
        //        mWifiManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
        //            @Override
        //            public void onSuccess() {
        //                Log.d(TAG, "onDestroy:removeGroup: ");
        //            }
        //
        //            @Override
        //            public void onFailure(int i) {
        //                Log.d(TAG, "onDestroy:removeGroup:onFailure ");
        //
        //            }
        //        });
        //
        //        unregisterReceiver(mReceiver);
        finish();


    }

    private void createConnet(String name, final String address) {
        if (connectingProgressDialog != null && connectingProgressDialog.isShowing()) {
            connectingProgressDialog.dismiss();
        }
        connectingProgressDialog = ProgressDialog.show(this, "连接设备", "连接中 :" + address, true, true,
                                                       // cancellable
                                                       new DialogInterface.OnCancelListener() {
                                                           @Override
                                                           public void onCancel(DialogInterface dialog)
                                                           {

                                                               if (mSelectedPostion.getIsLongClick()) {
                                                                   //如果是处于长安状态  那么 就先退出长安状态
                                                                   mSelectedPostion.setLongClick(
                                                                           false);
                                                                   //设置view的消失
                                                                   mIv_fenxiang.setVisibility(View.GONE);
                                                                   mTv_fenxiang_info.setVisibility(
                                                                           View.GONE);
                                                                   if (mInitOrientation == ORIENTATION_LANDSCAPE) {
                                                                       moveBeforeFocusNowColor(
                                                                               mSelectedPostion.getBefore_position_i(),
                                                                               mSelectedPostion.getBefore_position_j() + 1,
                                                                               Color.TRANSPARENT,
                                                                               Color.parseColor(
                                                                                       "#ffffff00"));
                                                                   } else {
                                                                       moveBeforeFocusNowColor(
                                                                               mSelectedPostion.getBefore_position_i(),
                                                                               mSelectedPostion.getBefore_position_j() + 1,
                                                                               Color.TRANSPARENT,
                                                                               Color.TRANSPARENT);
                                                                   }
                                                                   Toast.makeText(mContext,
                                                                                  "取消发送文件",
                                                                                  Toast.LENGTH_SHORT)
                                                                        .show();
                                                                   if (mLv_devices.getVisibility() == View.VISIBLE) {
                                                                       mLv_devices.setVisibility(
                                                                               View.GONE);
                                                                   }
                                                               }
                                                               //这里肯定是Client主动连接设备失效了
                                                               //                                                               mWifiManager.removeGroup(mChannel,
                                                               //                                                                                        new WifiP2pManager.ActionListener() {
                                                               //                                                                                            @Override
                                                               //                                                                                            public void onSuccess() {
                                                               //                                                                                                Log.d(TAG,
                                                               //                                                                                                      "onSuccessremoveGroup:再次 创建服务器2");
                                                               //
                                                               //                                                                                            }
                                                               //
                                                               //                                                                                            @Override
                                                               //                                                                                            public void onFailure(
                                                               //                                                                                                    int i)
                                                               //                                                                                            {
                                                               //                                                                                                Log.d(TAG,
                                                               //                                                                                                      "onFailureremoveGroup:再次 创建服务器");
                                                               //
                                                               //                                                                                            }
                                                               //                                                                                        });
                                                               //                                                               handler.postDelayed(new Runnable() {
                                                               //                                                                   @Override
                                                               //                                                                   public void run() {
                                                               //                                                                       mWifiManager.createGroup(
                                                               //                                                                               mChannel,
                                                               //                                                                               new WifiP2pManager.ActionListener() {
                                                               //                                                                                   @Override
                                                               //                                                                                   public void onSuccess() {
                                                               //
                                                               //                                                                                   }
                                                               //
                                                               //                                                                                   @Override
                                                               //                                                                                   public void onFailure(
                                                               //                                                                                           int i)
                                                               //                                                                                   {
                                                               //
                                                               //                                                                                   }
                                                               //                                                                               });
                                                               //                                                                   }
                                                               //                                                               } , 2000);


                                                           }
                                                       });
        //        final WifiP2pConfig config = new WifiP2pConfig();
        //        config.deviceAddress = address;
        //        config.wps.setup = WpsInfo.PBC;
        //        config.groupOwnerIntent = 0;
        //
        //        mWifiManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
        //
        //            @Override
        //            public void onSuccess() {
        //                LogUtils.logInfo(TAG, "onSuccess: ", "连接成功了");
        //                //再次发送文件
        //
        //            }
        //
        //            @Override
        //            public void onFailure(int reason) {
        //                LogUtils.logInfo(TAG, "onSuccess: ", "连接失败了" + reason);
        //
        //                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT)
        //                     .show();
        //            }
        //        });


    }

    @Override
    public void onScrollViewKeydownChanged(int keyCode) {
        Log.d(TAG, "onScrollViewKeydownChanged: 第一次调用一下？");
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
        if (mScrollView != null) {
            scrollViewHegiht = mScrollView.getMeasuredHeight();
        }
        int remainder = scrollViewHegiht % llItemHeight;
        //看一共有多少行
        int line_num = 0;
        if (mVideos.size() % 2 == 0) {
            //说明是偶数
            line_num = mVideos.size() / 2;
        } else {
            line_num = mVideos.size() / 2 + 1;
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
                if ((position_i * llItemHeight) > mScrollView.getScrollY()) {

                    //说明在范围内
                    Log.d(TAG, "onScrollViewKeydownChanged: 说明在一个范围内");

                } else if ((position_i * llItemHeight) <= mScrollView.getScrollY()) {

                    //那就把这个移动在最底下
                    Log.d(TAG, "onScrollViewKeydownChanged: 说明要一定了");
                    int move_height = scrollViewHegiht - (llItemHeight - (mScrollView.getScrollY() - (position_i * llItemHeight)));
                    mScrollView.smoothScrollBy(0, -move_height);

                }
                moveFocusMedia("move_i", position_i, false);

                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG,
                      "onKeyDown: :::::::1下" + "::" + mSelectedPostion.getPosition_i() + "::" + mSelectedPostion.getPosition_j() + "::" + mVideosName.size());

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

                if (mVideos.size() % 2 != 0) {
                    //说明多出一个
                    if (mSelectedPostion.getPosition_i() == mVideos.size() / 2 - 1) {
                        //到了最后一个
                        if (mSelectedPostion.getPosition_j() == 1) {
                            return;
                        }
                    }
                }


                if ((position_i * llItemHeight) >= mScrollView.getScrollY() && ((position_i + 1) * llItemHeight) <= (mScrollView.getScrollY() + scrollViewHegiht)) {
                    //在里面 并不用去 跳转  只需要把框框加一下


                } else if (((position_i + 1) * llItemHeight) >= (mScrollView.getScrollY() + scrollViewHegiht)) {
                    //这里就说明要重新移动了

                    mScrollView.smoothScrollBy(0,
                                               (position_i * llItemHeight) - mScrollView.getScrollY());

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
                      "onScrollViewKeydownChanged: " + mSelectedPostion.getPosition_i() + (mVideos.size() / 2));
                if (mVideos.size() / 2 != 0) {

                    if (mSelectedPostion.getPosition_i() == (mVideos.size() / 2)) {

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
                LogUtils.prinfLog("输出的selected" + "::i = " + mSelectedPostion.getPosition_i() + "::j = " + mSelectedPostion.getPosition_j());

                Intent intent = new Intent(MainActivity.mContext, VrPlayerActivity.class);
                intent.putStringArrayListExtra("Videos", mVideos);
                intent.putExtra("position",
                                2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
                startActivity(intent);
                break;
        }
    }

    //    private class MyAdatpter
    //            extends BaseAdapter
    //    {
    //
    //        @Override
    //        public int getCount() {
    //            return mPeerLists.size();
    //        }
    //
    //        @Override
    //        public Object getItem(int i) {
    //            return mPeerLists.get(i);
    //        }
    //
    //        @Override
    //        public long getItemId(int i) {
    //            return i;
    //        }
    //
    //        @Override
    //        public View getView(final int i, View convertView, ViewGroup viewGroup) {
    //            Button view;
    //            if (convertView == null) {
    //                view = new Button(mContext);
    //            } else {
    //                view = (Button) convertView;
    //            }
    //
    //            view.setText(mPeerLists.get(i)
    //                                   .get("name"));
    //            view.setOnClickListener(new View.OnClickListener() {
    //                @Override
    //                public void onClick(View view) {
    //                    Log.d(TAG,
    //                          "onClick: 传输数据" + mPeerLists.get(i)
    //                                                      .get("name"));
    //                    createConnet(mPeerLists.get(i)
    //                                           .get("name"),
    //                                 mPeerLists.get(i)
    //                                           .get("address"));
    //                    mBeClickedDeviceName = mPeerLists.get(i)
    //                                                     .get("name");
    //                }
    //            });
    //            return view;
    //        }
    //
    //    }

    public void setOrietationText(int oritation) {
        if (oritation == MainActivity.ORIENTATION_LANDSCAPE) {
            //横屏就进入VR模式
            // mTx_media.setText("媒体库：VR模式");
            // mTx_wifi_tranfer.setVisibility(View.VISIBLE);
            // mTx_wifi_tranfer.setText("媒体库：VR模式");
        } else {
            //mTx_media.setText("媒体库");
            // mTx_wifi_tranfer.setVisibility(View.GONE);
        }
    }


    private void moveSetImageViewBG(int position_i, int position_j, int parseColor) {
//        Log.d(TAG,
//              "moveSetImageViewBG: " + mLl_Arr.size() + position_i + "::" + position_j + "::" + mLl_Arr.get(
//                      position_i)
//                                                                                                       .getChildAt(
//                                                                                                               position_j));
            ImageView iv = (ImageView) ((LinearLayout) mLl_Arr.get(position_i)
                                                              .getChildAt(position_j)).getChildAt(0);
            iv.setBackgroundColor(parseColor);
    }

    public void scrollItemCliclDouble(int i, int j) {
        if (mSelectedPostion.getIsLongClick()) {
            //如果处于长安状态   那么
            //单机时间就不要处理
            return;
        }
        Log.d(TAG, "scrollItemCliclSingle: transfer_file" + i + "::" + j);
        moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffffff00"));


        if (mSelectedPostion.getIsFirst()) {
            mSelectedPostion.setIsFirst(false);
        }
        if(mIsSelctedClick) {

            Intent intent = new Intent(MainActivity.mContext, VrPlayerActivity.class);
            intent.putStringArrayListExtra("Videos", mVideos);
            intent.putExtra("position",
                            2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
            Log.d(TAG, "scrollItemCliclDouble: mSelectedPostion.getPosition_i()"+mSelectedPostion.getPosition_i() +"mSelectedPostion.getPosition_j()"+mSelectedPostion.getPosition_j());
            startActivity(intent);
        }



    }

    public void moveBeforeFocusNowColor(int i, int j, int transparent, int color) {
        if (i == mSelectedPostion.getPosition_i() && (j - 1) == mSelectedPostion.getPosition_j()) {
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






    }

    public void scrollItemLongCliclDouble(int i, int j) {
        //        if (mSelectedPostion.getIsLongClick()) {
        //            //如果处于长安状态  那么长安就不处理
        //            return;
        //        }
        //        if (mSelectedPostion.isDoing()) {
        //            Toast.makeText(MainActivity.mContext, " 正在传输中，等待任务完成再次传输", Toast.LENGTH_SHORT)
        //                 .show();
        //            if( mInitOrientation == ORIENTATION_LANDSCAPE){
        //                mRl_title_container.setVisibility(View.VISIBLE);
        //                if(handler.hasMessages(TITLE_LANDSCAPE_MISS)){
        //                    handler.removeMessages(TITLE_LANDSCAPE_MISS);
        //                }
        //                handler.sendEmptyMessageDelayed(TITLE_LANDSCAPE_MISS , 5000);
        //            }
        //            return;
        //        }else if(mSelectedPostion.isSeverReceving()){
        //            Toast.makeText(MainActivity.mContext, " 正在接受文件中，等待任务完成再次传输", Toast.LENGTH_SHORT)
        //                 .show();
        //            if( mInitOrientation == ORIENTATION_LANDSCAPE){
        //                mRl_title_container.setVisibility(View.VISIBLE);
        //                if(handler.hasMessages(TITLE_LANDSCAPE_MISS)){
        //                    handler.removeMessages(TITLE_LANDSCAPE_MISS);
        //                }
        //                handler.sendEmptyMessageDelayed(TITLE_LANDSCAPE_MISS , 5000);
        //            }
        //            return;
        //
        //        }
        //        Log.d(TAG, "scrollItemLongCliclDouble: 长安 double状态");
        //        //传递一个信息
        //        moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffff0000"));
        //
        //        UserEvent userEvent = new UserEvent(0, "transfer_file");
        //        userEvent.setFileName(mVideos.get(2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j()));
        //        RxBus.getInstance()
        //             .post(userEvent);
        //        mSelectedPostion.setLongClick(true);
        //        MainActivity.mIv_fenxiang.setVisibility(View.VISIBLE);
        //        MainActivity.mTv_fenxiang_info.setVisibility(View.VISIBLE);
        //        MainActivity.mTv_fenxiang_info.setText(mVideosName.get(2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j()) + "被选中");
        //        //如果是横屏的状态 一开始是不存在那个进度条的
        //        if(mRl_title_container.getVisibility() == View.GONE){
        //            //让他出现
        //            mRl_title_container.setVisibility(View.VISIBLE);
        //
        //        }
    }

    public void scrollItemLongCliclSingle(int i, int j) {

        Log.d(TAG, "scrollItemLongCliclDouble: 长安 single状态" + i + "::" + j);
        if (mSelectedPostion.getIsLongClick()) {
            //如果已经处于长安状态
            return;
        }
        if (mSelectedPostion.isDoing()) {
            Toast.makeText(MainActivity.mContext, " 正在传输中，等待任务完成再次传输", Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffff0000"));

        UserEvent userEvent = new UserEvent(0, "transfer_file");
        userEvent.setFileName(mVideos.get(2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j()));
        RxBus.getInstance()
             .post(userEvent);
        mSelectedPostion.setLongClick(true);
        MainActivity.mIv_fenxiang.setVisibility(View.VISIBLE);
        MainActivity.mTv_fenxiang_info.setVisibility(View.VISIBLE);
        MainActivity.mTv_fenxiang_info.setText(mVideosName.get(2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j()) + "被选中");

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
        Intent intent = new Intent(MainActivity.mContext, VrPlayerActivity.class);
        intent.putStringArrayListExtra("Videos", mVideos);
        intent.putExtra("position",
                        2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
        startActivity(intent);

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
    protected void onResume() {
        super.onResume();
        mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }
}