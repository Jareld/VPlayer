package com.example.lyc.vrexplayer.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.widget.ImageView;

import com.example.lyc.vrexplayer.Utils.LogUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/*
 *  @项目名：  TestWifiDerect 
 *  @包名：    com.example.lyc2.testwifiderect.service
 *  @文件名:   CameraDataTrandsferTask
 *  @创建者:   LYC2
 *  @创建时间:  2016/11/29 14:37
 *  @描述：    TODO
 */
public class CameraDataTrandsferService
        extends IntentService
{
    private static final String TAG                        = "CameraDataTrandsferTask";
    public static final  String EXTRAS_GROUP_OWNER_ADDRESS = "address";
    public static final  String EXTRAS_GROUP_OWNER_PORT    = "port";
    private static final int    SOCKET_TIMEOUT             = 5000;
    private static Socket               mSocket;
    private static OutputStream         mOutputStream;
    private static BufferedWriter       mWriter;
    private static ImageView            mIv_test_camera;
    private static int                  mWidth;
    private static int                  mHeight;
    private static BufferedOutputStream mBufferedOutputStream;
    private static int                  geshu;


    public CameraDataTrandsferService() {
        super("CameraDataTrandsferService");
    }

    public CameraDataTrandsferService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("HY测试", "FileTransferService：onHandleIntent: ");
        Context context = getApplicationContext();

        String host = intent.getExtras()
                            .getString(EXTRAS_GROUP_OWNER_ADDRESS);

        mSocket = new Socket();
        try {
            LogUtils.logInfo(TAG,
                             "onHandleIntent",
                             "设置前=" + mSocket.getSendBufferSize() + "地址：getInetAddress" + mSocket.getInetAddress() + "::getLocalAddress" + mSocket.getLocalAddress());

            mSocket.setReuseAddress(true);
        } catch (SocketException e) {

            e.printStackTrace();
        }
        int port = intent.getExtras()
                         .getInt(EXTRAS_GROUP_OWNER_PORT);

        try {
            Log.d("hy", "Opening client socket - host=" + host + "port" + port);
            mSocket.bind(null);

            mSocket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
            mSocket.setKeepAlive(true);
            mOutputStream = mSocket.getOutputStream();

            mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            mBufferedOutputStream = new BufferedOutputStream(mSocket.getOutputStream());

            LogUtils.logInfo(TAG, "onHandleIntent", "设置后=" + mSocket.getSendBufferSize());

        } catch (Exception e) {

        }
        LogUtils.logInfo(TAG, "onHandleIntent", "在这里查看一下有没有连接起来" + mSocket.isConnected());
    }



    public static void tranfeData(byte[] datas) {

        //  String name = Thread.currentThread()  .getName();
        geshu++;

        Log.d(TAG, "tranfeData: 传输的次数" + geshu);
        //  LogUtils.logInfo(TAG, "在传输了多少次", "传输的次数" + geshu +"name= "+name);


        //        Log.d(TAG,
        //              "tranfeData: " + datas.length + "geshu=" + geshu + "宽=" + mWidth + "高=" + mHeight);

        //        try {
        //            YuvImage image = new YuvImage(datas, ImageFormat.NV21, mWidth, mHeight, null);
        //            if (image != null) {
        //                ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //                image.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 80, mSocket.getOutputStream());
        //                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        //
        //                stream.close();
        //            }
        //        } catch (Exception ex) {
        //            Log.e("Sys", "Error:" + ex.getMessage());
        //
        //        } finally {
        //            try {
        //                mSocket.close();
        //                mOutputStream.close();
        //
        //            } catch (IOException e) {
        //                e.printStackTrace();
        //            }
        //        }
        try {

            //mWriter.flush();
            //
            //            mSocket.getOutputStream()
            //                   .flush();

            //            mSocket.getOutputStream()
            //                   .write(datas);
            //
            //                      mSocket.getOutputStream()
            //                                .flush();

            //            mBufferedOutputStream.write(datas);
            //            mBufferedOutputStream.flush();

            mOutputStream.write(datas);

            //   mOutputStream.write(datas);
            //            mOutputStream.write(datas);
            //            mOutputStream.flush();
            //  LogUtils.logInfo(TAG, "sendData", "发送出去的信息" + img.getYuvData().length);

            //Bitmap bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length);

            //mIv_test_camera.setImageBitmap(bitmap);

            //mWriter.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void closeSocket() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setImageView(ImageView iv_test_camera) {
        // mIv_test_camera = iv_test_camera;
    }

    public static void setPreviewSize(Camera.Size previewSize) {

        mWidth = previewSize.width;
        mHeight = previewSize.height;


    }
}
