package com.example.lyc.vrexplayer.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lyc.vrexplayer.R;
import com.example.lyc.vrexplayer.Utils.LogUtils;
import com.example.lyc.vrexplayer.Utils.RxBus;
import com.example.lyc.vrexplayer.Utils.SelectedPosition;
import com.example.lyc.vrexplayer.Utils.UserEvent;
import com.example.lyc.vrexplayer.activity.MainActivity;
import com.example.lyc.vrexplayer.activity.VrPlayerActivity;
import com.example.lyc.vrexplayer.application.VrexApplication;
import com.example.lyc.vrexplayer.view.ObservableScrollView;

import java.util.ArrayList;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.fragment
 *  @文件名:   MediaFragment
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/13 14:56
 *  @描述：    TODO
 */
public class MediaFragment
        extends Fragment
        implements ObservableScrollView.ScrollViewKeydownListener
{
    private static final String TAG = "MediaFragment";
    private ArrayList<String> mVideos;
    private ArrayList<String> mVideosName;

    private View                 mInflate;
    private LinearLayout         mLl_container;
    private ObservableScrollView mScrollView;
private SelectedPosition mSelectedPostion = new SelectedPosition();
    private ArrayList<LinearLayout> mLl_Arr = new ArrayList<>();

    public MediaFragment() {}

    ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "==onCreate()执行了");

        super.onCreate(savedInstanceState);
        //第二个
        //初始化数据
    }

    private void initData() {

        mVideos = new ArrayList<>();
        mVideosName = new ArrayList<>();


        ContentResolver contentResolver = MainActivity.mContext.getContentResolver();

        Uri           uri   = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Images.Media.TITLE + " != ''");
        String[] projection = new String[]{MediaStore.Video.Media.TITLE,
                                           MediaStore.Video.Media.DATA};
        where.append(" AND " + MediaStore.Images.Media.DATA + " LIKE '%" + "VrexPlayer" + "%'");
        final Cursor cursor = contentResolver.query(uri,
                                                    projection,
                                                    where.toString(),
                                                    null,
                                                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            Toast.makeText(MainActivity.mContext, "No video file is found !", Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        if (cursor.moveToFirst()) {
            do {
                String path  = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                Log.v(TAG, "[getImagess]path = " + path);
                mVideos.add(path);
                mVideosName.add(title);
            } while (cursor.moveToNext());
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.i(TAG, "==onCreateView()执行了");
        // inflater.inflate(resource, null);
        mInflate = inflater.inflate(R.layout.fragment_media, container, false);

        return mInflate;

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "==onViewCreated()执行了");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "==onActivityCreated()执行了");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.i(TAG, "==onStart()执行了");
        super.onStart();
        initData();


        initView();

    }

    private void initView() {
        mScrollView = (ObservableScrollView) mInflate.findViewById(R.id.scroll_view);
        mLl_container = (LinearLayout) mInflate.findViewById(R.id.ll_scrollview_container);
        Log.d(TAG, "initView: " + VrexApplication.mInitOrientation);
        if (VrexApplication.mInitOrientation == MainActivity.ORIENTATION_PROTRAIT) {
            //加载一个
            setProtraitOrientation();
            Log.d(TAG, "initView: 加载一个");
        } else if (VrexApplication.mInitOrientation == MainActivity.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "initView: 加载两个");
            setLandscapeOrientation();
        }

        mScrollView.setOnScrollViewKeydownListener(this);
        mScrollView.setOrientation(VrexApplication.mInitOrientation);

        if (!mSelectedPostion.getIsFirst()) {
            //那么久要进行某一个 方框的xuanze
            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j(),
                               Color.parseColor("#ffffff00"));
            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j() + 2,
                               Color.parseColor("#ffffff00"));
        }

    }

    public void configurationChange(int oritation) {
        switch (oritation) {
            case MainActivity.ORIENTATION_PROTRAIT:
                Log.d(TAG, "mediaconfigurationChange: 竖屏");
                setProtraitOrientation();
                mScrollView.setOrientation(MainActivity.ORIENTATION_PROTRAIT);
                VrexApplication.mInitOrientation = MainActivity.ORIENTATION_PROTRAIT;
                break;
            case MainActivity.ORIENTATION_LANDSCAPE:
                Log.d(TAG, "mediaconfigurationChange: 横屏");
                setLandscapeOrientation();
                mScrollView.setOrientation(MainActivity.ORIENTATION_LANDSCAPE);
                VrexApplication.mInitOrientation = MainActivity.ORIENTATION_LANDSCAPE;

                break;
            default:
                break;
        }
    }

    public void setInitOrietation(int initOrientation) {
        VrexApplication.mInitOrientation = initOrientation;
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
                 .sizeMultiplier(0.75f)
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i + 1))
                 .sizeMultiplier(0.75f)
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
                     .sizeMultiplier(0.75f)
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
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i))
                 .sizeMultiplier(0.75f)
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_1);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i + 1))
                 .sizeMultiplier(0.75f)
                 .placeholder(R.mipmap.ic_launcher)

                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_2);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i))
                 .sizeMultiplier(0.75f)
                 .placeholder(R.mipmap.ic_launcher)
                 .diskCacheStrategy(DiskCacheStrategy.RESULT)
                 .into(iv_3);
            Glide.with(MainActivity.mContext)
                 .load(mVideos.get(2 * i + 1))
                 .sizeMultiplier(0.75f)
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
                     .sizeMultiplier(0.75f)

                     .placeholder(R.mipmap.ic_launcher)
                     .diskCacheStrategy(DiskCacheStrategy.RESULT)
                     .into(iv_double_1);

                Glide.with(MainActivity.mContext)
                     .load(mVideos.get(mVideos.size() - 1))
                     .sizeMultiplier(0.75f)
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
                mLl_container.addView(inflate1);
            }
        }
    }

    public void scrollItemCliclDouble(int i, int j) {
        if (mSelectedPostion.getIsLongClick()) {
            //如果处于长安状态   那么
            //单机时间就不要处理
            return;
        }
        moveBeforeFocusNowColor(i, j, Color.TRANSPARENT, Color.parseColor("#ffffff00"));

        ImageView childAt = (ImageView) ((LinearLayout) mLl_Arr.get(i)
                                                               .getChildAt(j - 1)).getChildAt(0);

        childAt.setImageResource(R.mipmap.aq1);


        ImageView childAt2 = (ImageView) ((LinearLayout) mLl_Arr.get(i)
                                                                .getChildAt(j + 2 - 1)).getChildAt(0);
        childAt2.setImageResource(R.mipmap.aq1);
        Log.d(TAG, "scrollItemCliclSingle: transfer_file" + i + "::" + j);

        if (mSelectedPostion.getIsFirst()) {
            mSelectedPostion.setIsFirst(false);
        }
        Intent intent = new Intent(MainActivity.mContext, VrPlayerActivity.class);
        intent.putStringArrayListExtra("Videos", mVideos);
        intent.putExtra("position",
                        2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
        startActivity(intent);


    }

    public void moveBeforeFocusNowColor(int i, int j, int transparent, int color) {
        mSelectedPostion.setBefore_position_i(mSelectedPostion.getPosition_i());
        mSelectedPostion.setBefore_position_j(mSelectedPostion.getPosition_j());
        mSelectedPostion.setPosition_i(i);
        mSelectedPostion.setPosition_j(j - 1);
        //把之前的变成 无边框
        //把选中的变成 有边框

        if (VrexApplication.mInitOrientation == MainActivity.ORIENTATION_LANDSCAPE) {
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
        } else if (VrexApplication.mInitOrientation == MainActivity.ORIENTATION_PROTRAIT) {

            moveSetImageViewBG(mSelectedPostion.getBefore_position_i(),
                               mSelectedPostion.getBefore_position_j(),
                               transparent);
            moveSetImageViewBG(mSelectedPostion.getPosition_i(),
                               mSelectedPostion.getPosition_j(),
                               color);

        }


    }

    public void scrollItemLongCliclDouble(int i, int j) {
        if (mSelectedPostion.getIsLongClick()) {
            //如果处于长安状态  那么长安就不处理
            return;
        }
        if (mSelectedPostion.isDoing()) {
            Toast.makeText(MainActivity.mContext, " 正在传输中，等待任务完成再次传输", Toast.LENGTH_SHORT)
                 .show();
            return;
        }
        Log.d(TAG, "scrollItemLongCliclDouble: 长安 double状态");
        //传递一个信息
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
        ImageView childAt = (ImageView) ((LinearLayout) mLl_Arr.get(i)
                                                               .getChildAt(j - 1)).getChildAt(0);

        childAt.setImageResource(R.mipmap.aq1);

        Intent intent = new Intent(MainActivity.mContext, VrPlayerActivity.class);
        intent.putStringArrayListExtra("Videos", mVideos);
        intent.putExtra("position",
                        2 * mSelectedPostion.getPosition_i() + mSelectedPostion.getPosition_j());
        startActivity(intent);

    }

    @Override
    public void onScrollViewKeydownChanged(int keyCode) {
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

    private void moveSetImageViewBG(int position_i, int position_j, int parseColor) {
        Log.d(TAG, "moveSetImageViewBG: "+mLl_Arr.size() + position_i +"::"+ position_j +"::"+mLl_Arr.get(position_i)
                                                                                                     .getChildAt(position_j));
        ImageView iv = (ImageView) ((LinearLayout) mLl_Arr.get(position_i)
                                                          .getChildAt(position_j)).getChildAt(0);
        iv.setBackgroundColor(parseColor);

    }
}
