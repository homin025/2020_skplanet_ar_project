package com.example.armeeting;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.Image.Plane;
import android.view.Surface;

import org.opencv.core.CvType;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import java.nio.ByteBuffer;

public class OpenCvUtils {
    private static final String TAG = "OpenCvUtils";

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public static Mat imageToMat(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * 3 / 2];
        byte[] rowData = new byte[planes[0].getRowStride()];

        for(int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();

            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for(int row = 0; row < h; row++) {
                int bytesPerPixel =  ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;

                if(pixelStride == bytesPerPixel) {
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);

                    if(h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {
                  if(h - row == 1) {
                      buffer.get(rowData, 0, width - pixelStride + 1);
                  } else {
                      buffer.get(rowData, 0, rowStride);
                  }

                  for(int col = 0; col < w; col++) {
                      data[offset++] = rowData[col * pixelStride];
                  }
                }
            }
        }

        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mat.put(0, 0, data);

        return mat;
    };

    public static int detectFinger(Image src, Surface dst) {
//        if(src.getFormat() != ImageFormat.YUV_420_888) {
//            throw new IllegalArgumentException();
//        }
//
        Plane[] planes = src.getPlanes();
//        if(planes[1].getPixelStride() != 1 && planes[1].getPixelStride() != 1) {
//            throw new IllegalArgumentException();
//        }

        return detectFinger(src.getWidth(), src.getHeight(), planes[0].getBuffer(), dst);
    }

    private static native int detectFinger(int srcWidth, int srcHeight, ByteBuffer srcBuf, Surface dst);
}
