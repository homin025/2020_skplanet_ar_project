#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <android/native_window_jni.h>
#include "com_example_armeeting_OpenCvUtils.h"

#include <cstring>
#include <cstdlib>

#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>

using namespace std;
using namespace cv;

#ifndef int64_t
#define int64_t long long
#endif

#ifndef int32_t
#define int32_t int
#endif

#ifndef uint32_t
#define uint32_t unsigned int
#endif

#ifndef uint8_t
#define uint8_t unsigned char
#endif

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "Armeeting", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "Armeeting", __VA_ARGS__)

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_armeeting_OpenCvUtils_detectFinger(JNIEnv *env, jclass clazz, jint src_width,
                                                    jint src_height, jobject src_buf, jobject dst_surface) {

    uint8_t *srcPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(src_buf));
    if(nullptr == srcPtr) {
        LOGE("Error: srcPtr is nullptr");
        return NULL;
    }

    int count = 0;

    ANativeWindow *win = ANativeWindow_fromSurface(env, dst_surface);
    ANativeWindow_acquire(win);

    ANativeWindow_Buffer buf;
    ANativeWindow_setBuffersGeometry(win, src_height, src_width, 0);

    if(int32_t err = ANativeWindow_lock(win, &buf, NULL)) {
        LOGE("Error: win can't acquire lock");
        return NULL;
    }

    uint8_t *dstPtr = reinterpret_cast<uint8_t *>(buf.bits);

    Mat matSrcYuv(src_height + src_height / 2, src_width, CV_8UC1, srcPtr);
    Mat matDstRgba(src_width, buf.stride, CV_8UC1, dstPtr);
    Mat matSrcRgba(src_height, src_width, CV_8UC1);
    Mat matFlipRgba(src_width, src_height, CV_8UC1);
    Mat matRoi;
    Mat matThreshold;

    cvtColor(matSrcYuv, matSrcRgba, CV_YUV2RGBA_NV21);
    transpose(matSrcRgba, matFlipRgba);
    flip(matFlipRgba, matFlipRgba, 1);

    Rect roi(src_width / 2, src_height / 2, src_width / 4, src_height / 4);
    matRoi = matFlipRgba(roi);
    cvtColor(matRoi, matRoi, CV_RGBA2GRAY);

    GaussianBlur(matRoi, matRoi, Size(19, 19), 0.0, 0.0);
    threshold(matRoi, matThreshold, 0, 255, THRESH_BINARY_INV + THRESH_OTSU);

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(matThreshold, contours, hierarchy, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE, Point());

    if(contours.size() > 0) {
        size_t indexOfBiggestContour = -1;
        size_t sizeOfBiggestContour = 0;

        for(size_t i = 0; i < contours.size(); i++) {
            if(contours[i].size() > sizeOfBiggestContour) {
                indexOfBiggestContour = i;
                sizeOfBiggestContour = contours[i].size();
            }
        }

        vector<vector<int>> hull(contours.size());
        vector<vector<Point>> hullPoint(contours.size());
        vector<vector<Vec4i>> defects(contours.size());
        vector<vector<Point>> defectPoint(contours.size());
        vector<vector<Point>> contours_poly(contours.size());

        Point2f rectPoint[4];
        vector<RotatedRect>minRect(contours.size());
        vector<Rect> boundRect(contours.size());

        for(size_t i = 0; i < contours.size(); i++) {
            if(contourArea(contours[i]) > 5000) {
                convexHull(contours[i], hull[i], true);
                convexityDefects(contours[i], hull[i], defects[i]);

                if(indexOfBiggestContour == i) {
                    minRect[i] = minAreaRect(contours[i]);

                    for(size_t j = 0; j < hull[i].size(); j++) {
                        int indice = hull[i][j];
                        hullPoint[i].push_back(contours[i][indice]);
                    }

                    count = 0;

                    for(size_t j = 0; j < defects[i].size(); j++) {
                        if(defects[i][j][3] > 13 * 256) {
                            int pointStart = defects[i][j][0];
                            int pointEnd = defects[i][j][1];
                            int pointFar = defects[i][j][2];

                            defectPoint[i].push_back(contours[i][pointFar]);
                            circle(matRoi, contours[i][pointEnd], 3, Scalar(0, 255, 0), 2);

                            count++;
                        }
                    }

                    drawContours(matThreshold, contours, i, Scalar(255,255,0), 2, 8, vector<Vec4i>(), 0, Point() );
                    drawContours(matThreshold, hullPoint, i, Scalar(255,255,0), 1, 8, vector<Vec4i>(), 0, Point());
                    drawContours(matRoi, hullPoint, i, Scalar(0,0,255), 2, 8, vector<Vec4i>(), 0, Point() );
                    approxPolyDP(contours[i], contours_poly[i], 3, false);
                    boundRect[i] = boundingRect(contours_poly[i]);
                    rectangle(matRoi, boundRect[i].tl(), boundRect[i].br(), Scalar(255,0,0), 2, 8, 0);
                    minRect[i].points(rectPoint);
                    for(size_t j = 0; j < 4; j++){
                        line(matRoi, rectPoint[j], rectPoint[(j + 1) % 4], Scalar(0,255,0), 2, 8);
                    }
                }
            }
        }
    }

    ANativeWindow_unlockAndPost(win);
    ANativeWindow_release(win);

    return count;
}