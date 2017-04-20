#include <sys/stat.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/core/matx.hpp>
#include <opencv2/core/mat.hpp>


extern "C" {
#define  LOG_TAG    "videoplay"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
jstring
Java_com_example_lyc_vrexplayer_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jlong JNICALL
Java_com_example_lyc_vrexplayer_activity_MediaFile_getCreateTile(JNIEnv *env, jclass type,
                                                                 jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    struct stat buf;
    int result;

    //获得文件状态信息

    result = stat(path, &buf);

    //显示文件状态信息

    if (result != 0)
        LOGD("显示文件状态信息出错");//

    else {

        LOGD("文件大小: %d", buf.st_size);
        LOGD("文件创建时间: %ld", buf.st_ctime);
        //       printf("文件创建时间: %s", ctime(&buf.st_ctime));
//        printf("访问日期: %s", ctime(&buf.st_atime));
//        printf("最后修改日期: %s", ctime(&buf.st_mtime));

    }
    env->ReleaseStringUTFChars(path_, path);
    return buf.st_mtime;

}

JNIEXPORT void JNICALL
Java_com_example_lyc_vrexplayer_activity_DetailPicAcitvity_setPathAndSurfaceView(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jstring path_,
                                                                                 jobject surface,
                                                                                 jint width,
                                                                                 jint height,
                                                                                 jint type) {
    const char *path = env->GetStringUTFChars(path_, 0);
    jbyteArray arr = env->NewByteArray(width * height * 4);
    jbyte *rgba = env->GetByteArrayElements(arr, NULL);
    LOGD("jinru C++");

    cv::Mat src = cv::imread(path);
    cv::Mat dst;
    LOGD("jinru C++1");

    switch (type) {
        case cv::INTER_NEAREST :
            cv::resize(src, dst, cv::Size(width, height), 0, 0, cv::INTER_NEAREST);
            break;
        case cv::INTER_LINEAR :
            cv::resize(src, dst, cv::Size(width, height), 0, 0, cv::INTER_LINEAR);
            break;
        case cv::INTER_CUBIC :
            cv::resize(src, dst, cv::Size(width, height), 0, 0, cv::INTER_CUBIC);
            break;
        case cv::INTER_AREA :
            cv::resize(src, dst, cv::Size(width, height), 0, 0, cv::INTER_AREA);
            break;
        case cv::INTER_LANCZOS4 :
            cv::resize(src, dst, cv::Size(width, height), 0, 0, cv::INTER_LANCZOS4);
            break;
    }
    LOGD("jinru C++2");
    long index = 0;
    for (int i = 0; i < height; i++) {
        //获取第 i 行首像素指针
        cv::Vec3b *p = dst.ptr<cv::Vec3b>(i);
        //因为mat中只有3个通道的，  要是 Vec4b  就错了
        for (int j = 0; j < width; j++) {
            // 这里 为什么赋值是210   因为opencv中的顺序是BGR
            //                         而我们需要的顺序是RGBA，所以自己最后还加了个Alpha
            //  index = i *width *4 + j * 4;
//            rgba[index + 0 ]  =    p[j][2] ;
//            rgba[index + 1 ]  =    p[j][1] ;
//            rgba[index + 2 ]  =    p[j][0] ;
//            rgba[index + 3 ] = 255;
            rgba[index] = p[j][2];
            index++;
            rgba[index] = p[j][1];
            index++;
            rgba[index] = p[j][0];
            index++;
            rgba[index] = 255;
            index++;
        }
    }
    src.release();
    dst.release();
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_release(nativeWindow);
    ANativeWindow_setBuffersGeometry(nativeWindow, width, height,
                                     WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;
    ANativeWindow_lock(nativeWindow, &windowBuffer, 0);
    uint8_t *dst_data = (uint8_t *) windowBuffer.bits;
    int dstStride = windowBuffer.stride * 4;
    uint8_t *src_data = (uint8_t *) rgba;
    int srcStride = width * 4;
    int h;
    for (h = 0; h < height; h++) {

        memcpy(dst_data + h * dstStride, src_data + h * srcStride, srcStride);
    }
    ANativeWindow_unlockAndPost(nativeWindow);
    env->ReleaseByteArrayElements(arr, rgba, 0);
    env->ReleaseStringUTFChars(path_, path);
}

JNIEXPORT void JNICALL
Java_com_example_lyc_vrexplayer_activity_DetailPicAcitvity_setSurfaceView(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject surface,
                                                                          jbyteArray rgba_,
                                                                          jint width, jint height) {
    jbyte *rgba = env->GetByteArrayElements(rgba_, NULL);
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_release(nativeWindow);
    ANativeWindow_setBuffersGeometry(nativeWindow, 3840, 1700,
                                     WINDOW_FORMAT_RGBA_8888);


    ANativeWindow_Buffer windowBuffer;
    ANativeWindow_lock(nativeWindow, &windowBuffer, 0);
    uint8_t *dst = (uint8_t *) windowBuffer.bits;
    int dstStride = windowBuffer.stride * 4;
    uint8_t *src = (uint8_t *) rgba;
    int srcStride = width * 4;
    LOGD("dstStride = %d ----srcStride = %d ", dstStride, srcStride);

//    // 由于window的stride和帧的stride不同,因此需要逐行复制
    int h;
    for (h = 0; h < height; h++) {
        //  LOGD("进行了 == %d" , h);
        memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
    }
    LOGD("h=%d", h);
//
    ANativeWindow_unlockAndPost(nativeWindow);
    env->ReleaseByteArrayElements(rgba_, rgba, 0);
}
JNIEXPORT jbyteArray JNICALL
Java_com_example_lyc_vrexplayer_activity_DetailPicAcitvity_getNewPixels(JNIEnv *env,
                                                                        jobject instance,
                                                                        jintArray pixels_,
                                                                        jint width, jint height) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    int pixel_length = width * height;
    int lenght = width * height * 4;
    jbyteArray arr = env->NewByteArray(lenght);
    jbyte *rgba = env->GetByteArrayElements(arr, NULL);
    int i = 0;

    for (i = 0; i < pixel_length; i++) {
        rgba[i * 4] = (pixels[i] >> 16) & 0xFF;
        rgba[i * 4 + 1] = (pixels[i] >> 8) & 0xFF;
        rgba[i * 4 + 2] = pixels[i] & 0xFF;
        rgba[i * 4 + 3] = (pixels[i] >> 24) & 0xFF;
    }
    jbyteArray it = (*env).NewByteArray(lenght);
    (*env).SetByteArrayRegion(it, 0, lenght, rgba);
    env->ReleaseByteArrayElements(arr, rgba, 0);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return  it;
}
}
