package com.example.gammeeting;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gammeeting.util.BorderedText;
import com.example.gammeeting.util.ImageUtils;
import com.example.gammeeting.util.Logger;
import com.example.gammeeting.view.OverlayView;
import com.example.gammeeting.tensorflow.Classifier;
import com.example.gammeeting.tensorflow.MultiBoxTracker;
import com.example.gammeeting.tensorflow.YoloV4Classifier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DetectFragment extends CameraFragment implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    private static final int TF_OD_API_INPUT_SIZE = 416;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "yolov4-tiny-416.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/skplanet.txt";

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private static final boolean MAINTAIN_ASPECT = false;
    private Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;
    private String currDetectResult;
    private String prevDetectResult;
    private int detectCount = 0;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    private Context mContext;
    private AppCompatActivity mActivity;

    DetectEventListener listener;

    public static DetectFragment newInstance() {
        return new DetectFragment();
    }

    public interface DetectEventListener {
        // 가위바위보 인식결과 코드
        int ROCK = 101, SCISSORS = 102, PAPER = 103;

        void onHandDetected(int result);
        void onHandDisappeared();
    }

    public void setDetectionEventListener(DetectEventListener listener) {
        this.listener = listener;
    }

    public void stopDetection() {
        isHandDetected = true;
    }

    public void resumeDetection() {
        isHandDetected = false;
    }

    @Override
    public void onAttach(Context context) {
        LOGGER.d("onAttach " + this);
        super.onAttach(context);

        mContext = context;
        if(context instanceof Activity) {
            mActivity = (AppCompatActivity) context;
        }
    }
    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, mActivity.getApplicationContext().getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(mContext);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    YoloV4Classifier.create(
                            mContext.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);
//            detector = TFLiteObjectDetectionAPIModel.create(
//                    getAssets(),
//                    TF_OD_API_MODEL_FILE,
//                    TF_OD_API_LABELS_FILE,
//                    TF_OD_API_INPUT_SIZE,
//                    TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            mContext, "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            mActivity.finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) mActivity.findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        Runnable run = new Runnable() {
            @Override
            public void run() {
                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                Log.e("CHECK", "run: " + results.size());

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);

                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                switch (MODE) {
                    case TF_OD_API:
                        minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        break;
                }

                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();

                for (final Classifier.Recognition result : results) {
                    final RectF location = result.getLocation();
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                        canvas.drawRect(location, paint);
                        LOGGER.i("Detection result " + result.getTitle());

                        currDetectResult = result.getTitle();

                        // 인식 결과가 2번 연속으로 동일하게 나오면 인식 결과 확정
                        // 인식 결과 확정되면 인식 중단 (카운트에 맞춰서 다시 인식 시작)
                        if (detectCount == 2) {
                            int handType;
                            switch (currDetectResult) {
                                case "rock":
                                    handType = listener.ROCK;
                                    break;
                                case "scissors":
                                    handType = listener.SCISSORS;
                                    break;
                                case "paper":
                                    handType = listener.PAPER;
                                    break;
                                default:
                                    handType = -1;
                            }

                            getActivity().runOnUiThread(()->listener.onHandDetected(handType));

                            detectCount = 0;
                        }
                        else if (detectCount == 0) {
                            detectCount += 1;
                            prevDetectResult = currDetectResult;
                        }
                        else {
                            if (prevDetectResult.equals(currDetectResult)) {
                                detectCount += 1;
                            }
                            else {
                                detectCount = 0;
                                prevDetectResult = currDetectResult;
                            }
                        }

                        cropToFrameTransform.mapRect(location);

                        result.setLocation(location);
                        mappedRecognitions.add(result);
                    }
                }

                tracker.trackResults(mappedRecognitions, currTimestamp);
                trackingOverlay.postInvalidate();

                computingDetection = false;
            }
        };

        runInBackground(run);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cameraconnection;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected void setDesiredPreviewFrameSize(Size size) {
        DESIRED_PREVIEW_SIZE = size;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
}