package com.example.armeeting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlecamera);

        layoutGame = findViewById(R.id.layoutGame1);
        buttonRock = findViewById(R.id.buttonRock);
        buttonScissor = findViewById(R.id.buttonScissor);
        buttonPaper = findViewById(R.id.buttonPaper);

//        FragmentManager fragmentManager = getSupportFragmentManager();

//        if (null == savedInstanceState) {
//            fragmentManager.beginTransaction()
//                    .replace(R.id.fragment_game, GameFragment.newInstance())
//                    .commit();
//        }
    }
}