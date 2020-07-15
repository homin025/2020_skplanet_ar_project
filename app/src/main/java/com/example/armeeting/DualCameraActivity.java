package com.example.armeeting;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.armeeting.DualCameraFragment;

public class DualCameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dualcamera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DualCameraFragment.newInstance())
                    .commit();
        }
    }

}