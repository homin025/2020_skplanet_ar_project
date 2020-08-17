package com.example.armeeting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class DualCameraActivity extends AppCompatActivity {

    GameFragment gameFragment;
    DetectFragment detectFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dualcamera);

        gameFragment = GameFragment.newInstance();
        detectFragment = DetectFragment.newInstance();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (null == savedInstanceState) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container_game, gameFragment)
                    .commit();
            fragmentManager.beginTransaction()
                    .replace(R.id.container_detect, detectFragment)
                    .commit();
        }
    }
}