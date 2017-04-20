package com.example.lyc.vrexplayer.task;

import android.media.MediaPlayer;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;

import com.example.lyc.vrexplayer.Utils.CameraUtil;
import com.example.lyc.vrexplayer.Utils.LogUtils;
import com.example.lyc.vrexplayer.glsurface.GLFrameRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/*
 *  @项目名：  TestWifiDerect 
 *  @包名：    com.example.lyc2.testwifiderect.task
 *  @文件名:   CameraDataReciverTask
 *  @创建者:   LYC2
 *  @创建时间:  2016/11/29 14:55
 *  @描述：    TODO
 */
public class CameraDataReciverTask
        extends AsyncTask<Void, Void, String>
{
    private static final String TAG            = "hyCameraDataReciverTask";
    private static final String SOCKET_ADDRESS = "LOCAL_SOCKET_ADDRESS";
    private GLFrameRenderer mGlFrameRenderer;
    private Socket          mClient;
    private ServerSocket    mServerSocket;
    private String          mMessage;
    private String          mEchoMessage;
    private String          mFrameString;

    private Handler mHandler = new Handler();
    private LocalServerSocket localServerSocket;
    private LocalSocket       localSocketSender;
    private LocalSocket       localSocketReceiver;
    private MediaPlayer       mMediaPlayer;
    private SurfaceView       mSurfaceView;

    public CameraDataReciverTask(GLFrameRenderer glFrameRenderer) {
        mGlFrameRenderer = glFrameRenderer;
    }

    public CameraDataReciverTask() {

    }

    public CameraDataReciverTask(GLFrameRenderer glFrameRenderer,
                                 MediaPlayer mp,
                                 SurfaceView sv_reviver_data)
    {
        mGlFrameRenderer = glFrameRenderer;
        mMediaPlayer = mp;
        mSurfaceView = sv_reviver_data;
    }

    /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
    @Override
    protected void onPostExecute(String result) {

    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(Void... params) {

        mServerSocket = null;

        try {
            String name = Thread.currentThread()
                                .getName();

            mServerSocket = new ServerSocket(10086);
            //mClient.setReceiveBufferSize(1024 * 1024 * 10);

            mClient = mServerSocket.accept();
            mClient.setKeepAlive(true);
            // LogUtils.logInfo(TAG, "doInBackground", "name=" + name);

            byte[]      bys            = new byte[1024 * 100];
            ByteBuffer  frameBB        = ByteBuffer.allocate(CameraUtil.CAMERA_PIXELS);
            int         len;
            InputStream inputStream    = mClient.getInputStream();
            int         length         = 0;
            int         needDifference = 0;
            boolean     isOver         = false;
            byte[]      overBytes      = new byte[1024 * 100];

            //create a localsocket


            while ((len = inputStream.read(bys)) != -1) {
                length += len;

                if (length > CameraUtil.CAMERA_PIXELS) {
                    //说明就要截断了
                    int difference = length - CameraUtil.CAMERA_PIXELS;

                    needDifference = difference;

                    //上一个length需要他就成为了一个115200  一帧的数据

                    int beforeNeed = Math.abs(len - difference);

                    byte[] desBytes = new byte[beforeNeed];

                    System.arraycopy(bys, 0, desBytes, 0, beforeNeed);

                    isOver = true;

                    frameBB.put(desBytes);

                    overBytes = new byte[difference];

                    System.arraycopy(bys, beforeNeed, overBytes, 0, difference);

                    //  overFrameBB.clear();

                    // overFrameBB.put(overBytes);

                    length = CameraUtil.CAMERA_PIXELS;

                    //  Log.d(TAG, "这里超出了：");

                } else {
                    byte[] desBytes = new byte[len];

                    System.arraycopy(bys, 0, desBytes, 0, len);

                    frameBB.put(desBytes);

                    // Log.d(TAG, "这里正常添加了：");

                }

                if (length == CameraUtil.CAMERA_PIXELS) {
                    //  Log.d(TAG, "这里进入到了length为一帧的情况：");

                    //使用到的byte[]

                    //重置一下
                    length = 0;


                    splitYUVData(frameBB.array());


                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String name = Thread.currentThread()
                                                .getName();
                            //   LogUtils.logInfo(TAG, "run", "name=" + name);
                            mGlFrameRenderer.update(mYData, mUData, mVData);

                        }
                    });


                    frameBB.clear();

                    if (isOver) {
                        //                        LogUtils.logInfo(TAG,
                        //                                         "超出的时候 两者的position",
                        //                                         "framebb.postion" + frameBB.position());

                        //frameBB = overFrameBB;

                        //overFrameBB.clear();

                        frameBB.put(overBytes);

                        //                        LogUtils.logInfo(TAG,
                        //                                         "超出的时候 两者的position",
                        //                                         "framebb.postiofn复制后的" + frameBB.position() + "length=" + length);
                        length = needDifference;
                        isOver = false;
                    }
                }


                //                length += len;
                //
                //                LogUtils.logInfo(TAG,
                //                                 "doInBackground",
                //                                 "frameBB.length" + frameBB.array().length + "lenght" + length + "ByteBuffer.wrap(bys, 0, len).array().length" + ByteBuffer.wrap(
                //                                         bys,
                //                                         0,
                //                                         len)
                //                                                                                                                                                           .array().length);
                //
                //                int available = inputStream.available();
                //                LogUtils.logInfo(TAG, "doInBackground", "输入流可读的字数：" + available);
                //                //                byte[] desBytes = new byte[len];
                //                //
                //                //                System.arraycopy(bys, 0, desBytes, 0, len);
                //                //                frameBB.put(desBytes);
                //
                //
                //                LogUtils.logInfo(TAG, "doInBackground", "len" + len);
                //                if (length > CameraUtil.CAMERA_PIXELS) {
                //                    //说明就要截断了
                //                    int difference = length - CameraUtil.CAMERA_PIXELS;
                //                    needDifference = difference;
                //                    //上一个length需要他就成为了一个115200  一帧的数据
                //                    int    beforeNeed = Math.abs(len - difference);
                //                    byte[] desBytes   = new byte[beforeNeed];
                //                    System.arraycopy(bys, 0, desBytes, 0, beforeNeed);
                //                    isOver = true;
                //                    // frameBB.put(desBytes);
                //
                //                    LogUtils.logInfo(TAG,
                //                                     "doInBackground",
                //                                     "测试一下看各个差值分别是多少" + difference + "::" + beforeNeed + "::" + length + "::" + len);
                //
                //                    LogUtils.logInfo(TAG, "doInBackground", "超出的范围进行分割" + frameBB.position());
                //
                //                    overBytes = new byte[difference];
                //
                //
                //                    System.arraycopy(bys, beforeNeed, overBytes, 0, difference);
                //                    overFrameBB.clear();
                //                    overFrameBB.put(overBytes);
                //                    length = CameraUtil.CAMERA_PIXELS;
                //                } else {
                //                    byte[] desBytes = new byte[len];
                //
                //                    System.arraycopy(bys, 0, desBytes, 0, len);
                //
                //                    frameBB.put(desBytes);
                //                    LogUtils.logInfo(TAG, "doInBackground", "正常的叠加" + frameBB.position());
                //                }
                //
                //                //                                try {
                //                //
                //                //                                    frameBB.put(desBytes);
                //                //
                //                //                                } catch (Exception e) {
                //                //                                    //说明要丢帧了
                //             q   //                                    LogUtils.logInfo(TAG, "doInBackground", "说明要丢帧了 重试一下");
                //                //
                //                //                                }
                //
                //                if (length == CameraUtil.CAMERA_PIXELS) {
                //                    final byte[] finalFrameString = frameBB.array();
                //                    //重置一下
                //                    length = 0;
                //                    frameBB.clear();
                //
                //                    if (isOver) {
                //                        //如果是超出了
                //                        LogUtils.logInfo(TAG,
                //                                         "isOver",
                //                                         "看缓冲区的位置和difference是不是一样的：" + overFrameBB.position() + "::" + needDifference);
                //                        frameBB = overFrameBB;
                //                        overFrameBB.clear();
                //                        length = needDifference;
                //                        isOver = false;
                //                    }
                //                    mHandler.post(new Runnable() {
                //                        @Override
                //
                //                        public void run() {
                //                            LogUtils.logInfo(TAG, "hyrun", finalFrameString.length + "");
                //
                //                            //                        ByteArrayInputStream bais  = new ByteArrayInputStream(finalFrameString);
                //                            //                        YuvImage             image = new YuvImage(finalFrameString, ImageFormat.NV21,320, 240, null);
                //                            YuvImage image = new YuvImage(finalFrameString,
                //                                                          ImageFormat.NV21,
                //                                                          CameraUtil.CAMERA_WIDTH,
                //                                                          CameraUtil.CAMERA_HEIGHT,
                //                                                          null);
                //
                //                            if (image != null) {
                //                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //                                image.compressToJpeg(new Rect(0,
                //                                                              0,
                //                                                              CameraUtil.CAMERA_WIDTH,
                //                                                              CameraUtil.CAMERA_HEIGHT),
                //                                                     100,
                //                                                     stream);
                //                                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(),
                //                                                                           0,
                //                                                                           stream.size());
                //                                LogUtils.logInfo(TAG, "run()", stream.size() + "::");
                //                                mIv_camera.setImageBitmap(bmp);
                //
                //                            }
                //
                //                        }
                //
                //
                //                    });
                //
                //                }


            }
            //            InputStream           inputStream  = mClient.getInputStream();
            //            OutputStream          outputStream = mClient.getOutputStream();
            //            ByteArrayOutputStream swapStream   = new ByteArrayOutputStream();
            //
            //            byte          buf[]         = new byte[1024 * 10];
            //            String        frameString   = null;
            //            byte[]        frame         = new byte[115200];
            //            int           len;
            //            long          length        = 0;
            //            int           bytesRead     = 0;
            //            int           number        = 0;
            //            int           baseNum       = 115200;
            //            StringBuilder stringBuilder = new StringBuilder();

            //            while ((len = inputStream.read(frame)) != -1) {
            ////                //swapStream.write(buf, 0, len);
            ////                length += len;
            ////                float f = (float) length / (float) 115200;
            ////                int   i = (int) (length / 115200);
            ////                LogUtils.logInfo(TAG,
            ////                                 "doInBackground",
            ////                                 "这里进行了写入" + len + "::" + length + "length/len" + f);
            ////
            ////                boolean anInt = isInt(f, i);
            ////                LogUtils.logInfo(TAG, "doInBackground", "看是不是整数" + anInt + "buf的长度" + buf.toString());
            ////                stringBuilder.append(buf);
            ////                LogUtils.logInfo(TAG, "hyrun", "进入之前的长度" + stringBuilder.length());
            ////                if (anInt) {
            ////                    frameString = stringBuilder.toString();
            ////                    //说明 是一幅画面的倍数了
            ////
            ////
            ////                    //                    byte[] byteArray = new byte[1024 * 1024 * 10];
            ////                    //
            ////                    //
            ////                    //                    if (number == 0) {
            ////                    //                        bytesRead = inputStream.read(byteArray, 0, baseNum);
            ////                    //                        number++;
            ////                    //                    } else {
            ////                    //                        bytesRead = inputStream.read(byteArray, number * baseNum, baseNum);
            ////                    //                        number++;
            ////                    //                    }
            ////                    //                    mMessage = new String(byteArray, Charset.forName("ISO-8859-1"));
            ////                    //                    mFrameString = mMessage.toString();
            ////
            ////                    final String finalFrameString = frameString;
            ////                    mHandler.post(new Runnable() {
            ////                        @Override
            ////                        public void run() {
            ////                            byte[] frame = finalFrameString.getBytes();
            ////                            LogUtils.logInfo(TAG, "hyrun", frame.length + "");
            ////                            ByteArrayInputStream bais = new ByteArrayInputStream(frame);
            ////                            Bitmap               map  = BitmapFactory.decodeStream(bais);
            ////                            mIv_camera.setImageBitmap(map);
            ////                        }
            ////                    });
            ////
            ////                    frameString = "";
            ////                    stringBuilder = new StringBuilder();
            ////                }
            //                final byte[] finalFrameString = frame;
            //                mHandler.post(new Runnable() {
            //                                            @Override
            //                                            public void run() {
            //                                                LogUtils.logInfo(TAG,"run","frame的大小"+finalFrameString.length+"frame的tostring"+finalFrameString.toString());
            //                                                YuvImage              yuvimage =new YuvImage(finalFrameString, ImageFormat.NV21, 400, 400, null);//20、20分别是图的宽度与高度
            //                                                ByteArrayOutputStream baos     = new ByteArrayOutputStream();
            //                                                yuvimage.compressToJpeg(new Rect(0, 0, 400, 400), 100, baos);//80--JPG图片的质量[0-100],100最高
            //                                                byte[] jdata = baos.toByteArray();
            //                                                Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
            //
            //
            //                                                mIv_camera.setImageBitmap(bmp);
            //                                            }
            //                                        });
            //            }

            // byte[] in2b = swapStream.toByteArray();

            //   Log.d(TAG, "doInBackground: in2b" + in2b.length);

            LogUtils.logInfo(TAG, "doInBackground", "看Camera连接上了:" + mClient.isConnected());

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.logInfo(TAG, "doInBackground", "这里接受的时候出错了");
        } finally {
            try {
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
                if (mClient != null) {
                    mClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    private boolean isInt(float f, int i) {
        if (f - (float) i == 0) {
            //说明是整数
            return true;
        }
        return false;
    }
    //获取到宽高

    byte[] mYData = new byte[CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT];
    byte[] mUData = new byte[CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT / 4];
    byte[] mVData = new byte[CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT / 4];

    public void splitYUVData(byte[] data) {
        ByteBuffer mYUVData = ByteBuffer.wrap(data);
        Log.d(TAG, "HY不同的测试：splitYUVData" + data.length);
        mYUVData.get(mYData, 0, CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT);

        mYUVData.position(CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT);
        mYUVData.get(mVData, 0, CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT / 4);
        mYUVData.position(CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT * 5 / 4);
        mYUVData.get(mUData, 0, CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT / 4);


        //     int nFrameSize = CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT;

        //        int k = 0;
        //
        //        for (int i = 0; i < CameraUtil.CAMERA_WIDTH * CameraUtil.CAMERA_HEIGHT / 2; i += 2) {
        //            mVData[k] = data[nFrameSize + i]; //v
        //            mUData[k] = data[nFrameSize + i + 1];//u
        //            k++;
        //        }


    }
}