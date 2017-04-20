package com.example.lyc.vrexplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.lyc.vrexplayer.R;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.view
 *  @文件名:   DoubleSeekBar
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/17 10:11
 *  @描述：    TODO
 */
public class DoubleSeekBar extends LinearLayout
        implements SeekBar.OnSeekBarChangeListener
{
    private static final String TAG = "DoubleSeekBar";
    private TextView mTvRightPosition;
    private TextView mTvLeftPosition;
    private TextView mTvRightDuration;
    private TextView mTvLeftDuration;
    private SeekBar mSeekBarRight;
    private SeekBar mSeekBarLeft;
    public boolean isTouchTraching;

    public DoubleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initEvent();
    }

    private void initEvent() {
        mSeekBarLeft.setOnSeekBarChangeListener(this);
        mSeekBarRight.setOnSeekBarChangeListener(this);
    }

    private void initView(Context context) {
        View rootView = View.inflate(context , R.layout.seekbar_double , this);
        mSeekBarLeft = (SeekBar) rootView.findViewById(R.id.seekBar_left);
        mSeekBarRight = (SeekBar) rootView.findViewById(R.id.seekBar_right);
        mTvLeftDuration = (TextView) rootView.findViewById(R.id.tvDur_left);
        mTvRightDuration = (TextView) rootView.findViewById(R.id.tvDur_right);
        mTvLeftPosition = (TextView) rootView.findViewById(R.id.tvPos_left);
        mTvRightPosition = (TextView) rootView.findViewById(R.id.tvPos_right);

    }
    //设置最大
    public void setDoubleMax(int progress){
        mSeekBarLeft.setMax(progress);
        mSeekBarRight.setMax(progress);
    }

    //设置 进度
    public void setDoubleProgress(int progress){
        mSeekBarLeft.setProgress(progress);
        mSeekBarRight.setProgress(progress);
    }

    //设置Position
    public void SetDoublePosition(String position){
        mTvLeftPosition.setText(position);
        mTvRightPosition.setText(position);
    }

    //设置Duration
    public void setDoubleDuration(String duration){
        mTvLeftDuration.setText(duration);
        mTvRightDuration.setText(duration);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        switch (seekBar.getId()){
            case R.id.seekBar_left:
                //左边动  右边随着动
                mSeekBarRight.setProgress(mSeekBarLeft.getProgress());
                break;
            case  R.id.seekBar_right:
                //右边动 左边也动
                mSeekBarLeft.setProgress(mSeekBarRight.getProgress());
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTouchTraching = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch: video移动到哪里"+seekBar.getProgress());

if(mStopTrackingTouchListener!=null){

    mStopTrackingTouchListener.stopDoubleTouch(seekBar.getProgress());
    isTouchTraching =false;
}
    }

    //监听的反馈

private StopTrackingTouchListener mStopTrackingTouchListener;
    public interface StopTrackingTouchListener{
    public void stopDoubleTouch(int progress);
};
public void setOnStopTrackingTouchListener(StopTrackingTouchListener stopTrackingTouchListener){
    mStopTrackingTouchListener = stopTrackingTouchListener;
}
    public int getDoubleProgress(){
        int progress = mSeekBarLeft.getProgress();
        return progress;
    }













}
