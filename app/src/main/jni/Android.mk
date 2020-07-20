LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_ROOT := C:\Users\skm\AndroidStudioProjects\ARMeeting\sdk
OPENCV_CAMERA_MODULE := on
OPENCV_INSTALL_MODULE := on
OPENCV_LIB_TYPE := SHARED
include ${OPENCV_ROOT}\native\jni\OpenCV.mk

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := main.cpp
LOCAL_CFALGS = -DSTDC_HEADERS

include $(BUILD_SHARED_LIBRARY)