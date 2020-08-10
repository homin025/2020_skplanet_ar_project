package com.example.armeeting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.SensorManager.AXIS_X;
import static android.hardware.SensorManager.AXIS_Z;

public class TreasureFindActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, SensorEventListener {

    private static final String TAG = "TreasureFindActivity";

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
    };

    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;

    public Location mCurrentLocation;

    private Location[] markers = new Location[3];

    private View mLayout;

    private ArFragment arFragment;
    private static ArSceneView arSceneView;
    private AnchorNode[] mAnchorNode = new AnchorNode[3];
    private ModelRenderable[] modelRenderable = new ModelRenderable[3];

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    public static float mCurrentAzim = 0f; // 방위각
    public static float mCurrentPitch = 0f; // 피치
    public static float mCurrentRoll = 0f; // 롤

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasurefind);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mLayout = findViewById(R.id.layout_treasure_find_activity);

        markers[0] = new Location("point 0");
        markers[0].setLatitude(0);
        markers[0].setLongitude(0);

        markers[1] = new Location("point 1");
        markers[1].setLatitude(0);
        markers[1].setLongitude(0);

        markers[2] = new Location("point 2");
        markers[2].setLatitude(0);
        markers[2].setLongitude(0);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {}
        );

        FragmentManager fm =getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if(null != mapFragment) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        arFragment = (ArFragment) fm.findFragmentById(R.id.arCamera);
        arSceneView = arFragment.getArSceneView();
        setUpModel();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateScene);
    }

    @Override
    protected void onResume() {
        super.onResume();
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);

        arFragment.onResume();

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        arFragment.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    private void setUpModel() {
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> modelRenderable[0] = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);

        Marker marker1 = new Marker();
        marker1.setPosition(new LatLng(markers[0].getLatitude(), markers[0].getLongitude()));
        marker1.setHeight(70);
        marker1.setWidth(60);
        marker1.setIcon(OverlayImage.fromResource(R.drawable.roundedbutton));
        marker1.setAnchor(new PointF(0.5f, 1));
        marker1.setMap(naverMap);

        Marker marker2 = new Marker();
        marker2.setPosition(new LatLng(markers[1].getLatitude(), markers[1].getLongitude()));
        marker2.setHeight(70);
        marker2.setWidth(60);
        marker2.setIcon(OverlayImage.fromResource(R.drawable.roundedbutton));
        marker2.setAnchor(new PointF(0.5f, 1));
        marker2.setMap(naverMap);

        Marker marker3 = new Marker();
        marker3.setPosition(new LatLng(markers[2].getLatitude(), markers[2].getLongitude()));
        marker3.setHeight(70);
        marker3.setWidth(60);
        marker3.setIcon(OverlayImage.fromResource(R.drawable.roundedbutton));
        marker3.setAnchor(new PointF(0.5f, 1));
        marker3.setMap(naverMap);

        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setScaleBarEnabled(false);
        uiSettings.setZoomControlEnabled(false);
        uiSettings.setLocationButtonEnabled(false);
        uiSettings.setLogoGravity(Gravity.LEFT | Gravity.BOTTOM);
        uiSettings.setLogoMargin(0, 0, 0, -5);

        CameraUpdate cameraUpdate = CameraUpdate.zoomTo(15);
        mNaverMap.moveCamera(cameraUpdate);
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        mNaverMap.setLiteModeEnabled(true);

        LocationOverlay locationOverlay = mNaverMap.getLocationOverlay();

        locationOverlay.setIconWidth(40);
        locationOverlay.setIconHeight(40);

        locationOverlay.setSubIconWidth(40);
        locationOverlay.setSubIconHeight(40);
        locationOverlay.setSubAnchor(new PointF(0.5f, 0.9f));

        mNaverMap.addOnLocationChangeListener(location ->{
            mCurrentLocation = location;
        });
        mNaverMap.addOnCameraIdleListener(()->{
            if(mNaverMap.getLocationTrackingMode() == LocationTrackingMode.NoFollow ||
                    mNaverMap.getLocationTrackingMode() == LocationTrackingMode.None){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mLocationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!mLocationSource.isActivated()) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrix(rotationMatrix, null, mLastAccelerometer, mLastMagnetometer);

            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rotationMatrix, AXIS_X, AXIS_Z, adjustedRotationMatrix);
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);

            mCurrentAzim = orientation[0];
            mCurrentPitch = orientation[1];
            mCurrentRoll = orientation[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void onUpdateScene(FrameTime frameTime) {
        Frame frame = arSceneView.getArFrame();

        for (int i = 0; i < 3; i++) {
            if (mAnchorNode[i] != null) {
                if (mAnchorNode[i].getAnchor().getTrackingState() != TrackingState.TRACKING
                        && arSceneView.getArFrame().getCamera().getTrackingState() == TrackingState.TRACKING) {
                    // Detach the old anchor
                    List<Node> children = new ArrayList<>(mAnchorNode[i].getChildren());
                    for (Node n : children) {
                        if (n instanceof Node) {
                            mAnchorNode[i].removeChild(n);
                            n.setParent(null);
                        }
                    }
                    arSceneView.getScene().removeChild(mAnchorNode[i]);
                    mAnchorNode[i].getAnchor().detach();
                    mAnchorNode[i].setParent(null);
                    mAnchorNode[i] = null;
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            if (mAnchorNode[i] == null) {
                if (createNode(i) == false) continue;
            }
        }

        return;
    }

    public boolean createNode(int i) {
        float dLatitude = (float) (markers[i].getLatitude() - mCurrentLocation.getLatitude()) * 110900f;
        float dLongitude = (float) (markers[i].getLongitude() - mCurrentLocation.getLongitude()) * 88400f;

        if( i == 0 ) {
            dLatitude = 3f;
            dLongitude = 0f;
            return false;
        }
        else if ( i == 1 ){
            dLatitude = -3f;
            dLongitude = 0f;
        }
        else{
            dLatitude = 0f;
            dLongitude = 3f;
        }

        float distance = (float) Math.sqrt((dLongitude * dLongitude) + (dLatitude * dLatitude));

        if (distance > 15) {
            return false;
        }
        float height = -0.5f;
        Vector3 objVec = new Vector3(dLongitude, dLatitude, height);

        Vector3 xUnitVec;
        Vector3 yUnitVec;
        Vector3 zUnitVec;

        zUnitVec = new Vector3((float) (Math.cos(mCurrentPitch) * Math.sin(mCurrentAzim)), (float) (Math.cos(mCurrentPitch) * Math.cos(mCurrentAzim)), (float) (-Math.sin(mCurrentPitch)));
        zUnitVec = zUnitVec.normalized().negated();

        yUnitVec = new Vector3((float) (Math.sin(mCurrentPitch) * Math.sin(mCurrentAzim)), (float) (Math.sin(mCurrentPitch) * Math.cos(mCurrentAzim)), (float) (Math.cos(mCurrentPitch))).normalized();

        float wx = zUnitVec.x;
        float wy = zUnitVec.y;
        float wz = zUnitVec.z;

        float yx = yUnitVec.x;
        float yy = yUnitVec.y;
        float yz = yUnitVec.z;

        float t = 1 - (float) Math.cos(mCurrentRoll);
        float s = (float) Math.sin(mCurrentRoll);
        float c = (float) Math.cos(mCurrentRoll);

        float[][] rotMat = {{wx * wx * t + c, wx * wy * t + wz * s, wx * wz * t - wy * s},
                {wy * wx * t - wz * s, wy * wy * t + c, wy * wz * t + wx * s},
                {wz * wx * t + wy * s, wz * wy * t - wx * s, wz * wz * t + c}};

        yUnitVec = new Vector3(yx * rotMat[0][0] + yy * rotMat[0][1] + yz * rotMat[0][2],
                yx * rotMat[1][0] + yy * rotMat[1][1] + yz * rotMat[1][2],
                yx * rotMat[2][0] + yy * rotMat[2][1] + yz * rotMat[2][2]).normalized();


        xUnitVec = Vector3.cross(yUnitVec, zUnitVec).normalized();

        float xPos = Vector3.dot(objVec, xUnitVec);
        float yPos = Vector3.dot(objVec, yUnitVec);
        float zPos = Vector3.dot(objVec, zUnitVec);

        Vector3 xAxis = arSceneView.getScene().getCamera().getRight().normalized().scaled(xPos);
        Vector3 yAxis = arSceneView.getScene().getCamera().getUp().normalized().scaled(yPos);
        Vector3 zAxis = arSceneView.getScene().getCamera().getBack().normalized().scaled(zPos);
        Vector3 objectPos = new Vector3(xAxis.x + yAxis.x + zAxis.x, xAxis.y + yAxis.y + zAxis.y, xAxis.z + yAxis.z + zAxis.z);
        Vector3 cameraPos = arSceneView.getScene().getCamera().getWorldPosition();

        Vector3 position = Vector3.add(cameraPos, objectPos);

        // Create an ARCore Anchor at the position.
        Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
        Anchor anchor = arSceneView.getSession().createAnchor(pose);

        mAnchorNode[i] = new AnchorNode(anchor);
        mAnchorNode[i].setParent(arSceneView.getScene());

        Vector3 v = new Vector3(0f, 0f, 1f);
        xPos = Vector3.dot(v, xUnitVec);
        yPos = Vector3.dot(v, yUnitVec);
        zPos = Vector3.dot(v, zUnitVec);

        xAxis = arSceneView.getScene().getCamera().getRight().normalized().scaled(xPos);
        yAxis = arSceneView.getScene().getCamera().getUp().normalized().scaled(yPos);
        zAxis = arSceneView.getScene().getCamera().getBack().normalized().scaled(zPos);

        Vector3 up = new Vector3(xAxis.x + yAxis.x + zAxis.x, xAxis.y + yAxis.y + zAxis.y, xAxis.z + yAxis.z + zAxis.z).normalized();

        return true;
    }
}