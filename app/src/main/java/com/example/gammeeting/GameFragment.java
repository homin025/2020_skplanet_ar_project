package com.example.gammeeting;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class GameFragment extends ArFragment {
    private static final String TAG = "GameFragment";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private Session session;
    private Scene scene;

    public MediaPlayer mediaPlayer;
    public ExternalTexture texture;
    public ModelRenderable videoRenderable;

    GameEventListener listener;

    boolean instructionDone;
    String currTrackingImageName = "";

    public interface GameEventListener {
        void onMarkerFound(String name);
        // 게임들 끝났을 떄 (가위바위보 내는 타이밍, 참참참 타이밍) 호출되는 이벤트메소드 추가하기
    }

    public static GameFragment newInstance() {
        return new GameFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually./
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            Log.e(TAG, "Sceneform requires Android N or later");

        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();

        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION)
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");

        listener = (GameEventListener)context;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        texture = new ExternalTexture();
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.vid2);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(false);
        ModelRenderable.builder()
                .setSource(getContext(), R.raw.video_screen)
                .build()
                .thenAccept(modelRenderable -> {
                    videoRenderable = modelRenderable;
                    videoRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    videoRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1.0f, 0.098f));
                });

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        return view;
    }

    @Override protected Config getSessionConfiguration(Session session){
        getPlaneDiscoveryController().setInstructionView(null);

        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        setupAugmentedImageDatabase(config, session);

        session.configure(config);
        getArSceneView().setupSession(session);

        return config;
    }

    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        HashMap<String, String> fileNames = new HashMap<>();
        fileNames.put("img1.png", "방탄소년단");
        fileNames.put("img2.png", "NCT127");
        fileNames.put("img3.jpg", "레드벨벳");

        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(session);
        ArrayList<Bitmap> augmentedImageBitmap = new ArrayList<>();

        for(String imgName: fileNames.keySet()) {
            try (InputStream is = getActivity().getAssets().open(imgName)) {
                augmentedImageDatabase.addImage(fileNames.get(imgName), BitmapFactory.decodeStream(is));
            } catch (IOException e) {
                Log.e("error", "IOError on loading Bitmap");
                return false;
            }
        }

        if (augmentedImageBitmap == null)
            return false;

        config.setAugmentedImageDatabase(augmentedImageDatabase);

        return true;
    }


    public void onUpdateFrame(FrameTime frameTime) {
        Frame frame = getArSceneView().getArFrame();
        if(frame == null || !instructionDone)
            return;

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            String imageName = augmentedImage.getName();
            if(!currTrackingImageName.equals(imageName)) {
                listener.onMarkerFound(imageName);
                currTrackingImageName = imageName;
            }

            switch (augmentedImage.getTrackingState()) {
                case TRACKING:
                    //getArSceneView().getScene().addChild(createVideoNode(augmentedImage));
                    break;
                case STOPPED:
                    break;
            }
            break;
        }
    }

    private AnchorNode createVideoNode(AugmentedImage augmentedImage) {
        AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
        mediaPlayer.start();
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            anchorNode.setRenderable(videoRenderable);
            anchorNode.setLocalScale(new Vector3(
                    augmentedImage.getExtentX(), 1.0f, augmentedImage.getExtentZ()));
        });
        return anchorNode;
    }

    public void changeVideo() {
        texture = new ExternalTexture();
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.vid_alpha);
        mediaPlayer.setSurface(texture.getSurface());
        videoRenderable.getMaterial().setExternalTexture("videoTexture", texture);
    }

    public void setInstructionDone(boolean value) {
        instructionDone = value;
    }

    public void showHand(int type) {
        Frame frame = getArSceneView().getArFrame();
        if (frame == null)
            return;

        Pose pos = frame.getCamera().getPose().compose(Pose.makeTranslation(0, 0, -0.3f));
        Anchor anchor = getArSceneView().getSession().createAnchor(pos);
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(getArSceneView().getScene());

        Node hand = new Node();
        hand.setParent(anchorNode);

        Renderable handRenderable;
        switch(type) {
            case DetectFragment.DetectEventListener.ROCK:
                handRenderable = null;
                break;
            case DetectFragment.DetectEventListener.SCISSORS:
                handRenderable = null;
                break;
            case DetectFragment.DetectEventListener.PAPER:
                handRenderable = null;
                break;
            default:
                Log.e("GameFragment","Improper type of hand");
                return;
        }
        hand.setRenderable(handRenderable);

        // TODO: 매 프레임마다 손 계속 생성하지 않도록 DetectFragment 혹은 GameFragment 둘 중 하나에서 조정
        // TODO: GameFragment 생성 시 가위, 바위, 보 손 모델링 미리 로드해놓기, 적절하게 회전해놓기
    }

    public void removeHand() {
        // TODO: showHand에서 만든 앵커 삭제, 앵커 가져올 수 있는 방법 구글링
    }
}