package com.example.lyc.vrexplayer.Utils;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.Utils
 *  @文件名:   SelectedPosition
 *  @创建者:   LYC2
 *  @创建时间:  2017/2/14 13:46
 *  @描述：    TODO
 */
public class SelectedPosition {
    private static final String TAG = "SelectedPosition";
    private  int position_i;
    private  int position_j;

    public boolean isSeverReceving() {
        return isSeverReceving;
    }

    public void setSeverReceving(boolean severReceving) {
        isSeverReceving = severReceving;
    }

    private boolean isSeverReceving =false;
    public int getBefore_position_i() {
        return before_position_i;
    }

    public void setBefore_position_i(int before_position_i) {
        this.before_position_i = before_position_i;
    }

    public int getBefore_position_j() {
        return before_position_j;
    }

    public void setBefore_position_j(int before_position_j) {
        this.before_position_j = before_position_j;
    }

    private int before_position_i;
    private int before_position_j;
    private boolean isFirst = true;
    private boolean isLongClick = false;

    public boolean isDoing() {
        return isDoing;
    }

    public void setDoing(boolean doing) {
        isDoing = doing;
    }

    private boolean isDoing = false;
    public void setPosition_i(int i ){
        position_i = i;
    }
    public void setPosition_j(int j ){
        position_j = j;
    }

    public int getPosition_i() {
        return position_i;
    }

    public int getPosition_j() {
        return position_j;
    }
    public  void setIsFirst(boolean first){
        isFirst = first;
    }
    public boolean getIsFirst(){
        return  isFirst;
    }

    public boolean getIsLongClick() {
        return isLongClick;
    }

    public void setLongClick(boolean longClick) {
        isLongClick = longClick;
    }
}
