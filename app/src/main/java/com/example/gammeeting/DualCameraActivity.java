package com.example.gammeeting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class DualCameraActivity extends AppCompatActivity implements GameFragment.GameEventListener {

    FrameLayout container;

    GameFragment gameFragment;
    DetectFragment detectFragment;
    ConstraintLayout detectFragmentContainer;

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;

    Button button, buttonWin, buttonLose;

    String idolName;

    IntroDialog introDialog;
    FitLogoDialog fitLogoDialog;
    GameChooseDialog gameChooseDialog;
    GameResultDialog gameResultDialog;

    TextView textViewTrackingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dualcamera);

        container = (FrameLayout)findViewById(R.id.container);

        gameFragment = (GameFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentGame);
        detectFragment = (DetectFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentDetect);
        detectFragmentContainer = (ConstraintLayout) findViewById(R.id.fragmentDetectContainer);

//        detectFragmentContainer.post(() -> {
//            // 화면의 35%만큼 차지하게 margin 설정
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//            int screenWidth = displayMetrics.widthPixels;
//            int screenHeight = displayMetrics.heightPixels;
//            int offset = (int)(screenHeight * 0.35 - screenWidth);
//
//            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)detectFragmentContainer.getLayoutParams();
//            params.setMargins(0,0,0, offset);
//            detectFragmentContainer.setLayoutParams(params);
//            detectFragmentContainer.postInvalidate();
//        });

        layoutGame1 = findViewById(R.id.layoutGame1);
        layoutGame2 = findViewById(R.id.layoutGame2);
        layoutGame3 = findViewById(R.id.layoutGame3);

        layouts = new ArrayList<>();
        layouts.add(layoutGame1);
        layouts.add(layoutGame2);
        layouts.add(layoutGame3);

        introDialog = new IntroDialog(this);
        fitLogoDialog = new FitLogoDialog(this);
        gameChooseDialog = new GameChooseDialog(this);
        gameResultDialog = new GameResultDialog(this);

        // introDialog, fitLogoDialog 순서대로 표시, 끝나면 instructionDone이 true로 바뀜
        introDialog.create();
        introDialog.setOnDismissListener(view -> fitLogoDialog.show());
        introDialog.show();

        fitLogoDialog.setOnDismissListener(view -> gameFragment.setInstructionDone(true));

        // AR 마커를 인식하면 GameChooseDialog 표시
        gameChooseDialog.setOnDismissListener(view ->
                setLayoutVisibility(gameChooseDialog.getChoice()));

        button = findViewById(R.id.buttonGameChoose);
        buttonWin = findViewById(R.id.buttonWin);
        buttonLose = findViewById(R.id.buttonLose);

        button.setOnClickListener(view -> gameChooseDialog.show());
        buttonWin.setOnClickListener(view -> {
            gameResultDialog.create();
            gameResultDialog.setResult(true);
            gameResultDialog.show();
        });
        buttonLose.setOnClickListener(view -> {
            gameResultDialog.create();
            gameResultDialog.setResult(false);
            gameResultDialog.show();
        });

        textViewTrackingImage = findViewById(R.id.textViewTrackingImage);
    }

    private void setLayoutVisibility(int index) {
        for(int i=0; i<layouts.size(); i++)
            layouts.get(i).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onMarkerFound(String name) {
        idolName = name;
        textViewTrackingImage.setText(idolName);

        gameChooseDialog.show();
        gameChooseDialog.setOpponentName(idolName);
    }
}