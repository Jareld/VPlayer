package com.example.lyc.vrexplayer.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.view.DoubleSeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.activity
 *  @文件名:   VrPlayerActivity
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/17 10:43
 *  @描述：    TODO
 */
public class VrPlayerActivity
        extends AppCompatActivity
        implements DoubleSeekBar.StopTrackingTouchListener,
                   MediaPlayer.OnPreparedListener,
                   View.OnClickListener
{
    private static final String TAG = "VrPlayerActivity";
    private String            FILEPATH;
    private VideoView         mVvVRPlayer;
    private DoubleSeekBar     mDoubleSeekBar;
    private LinearLayout      mLl_play;
    private int               mPosition;
    private ArrayList<String> mVideos;
    private int               mMax;
    private int               seekBarStep;
    private Timer             mTimer;
    private TimerTask         mTimerTask;
    private static final int DOUBLE_SEEKBAR_MISS = 1;
    private static final int DOUBLE_VOL_MISS     = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOUBLE_SEEKBAR_MISS:
                    mDoubleSeekBar.setVisibility(View.GONE);
                    break;
                case DOUBLE_VOL_MISS:
                    mLl_vr_volmu.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };
    private View            mLayout;
    private AudioManager    mAudioManager;
    private int             mCurrentVolume;
    private int             mVolumeMax;
    private LinearLayout    mLl_vr_volmu;
    private TextView        mTvVRVolLeft;
    private TextView        mTvVRVolRight;
    private ImageView       mTv_play_pause_left;
    private ImageView       mTv_play_pause_right;
    private GestureDetector mGestureDetector;
    private LinearLayout    mLl_video_next_or_replay;
    private Button          mBtn_left_video_next;
    private Button          mBtn_left_video_replay;
    private Button          mBtn_right_video_next;
    private Button          mBtn_right_video_replay;
    private int             status_play_or_next;
    private static final int STATUS_PLAYING        = 3;
    private static final int STATUS_NEXT_OR_REPLAY = 4;
    private Display mDefaultDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = getLayoutInflater().from(this)
                                     .inflate(R.layout.activity_vr_player, null);
        getSupportActionBar().hide();
        this.getWindow()
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                      WindowManager.LayoutParams.FLAG_FULLSCREEN);
      //  initDisplay();
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
                if (mVvVRPlayer.isPlaying()) {
                    mVvVRPlayer.pause();
                    mDoubleSeekBar.setVisibility(View.VISIBLE);
                    mLl_play.setVisibility(View.VISIBLE);

                } else {
                    mVvVRPlayer.start();
                    mDoubleSeekBar.setVisibility(View.GONE);
                    mLl_play.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initDisplay() {


        Display.Mode mode = getWindowManager().getDefaultDisplay()
                                              .getMode();
        int physicalHeight = mode.getPhysicalHeight();
        int physicalWidth  = mode.getPhysicalWidth();
        int modeId         = mode.getModeId();

        int displayId = getWindowManager().getDefaultDisplay()
                                          .getDisplayId();


        float refreshRate = mode.getRefreshRate();
        Log.d(TAG,
              "onClick:11 " + "::" + modeId + "::" + physicalWidth + "::" + physicalHeight + "::" + refreshRate + "::" + displayId);

        mDefaultDisplay = getWindowManager().getDefaultDisplay();
        Display.Mode[] supportedModes = mDefaultDisplay.getSupportedModes();
        for (Display.Mode mode1 : supportedModes) {
            Log.d(TAG,
                  "onClick: 22" + mode1.getPhysicalWidth() + "::" + mode1.getPhysicalHeight() + "::" +
                          mode1.getModeId() + "::" + mode1.getRefreshRate());
        }

        //
        Window                     window    = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        Log.d(TAG, "onClick: 44" + mDefaultDisplay.getDisplayId());
        int preferredDisplayModeId = winParams.preferredDisplayModeId;
        Log.d(TAG, "onClick:33 " + preferredDisplayModeId);
        winParams.preferredDisplayModeId = 2;
        winParams.preferredRefreshRate = 60.0f;
        Log.d(TAG, "onClick: 44" + mDefaultDisplay.getDisplayId());

        WindowManager.LayoutParams attributes = window.getAttributes();
        Log.d(TAG,
              "onClick: 44" + mDefaultDisplay.getDisplayId() + "::" + attributes.preferredDisplayModeId);
        window.setAttributes(winParams);


    }

    private void initEvent() {
        mDoubleSeekBar.setOnStopTrackingTouchListener(this);
        mVvVRPlayer.setOnPreparedListener(this);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mVvVRPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onCompletion: 播放完成");
                status_play_or_next = STATUS_NEXT_OR_REPLAY;
                mLl_video_next_or_replay.setVisibility(View.VISIBLE);
                mBtn_left_video_next.setSelected(true);
                mBtn_left_video_replay.setSelected(false);
                mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
            }
        });

        mBtn_left_video_next.setOnClickListener(this);
        mBtn_left_video_replay.setOnClickListener(this);
        mBtn_right_video_next.setOnClickListener(this);
        mBtn_right_video_replay.setOnClickListener(this);

    }


    private void initData() {
        mPosition = getIntent().getIntExtra("position", -1);
        Uri uri;
        Log.d(TAG, "initData: "+mPosition);
        if (mPosition != -1) {
            mVideos = getIntent().getStringArrayListExtra("Videos");
            FILEPATH = mVideos.get(mPosition);
            uri = Uri.parse(FILEPATH);
        } else {
            uri = getIntent().getData();
        }
        Log.d(TAG,
              "initData: " + mPosition + "::" + mVideos.get(mPosition) + "::" + uri.toString() + "::" + mVvVRPlayer);

        mVvVRPlayer.setVideoPath(uri.toString());

        initVolume();
    }

    private void initVolume() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //Log.d(TAG, "initVolume: " + mVolumeMax + "::" + mCurrentVolume);

        int             value = 0;
        ContentResolver cr    = getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {


        }
    }

    private void initViews() {
        mVvVRPlayer = (VideoView) findViewById(R.id.vv_vr_player);
        mDoubleSeekBar = (DoubleSeekBar) findViewById(R.id.double_seekbar);
        mLl_play = (LinearLayout) findViewById(R.id.ll_vr_play);
        mLl_vr_volmu = (LinearLayout) findViewById(R.id.ll_vr_volume);
        mTvVRVolLeft = (TextView) findViewById(R.id.tv_ll_vr_volume_left);
        mTvVRVolRight = (TextView) findViewById(R.id.tv_ll_vr_volume_right);
        mTv_play_pause_left = (ImageView) findViewById(R.id.tv_play_pause_left);
        mTv_play_pause_right = (ImageView) findViewById(R.id.tv_play_pause_right);
        mLl_video_next_or_replay = (LinearLayout) findViewById(R.id.ll_video_next_or_replay);

        mBtn_left_video_next = (Button) findViewById(R.id.btn_video_next_left);
        mBtn_left_video_replay = (Button) findViewById(R.id.btn_video_replay_left);
        mBtn_right_video_next = (Button) findViewById(R.id.btn_video_next_right);
        mBtn_right_video_replay = (Button) findViewById(R.id.btn_video_replay_right);


    }

    @Override
    public void stopDoubleTouch(int progress) {
        //告诉他 拖动的时候停止在了哪里
        Log.d(TAG, "stopDoubleTouch: kan shibushi");
        mVvVRPlayer.seekTo(mDoubleSeekBar.getDoubleProgress());
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //准备好后 初始化seekbar的内容
        initSeekBar();
        //开始播放
        mVvVRPlayer.start();

        status_play_or_next = STATUS_PLAYING;
        //初始化事件
        initTimer();
    }

    public static String getTimeShort(int time) {

        SimpleDateFormat formatter;
        if (time > 60 * 60 * 1000) {
            formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            //Log.d(TAG, "initSeekBar: initSeekBar: " + formatter.format(time) + "time" + time);

        } else {
            ////Log.d(TAG, "getTimeShort: NO HH");

            formatter = new SimpleDateFormat("mm:ss");

        }
        String dateString = formatter.format(time);
        ////Log.d(TAG, "getTimeShort: " + dateString);

        return dateString;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                Log.d(TAG, "onKeyDown: 声音高");
                mCurrentVolume++;
                if (mCurrentVolume == mVolumeMax + 1) {
                    //说明超纲了
                    mCurrentVolume = mVolumeMax;
                }
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                              mCurrentVolume,
                                              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                mLl_vr_volmu.setVisibility(View.VISIBLE);
                mTvVRVolLeft.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                mTvVRVolRight.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                if (mHandler.hasMessages(DOUBLE_VOL_MISS)) {
                    mHandler.removeMessages(DOUBLE_VOL_MISS);
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                } else {
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG, "onKeyDown: 声音低");
                mCurrentVolume--;
                if (mCurrentVolume == -1) {
                    mCurrentVolume = 0;
                }
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                              mCurrentVolume,
                                              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                mLl_vr_volmu.setVisibility(View.VISIBLE);
                mTvVRVolLeft.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                mTvVRVolRight.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                if (mHandler.hasMessages(DOUBLE_VOL_MISS)) {
                    mHandler.removeMessages(DOUBLE_VOL_MISS);
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                } else {
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                }


                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:

                if (status_play_or_next == STATUS_PLAYING) {
                    mDoubleSeekBar.setVisibility(View.VISIBLE);
                    if (mDoubleSeekBar.getDoubleProgress() - (mMax / seekBarStep) <= 0) {
                        mVvVRPlayer.seekTo(0);

                    } else {
                        Log.d(TAG,
                              "onKeyDown: 进入到这里来前进mMax -   mDoubleSeekBar.getDoubleProgress() " + mMax + mDoubleSeekBar.getDoubleProgress());
                        mVvVRPlayer.seekTo(mDoubleSeekBar.getDoubleProgress() - (mMax / seekBarStep));
                        Log.d(TAG,
                              "onKeyDown: 进入到这里来前进mMax -   mDoubleSeekBar.getDoubleProgress() " + mMax + mDoubleSeekBar.getDoubleProgress());

                    }
                    if (mHandler.hasMessages(DOUBLE_SEEKBAR_MISS)) {
                        //移除任务 重新发布分吴
                        mHandler.removeMessages(DOUBLE_SEEKBAR_MISS);
                        mHandler.sendEmptyMessageDelayed(DOUBLE_SEEKBAR_MISS, 3000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(DOUBLE_SEEKBAR_MISS, 3000);
                    }
                } else if (status_play_or_next == STATUS_NEXT_OR_REPLAY) {
                    Log.d(TAG, "onKeyDown: 处理右边时间");

                    if (mBtn_left_video_replay.isSelected()) {
                        //在左边 处理一下
                        mBtn_left_video_next.setSelected(true);
                        mBtn_left_video_replay.setSelected(false);
                        mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
                        mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
                        mBtn_left_video_replay.setTextColor(Color.parseColor("#000000"));
                        mBtn_right_video_replay.setTextColor(Color.parseColor("#000000"));
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (status_play_or_next == STATUS_PLAYING) {

                    mDoubleSeekBar.setVisibility(View.VISIBLE);

                    if (mDoubleSeekBar.getDoubleProgress() + (mMax / seekBarStep) >= mMax) {
                        mVvVRPlayer.seekTo(mMax);
                        //// TODO: 2017/3/27 这里到了最后之后 是要播放下一曲还是继续播放这


                    } else {
                        Log.d(TAG,
                              "onKeyDown: 进入到这里来前进mMax +   mDoubleSeekBar.getDoubleProgress() " + mMax + mDoubleSeekBar.getDoubleProgress());

                        mVvVRPlayer.seekTo(mDoubleSeekBar.getDoubleProgress() + (mMax / seekBarStep));
                        Log.d(TAG,
                              "onKeyDown: 进入到这里来前进mMax +   mDoubleSeekBar.getDoubleProgress() " + mMax + mDoubleSeekBar.getDoubleProgress());

                    }
                    if (mHandler.hasMessages(DOUBLE_SEEKBAR_MISS)) {
                        //移除任务 重新发布分吴
                        mHandler.removeMessages(DOUBLE_SEEKBAR_MISS);
                        mHandler.sendEmptyMessageDelayed(DOUBLE_SEEKBAR_MISS, 3000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(DOUBLE_SEEKBAR_MISS, 3000);

                    }
                } else if (status_play_or_next == STATUS_NEXT_OR_REPLAY) {
                    Log.d(TAG, "onKeyDown: 处理左边的时间");

                    if (mBtn_left_video_next.isSelected()) {
                        //在左边 处理一下
                        mBtn_left_video_next.setSelected(false);
                        mBtn_left_video_replay.setSelected(true);
                        mBtn_left_video_next.setTextColor(Color.parseColor("#000000"));
                        mBtn_right_video_next.setTextColor(Color.parseColor("#000000"));
                        mBtn_left_video_replay.setTextColor(Color.parseColor("#ff0000"));
                        mBtn_right_video_replay.setTextColor(Color.parseColor("#ff0000"));
                    }


                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:

                if (status_play_or_next == STATUS_PLAYING) {
                    if (mVvVRPlayer.isPlaying()) {
                        mLl_play.setVisibility(View.VISIBLE);
                        mVvVRPlayer.pause();
                    } else {
                        mLl_play.setVisibility(View.GONE);
                        mVvVRPlayer.start();
                    }
                } else if (status_play_or_next == STATUS_NEXT_OR_REPLAY) {

                    if (mBtn_left_video_next.isSelected()) {
                        //这里就不需要重置状态了
                        Log.d(TAG, "onKeyDown: 播放下一曲");
                        if(mPosition == mVideos.size() - 1){
                            //说明到顶了 重新开始播放
                            mPosition = 0 ;
                        }else{
                            mPosition++;
                        }

                        String s = mVideos.get(mPosition);
                        Uri    parse = Uri.parse(s);
                        mVvVRPlayer.setVideoPath(parse.toString());
                        mVvVRPlayer.start();

                    } else {
                        //再看一遍
                        Log.d(TAG, "onKeyDown: 再看一遍");
                        mVvVRPlayer.seekTo(0);
                        mVvVRPlayer.start();
                    }
                    //重置状态
                    status_play_or_next = STATUS_PLAYING;
                    mLl_video_next_or_replay.setVisibility(View.GONE);
                    mBtn_left_video_next.setSelected(true);
                    mBtn_left_video_replay.setSelected(false);
                    mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
                    mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
                    mBtn_left_video_replay.setTextColor(Color.parseColor("#000000"));
                    mBtn_right_video_replay.setTextColor(Color.parseColor("#000000"));

                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d(TAG, "onKeyDown: 声音低");
                mCurrentVolume--;
                if (mCurrentVolume == -1) {
                    mCurrentVolume = 0;
                }
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                              mCurrentVolume,
                                              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                mLl_vr_volmu.setVisibility(View.VISIBLE);
                mTvVRVolLeft.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                mTvVRVolRight.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                if (mHandler.hasMessages(DOUBLE_VOL_MISS)) {
                    mHandler.removeMessages(DOUBLE_VOL_MISS);
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                } else {
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d(TAG, "onKeyDown: 声音高");
                mCurrentVolume++;
                if (mCurrentVolume == mVolumeMax + 1) {
                    //说明超纲了
                    mCurrentVolume = mVolumeMax;
                }
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                              mCurrentVolume,
                                              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                mLl_vr_volmu.setVisibility(View.VISIBLE);
                mTvVRVolLeft.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                mTvVRVolRight.setText((int) (mCurrentVolume * 100 / mVolumeMax) + "%");
                if (mHandler.hasMessages(DOUBLE_VOL_MISS)) {
                    mHandler.removeMessages(DOUBLE_VOL_MISS);
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                } else {
                    mHandler.sendEmptyMessageDelayed(DOUBLE_VOL_MISS, 3000);
                }
                return true;

        }
        return super.onKeyDown(keyCode, event);

    }

    //初始化seekbar需要的东西
    public void initSeekBar() {
        mMax = mVvVRPlayer.getDuration();
        if (mMax <= 60000) {
            //如果时间段 就不要 这么小的跳跃
            seekBarStep = 10;
        } else if (mMax <= 120000) {
            seekBarStep = 20;
        } else {
            seekBarStep = 20;
        }
        Log.d(TAG, "initSeekBar: " + seekBarStep);
        mDoubleSeekBar.setDoubleMax(mMax);
        mDoubleSeekBar.setDoubleDuration(getTimeShort(mVvVRPlayer.getDuration()));
        mHandler.sendEmptyMessageDelayed(DOUBLE_SEEKBAR_MISS, 3000);
    }

    public void initTimer() {
        Log.v(TAG, "initTimer");
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mDoubleSeekBar.isTouchTraching) {
                    return;
                }
                //Log.d(TAG, "run: 看一下状态" + controller_status);
                mDoubleSeekBar.setDoubleProgress(mVvVRPlayer.getCurrentPosition());
                //更新 左边position的状态
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDoubleSeekBar.SetDoublePosition(getTimeShort(mVvVRPlayer.getCurrentPosition()));

                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 0, 200);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mVvVRPlayer != null) {
            mVvVRPlayer.stopPlayback();
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mHandler.hasMessages(DOUBLE_VOL_MISS)) {
            mHandler.removeCallbacksAndMessages(DOUBLE_VOL_MISS);
        }
        if (mHandler.hasMessages(DOUBLE_SEEKBAR_MISS)) {
            mHandler.removeCallbacksAndMessages(DOUBLE_SEEKBAR_MISS);
        }


        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_video_next_left:
                if(mPosition == mVideos.size() - 1){
                    //说明到顶了 重新开始播放
                    mPosition = 0 ;
                }else{
                    mPosition++;
                }

            {
                String s     = mVideos.get(mPosition);
                Uri    parse = Uri.parse(s);
                mVvVRPlayer.setVideoPath(parse.toString());
                mVvVRPlayer.start();
                status_play_or_next = STATUS_PLAYING;
                mLl_video_next_or_replay.setVisibility(View.GONE);
                mBtn_left_video_next.setSelected(true);
                mBtn_left_video_replay.setSelected(false);
                mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_left_video_replay.setTextColor(Color.parseColor("#000000"));
                mBtn_right_video_replay.setTextColor(Color.parseColor("#000000"));
            }
                break;
            case R.id.btn_video_next_right:
                if(mPosition == mVideos.size() - 1){
                    //说明到顶了 重新开始播放
                    mPosition = 0 ;
                }else{
                    mPosition++;
                }

                String s = mVideos.get(mPosition);
                Uri    parse = Uri.parse(s);
                mVvVRPlayer.setVideoPath(parse.toString());
                mVvVRPlayer.start();
                status_play_or_next = STATUS_PLAYING;
                mLl_video_next_or_replay.setVisibility(View.GONE);
                mBtn_left_video_next.setSelected(true);
                mBtn_left_video_replay.setSelected(false);
                mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_left_video_replay.setTextColor(Color.parseColor("#000000"));
                mBtn_right_video_replay.setTextColor(Color.parseColor("#000000"));
                break;
            case R.id.btn_video_replay_left:
                mVvVRPlayer.seekTo(0);
                mVvVRPlayer.start();
        //重置状态
        status_play_or_next = STATUS_PLAYING;
        mLl_video_next_or_replay.setVisibility(View.GONE);
        mBtn_left_video_next.setSelected(true);
        mBtn_left_video_replay.setSelected(false);
        mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
        mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
        mBtn_left_video_replay.setTextColor(Color.parseColor("#000000"));
        mBtn_right_video_replay.setTextColor(Color.parseColor("#000000"));

        break;
            case R.id.btn_video_replay_right:
                mVvVRPlayer.seekTo(0);
                mVvVRPlayer.start();
                //重置状态
                status_play_or_next = STATUS_PLAYING;
                mLl_video_next_or_replay.setVisibility(View.GONE);
                mBtn_left_video_next.setSelected(true);
                mBtn_left_video_replay.setSelected(false);
                mBtn_left_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_right_video_next.setTextColor(Color.parseColor("#ff0000"));
                mBtn_left_video_replay.setTextColor(Color.parseColor("#000000"));
                mBtn_right_video_replay.setTextColor(Color.parseColor("#000000"));

                break;
            default:
                break;
        }
    }
}
