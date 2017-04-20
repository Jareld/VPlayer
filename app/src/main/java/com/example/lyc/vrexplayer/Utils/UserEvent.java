package com.example.lyc.vrexplayer.Utils;

/*
 *  @项目名：  TestWifiDerect 
 *  @包名：    com.example.lyc2.testwifiderect.utils
 *  @文件名:   UserEvent
 *  @创建者:   LYC2
 *  @创建时间:  2016/12/5 10:48
 *  @描述：    TODO
 */
public class UserEvent {
    private static final String TAG = "UserEvent";

    private String name;
    private float   progress;
    private String fileName;

    public int getFileLengthMB() {
        return fileLengthMB;
    }

    public void setFileLengthMB(int fileLengthMB) {
        this.fileLengthMB = fileLengthMB;
    }

    private int fileLengthMB;
    public UserEvent (String sName ){
        name = sName;
    }
    public UserEvent(long l, String n) {
        name = n;
        progress = l;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float l) {
        this.progress = l;
    }

    public void setFileName(String fn) {
        this.fileName = fn;
    }
    public String getFileName(){
        return fileName;
    }
}
