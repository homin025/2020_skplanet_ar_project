package com.example.gammeeting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button buttonSingle, buttonDual;

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