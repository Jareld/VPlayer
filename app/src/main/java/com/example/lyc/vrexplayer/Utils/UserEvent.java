package com.example.lyc.vrexplayer.Utils;

import java.util.ArrayList;

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
    private float  progress;
    private String fileName;
    private int    files_size;

    public void setFilesSize(int size) {
        this.files_size = size;
    }

    public int getFilesSize() {
        return files_size;

    }

    public long getFileLengthMB() {
        return fileLengthMB;
    }

    public void setFileLengthMB(long fileLengthMB) {
        this.fileLengthMB = fileLengthMB;
    }

    private long fileLengthMB;

    public UserEvent(String sName) {
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

    public String getFileName() {
        return fileName;
    }

    public int getFiles_size() {
        return files_size;
    }


    public ArrayList<String> pathsArr;
    public void setPathsArr(ArrayList<String> arrayList){
        pathsArr = arrayList;
    }
    public ArrayList<String> getPathsArr(){
        return pathsArr;
    }
}
