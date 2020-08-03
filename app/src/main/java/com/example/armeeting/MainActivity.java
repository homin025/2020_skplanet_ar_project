package com.example.armeeting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button buttonSingle, buttonDual;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSingle = findViewById(R.id.buttonSingle);
        buttonDual = findViewById(R.id.buttonDual);

        buttonSingle.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SingleCameraActivity.class);
            startActivity(intent);
        });

        buttonDual.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DualCameraActivity.class);
            startActivity(intent);
        });
    }
}