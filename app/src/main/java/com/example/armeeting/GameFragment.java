package com.example.armeeting;

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

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
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


public class GameFragment extends ArFragment {
    private static final String TAG = "GameFragment";
    private static final boolean USE_SINGLE_IMAGE = false;
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            Log.e(TAG, "Sceneform requires Android N or later");

        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION)
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
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
        fileNames.put("img1.png", "vid4.mp4");
//        fileNames.put("img2.png", "vid2.mp4");
//        fileNames.put("img3.png", "vid3.mp4");

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

}