#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include "com_example_armeeting_JNIUtils.h"

#include <algorithm>
#include <cstring>
#include <cstdlib>
#include <vector>

#include <opencv2/dnn.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>

using namespace std;
using namespace cv;
using namespace dnn;

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

float confThreashold = 0.5;
float nmsThreashold = 0.4;
int inpWidth = 416;
int inpHeigth = 416;

String classes[] = { "rock", "scissors", "paper" };
int classesSize = 3;
String modelConfiguration = "C:/Users/homin/Documents/Code/AndroidStudio/2020_skplanet_ar_project/app/src/main/assets/yolov4-skplanet.cfg";
String modelWeights = "C:/Users/homin/Documents/Code/AndroidStudio/2020_skplanet_ar_project/app/src/main/assets/yolov4-skplanet.weights";

vector<String> getOutLayerNames(const Net& net) {
    static vector<String> names;

    if(names.empty()) {
        vector<int> outLayers = net.getUnconnectedOutLayers();
        vector<String> layersName = net.getLayerNames();

        names.resize(outLayers.size());
        for(size_t i = 0; i < outLayers.size(); ++i)
            names[i] = layersName[outLayers[i] - 1];
    }

    return names;
}

int cutLowConfidence(Mat& frame, const vector<Mat>& outs) {
    vector<int> classID;
    vector<float> classConf;
    vector<Rect> classBox;

    for(size_t i = 0; i < outs.size(); ++i) {
        float* data = (float*) outs[i].data;

        for(int j = 0; j < outs[i].rows; ++j, data += outs[i].cols) {
            Mat score = outs[i].row(j).colRange(5, outs[i].cols);
            Point point;
            double conf;

            minMaxLoc(score, 0, &conf, 0, &point);
            if(conf > confThreashold) {
//                int centerX = (int)(data[0] * frame.cols);
//                int centerY = (int)(data[1] * frame.rows);
//                int width = (int)(data[2] * frame.cols);
//                int height = (int)(data[3] * frame.rows);
//                int left = centerX - width / 2;
//                int top = centerY - height / 2;

                classID.push_back(point.x);
                classConf.push_back((float) conf);
//                classBox.push_back(Rect(left, top, width, height));
            }
        }
    }

    vector<int> indices;
    NMSBoxes(classBox, classConf, confThreashold, nmsThreashold, indices);
//    for(size_t i = 0; i < indices.size(); ++i) {
//        int idx = indices[i];
//
//        Rect box = classBox[idx];
//        int left = box.x;
//        int top = box.y;
//        int right = box.x + box.width;
//        int bottom = box.y + box.height;
//
//        rectangle(frame, Point(left, top), Point(right, bottom), Scalar(0, 0, 255));
//
//        String label = format("%.2f", classConf[idx]);
//        CV_Assert(classID[idx] < classesSize);
//        label = classes[classID[idx]] + ":" + label;
//
//        int baseLine;
//        Size labelSize = getTextSize(label, FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);
//        top = max(top, labelSize.height);
//        putText(frame, label, Point(left, top), FONT_HERSHEY_SIMPLEX, 0.5, Scalar(255, 255, 255));
//    }

    int answer = distance(classConf.begin(), max_element(classConf.begin(), classConf.end()));

    return classID[answer];
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_armeeting_util_JNIUtils_detectFinger(JNIEnv *env, jclass clazz, jint src_width,
                                                      jint src_height, jlong src_mat_addr,
                                                      jobject dst) {
    Net net = readNetFromDarknet(modelConfiguration, modelWeights);
    LOGD("NUMBER 2");
    net.setPreferableBackend(DNN_BACKEND_OPENCV);
    LOGD("NUMBER 3");
    net.setPreferableTarget(DNN_TARGET_CPU);
    LOGD("NUMBER 4");

    Mat &frame = *(Mat *) src_mat_addr;
    Mat blob;
    vector<Mat> outputs;

    blobFromImage(frame, blob, 1 / 255.0, cvSize(inpWidth, inpHeigth), Scalar(0, 0, 0), true,
                  false);

    net.setInput(blob);
    net.forward(outputs, getOutLayerNames(net));
    int answer = cutLowConfidence(frame, outputs);

    return answer;
}
