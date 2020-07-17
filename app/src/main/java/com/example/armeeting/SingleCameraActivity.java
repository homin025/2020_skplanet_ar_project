package com.example.armeeting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SingleCameraActivity extends AppCompatActivity {

    LinearLayout layoutGame;
    Button buttonRock, buttonScissor, buttonPaper;

    GameFragment arFragment;

    MediaPlayer mediaPlayer;
    ExternalTexture texture;
    ModelRenderable videoRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlecamera);

        layoutGame = findViewById(R.id.layoutGame1);
        buttonRock = findViewById(R.id.buttonRock);
        buttonScissor = findViewById(R.id.buttonScissor);
        buttonPaper = findViewById(R.id.buttonPaper);

        arFragment = (GameFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_game);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        texture = new ExternalTexture();
        mediaPlayer = MediaPlayer.create(this, R.raw.vid4);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(false);
        ModelRenderable.builder()
                .setSource(this, R.raw.video_screen)
                .build()
                .thenAccept(modelRenderable -> {
                    videoRenderable = modelRenderable;
                    videoRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    videoRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1.0f, 0.098f));
                });
    }

    public void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if(frame == null)
            return;

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case TRACKING:
                    //arFragment.getArSceneView().getScene().addChild(createVideoNode(augmentedImage));
                    break;
                case STOPPED:
                    break;
            }
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
}