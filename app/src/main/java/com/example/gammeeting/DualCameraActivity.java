package com.example.gammeeting;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class DualCameraActivity extends AppCompatActivity
        implements GameFragment.GameEventListener, DetectFragment.DetectEventListener {

    FrameLayout container;

    GameFragment gameFragment;
    DetectFragment detectFragment;
    ConstraintLayout detectFragmentContainer;

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;

    Button button, buttonWin, buttonLose, buttonStart, buttonDetect;

    String idolName;
    String detectResult;

    IntroDialog introDialog;
    FitLogoDialog fitLogoDialog;
    GameChooseDialog gameChooseDialog;
    GameResultDialog gameResultDialog;

    TextView textViewTrackingImage;

    ImageView imageView;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dualcamera);

        container = (FrameLayout)findViewById(R.id.container);

        gameFragment = (GameFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentGame);
        detectFragment = (DetectFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentDetect);
        detectFragmentContainer = (ConstraintLayout) findViewById(R.id.fragmentDetectContainer);

        detectFragment.setDetectionEventListener(this);
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

        countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long l) {
                switch ((int) Math.round((double)l / 1000)) {
                    case 5:
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.drawable.ready);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.three);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.two);
                        break;
                    case 1:
                        imageView.setImageResource(R.drawable.one);
                        break;
                }
            }

            @Override
            public void onFinish() {
                imageView.setVisibility(View.INVISIBLE);
            }
        };

        button = findViewById(R.id.buttonGameChoose);
        buttonWin = findViewById(R.id.buttonWin);
        buttonLose = findViewById(R.id.buttonLose);
        buttonStart = findViewById(R.id.buttonStart);
        buttonDetect = findViewById(R.id.buttonDetect);

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
        buttonStart.setOnClickListener(view -> {
            countDownTimer.start();
        });
        buttonDetect.setOnClickListener(view -> {
            // isHandDetected = false로 바꿔서
            // 손 모양이 결정된 후 중지된 인식 과정을 다시 활성화
            // 안그러면 박스가 인식됐을때 상태 그대로 멈춰있음
            detectFragment.resumeDetection();
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

    @Override
    public void onHandDetected(int handType) {
        switch(handType) {
            case 101:
                detectResult = "rock";
                break;
            case 102:
                detectResult = "scissors";
                break;
            case 103:
                detectResult = "paper";
                break;
        }

    }

    @Override
    public void onHandDisappeared() {

    }
}