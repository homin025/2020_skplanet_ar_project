package com.example.armeeting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class DualCameraActivity extends AppCompatActivity {

    LinearLayout layoutGame;
    Button buttonRock, buttonScissor, buttonPaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dualcamera);

        layoutGame = findViewById(R.id.layoutGame1);
        buttonRock = findViewById(R.id.buttonRock);
        buttonScissor = findViewById(R.id.buttonScissor);
        buttonPaper = findViewById(R.id.buttonPaper);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (null == savedInstanceState) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_game, GameFragment.newInstance())
                    .commit();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_opencv, OpenCvFragment.newInstance())
                    .commit();
        }
    }

}