package com.example.lyc.vrexplayer.activity;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/*
 *  @项目名：  VrexPlayer 
 *  @包名：    com.example.lyc.vrexplayer.activity
 *  @文件名:   MediaFile
 *  @创建者:   LYC2
 *  @创建时间:  2017/3/26 16:13
 *  @描述：    TODO
 */
public class MediaFile {
    private static final String TAG = "MediaFile";
    // comma separated list of all file extensions supported by the media scanner
    public static String sFileExtensions;
    static {
        System.loadLibrary("native-lib");
    }
    // Audio file types
    public static final  int FILE_TYPE_MP3         = 1;
    public static final  int FILE_TYPE_M4A         = 2;
    public static final  int FILE_TYPE_WAV         = 3;
    public static final  int FILE_TYPE_AMR         = 4;
    public static final  int FILE_TYPE_AWB         = 5;
    public static final  int FILE_TYPE_WMA         = 6;
    public static final  int FILE_TYPE_OGG         = 7;
    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_MP3;
    private static final int LAST_AUDIO_FILE_TYPE  = FILE_TYPE_OGG;

    // MIDI file types
    public static final  int FILE_TYPE_MID        = 11;
    public static final  int FILE_TYPE_SMF        = 12;
    public static final  int FILE_TYPE_IMY        = 13;
    private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
    private static final int LAST_MIDI_FILE_TYPE  = FILE_TYPE_IMY;

    // Video file types
    public static final  int FILE_TYPE_MP4         = 21;
    public static final  int FILE_TYPE_M4V         = 22;
    public static final  int FILE_TYPE_3GPP        = 23;
    public static final  int FILE_TYPE_3GPP2       = 24;
    public static final  int FILE_TYPE_WMV         = 25;
    private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_MP4;
    private static final int LAST_VIDEO_FILE_TYPE  = FILE_TYPE_WMV;

    // Image file types
    public static final  int FILE_TYPE_JPEG        = 31;
    public static final  int FILE_TYPE_GIF         = 32;
    public static final  int FILE_TYPE_PNG         = 33;
    public static final  int FILE_TYPE_BMP         = 34;
    public static final  int FILE_TYPE_WBMP        = 35;
    private static final int FIRST_IMAGE_FILE_TYPE = FILE_TYPE_JPEG;
    private static final int LAST_IMAGE_FILE_TYPE  = FILE_TYPE_WBMP;

    // Playlist file types
    public static final  int FILE_TYPE_M3U            = 41;
    public static final  int FILE_TYPE_PLS            = 42;
    public static final  int FILE_TYPE_WPL            = 43;
    private static final int FIRST_PLAYLIST_FILE_TYPE = FILE_TYPE_M3U;
    private static final int LAST_PLAYLIST_FILE_TYPE  = FILE_TYPE_WPL;

    //静态内部类
    static class MediaFileType {

        int    fileType;
        String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    private static HashMap<String, MediaFileType> sFileTypeMap = new HashMap<String, MediaFileType>();
    private static HashMap<String, Integer>       sMimeTypeMap = new HashMap<String, Integer>();

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, new Integer(fileType));
    }

    static {
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg");
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4");
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav");
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr");
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb");
        addFileType("WMA", FILE_TYPE_WMA, "audio/x-ms-wma");
        addFileType("OGG", FILE_TYPE_OGG, "application/ogg");

        addFileType("MID", FILE_TYPE_MID, "audio/midi");
        addFileType("XMF", FILE_TYPE_MID, "audio/midi");
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi");
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi");
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody");

        addFileType("MP4", FILE_TYPE_MP4, "video/mp4");
        addFileType("M4V", FILE_TYPE_M4V, "video/mp4");
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2");
        addFileType("3GPP2", FILE_TYPE_3GPP2, "video/3gpp2");
        addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv");

        addFileType("JPG", FILE_TYPE_JPEG, "image/jpeg");
        addFileType("JPEG", FILE_TYPE_JPEG, "image/jpeg");
        addFileType("GIF", FILE_TYPE_GIF, "image/gif");
        addFileType("PNG", FILE_TYPE_PNG, "image/png");
        addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp");
        addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp");

        addFileType("M3U", FILE_TYPE_M3U, "audio/x-mpegurl");
        addFileType("PLS", FILE_TYPE_PLS, "audio/x-scpls");
        addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl");

        // compute file extensions list for native Media Scanner
        StringBuilder    builder  = new StringBuilder();
        Iterator<String> iterator = sFileTypeMap.keySet()
                                                .iterator();

        while (iterator.hasNext()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(iterator.next());
        }
        sFileExtensions = builder.toString();
    }

    public static final String UNKNOWN_STRING = "<unknown>";

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE && fileType <= LAST_AUDIO_FILE_TYPE) || (fileType >= FIRST_MIDI_FILE_TYPE && fileType <= LAST_MIDI_FILE_TYPE));
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE && fileType <= LAST_VIDEO_FILE_TYPE);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE && fileType <= LAST_IMAGE_FILE_TYPE);
    }

    public static boolean isPlayListFileType(int fileType) {
        return (fileType >= FIRST_PLAYLIST_FILE_TYPE && fileType <= LAST_PLAYLIST_FILE_TYPE);
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0) { return null; }
        return sFileTypeMap.get(path.substring(lastDot + 1)
                                    .toUpperCase());
    }

    //根据视频文件路径判断文件类型
    public static boolean isVideoFileType(String path) {  //自己增加
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isVideoFileType(type.fileType);
        }
        return false;
    }

    //根据图片文件路径判断文件类型
    public static boolean isPicFileType(String path) {  //自己增加
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isImageFileType(type.fileType);
        }
        return false;
    }

    //根据音频文件路径判断文件类型
    public static boolean isAudioFileType(String path) {  //自己增加
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isAudioFileType(type.fileType);
        }
        return false;
    }

    //根据mime类型查看文件类型
    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = sMimeTypeMap.get(mimeType);
        return (value == null
                ? 0
                : value.intValue());
    }
    //看该目录下有没有视频 或者 照片文件

    public static final String hasVideo = "hasVideo";
    public static final String hasPic   = "hasPic";

    public static boolean hasVideoOrPicFile(String str, String filePath) {
        switch (str) {
            case hasVideo:
                File file = new File(filePath);
                File[] files = file.listFiles();
                for (File f : files) {
                    if (isVideoFileType(f.getAbsolutePath())) {
                        return true;
                    }
                }
                //循环完了 都没有
                return false;

            case hasPic:
                File file1 = new File(filePath);
                File[] files1 = file1.listFiles();
                for (File f : files1) {
                    if (isPicFileType(f.getAbsolutePath())) {
                        return true;
                    }
                }
                //循环完了 都没有
                return false;

        }
        return false;
    }
    //获取文件夹中 最晚创建的那个文件的时间

    public static long getLastFileTime(String s) {
        File file = new File(s);
        File[] files = file.listFiles();
        boolean isFirst = true;
        long lastTime = 0;
        long temp = 0 ;
        for (File f : files) {
            if(isFirst){
            lastTime = f.lastModified();
                isFirst = false;
            continue;
            }
            if(f.lastModified() > lastTime){
                lastTime = f.lastModified();
                Log.d(TAG, "getLastFileTime: "+f.getAbsolutePath());
            }
        }
        return lastTime;
    }

public static native long getCreateTile(String path);
    public   static String getCurrentTime(long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        String           str    = format.format(new Date(date));
        return str;
    }
}
