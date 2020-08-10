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
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class OpenCvUtils {
    private static final String TAG = "OpenCvUtils";

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public static Mat IMG2MAT(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
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
                }
                else {
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

        Mat yuvMat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        yuvMat.put(0, 0, data);

        Mat bgrMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
        Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);

        return bgrMat;
    };

    public static Mat NV212RGB(byte[] data, int width, int height) {
        Mat mYUV = new Mat(width + height / 2, width, CV_8UC1);
        mYUV.put(0, 0, data);

        Mat mRGB = new Mat();
        cvtColor(mYUV, mRGB, Imgproc.COLOR_YUV2RGB_NV21);

        return mRGB;
    }

    public static Mat YUV2MAT(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;
        if(rowStride == width) {
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else {
            int yBufferPos = yBuffer.position();
            for(; pos < ySize; pos += width) {
                yBuffer.position(yBufferPos);
                yBuffer.get(nv21, pos, width);
                yBufferPos += rowStride;
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());

        if(pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            byte savePixel = vBuffer.get(1);

            try {
                vBuffer.put(1, (byte)~savePixel);

                if(uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.get(nv21, ySize, uvSize);

                    return NV212RGB(nv21, width, height);
                }
            } catch (ReadOnlyBufferException e) {

            }
        }

        for(int row = 0; row < height / 2; row++) {
            for(int col = 0; col < width / 2; col++) {
                int vuBufferPos = col * pixelStride + row * rowStride;
                nv21[pos++] = vBuffer.get(vuBufferPos);
                nv21[pos++] = uBuffer.get(vuBufferPos);
            }
        }

        return NV212RGB(nv21, width, height);
    };

    public static int detectFinger(Image src, Surface dst) {
        //return detectFinger(src.getWidth(), src.getHeight(), IMG2MAT(src), dst);
        return detectFinger(src.getWidth(), src.getHeight(), YUV2MAT(src).getNativeObjAddr(), dst);
    }

    private static native int detectFinger(int srcWidth, int srcHeight, long srcMatAddr, Surface dst);
}
