package com.example.gammeeting;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gammeeting.view.HandView;

public class DualCameraActivity extends AppCompatActivity
        implements GameFragment.GameEventListener, DetectFragment.DetectEventListener {

    FrameLayout container;

    GameFragment gameFragment;
    DetectFragment detectFragment;
    ConstraintLayout detectFragmentContainer;

    LinearLayout layoutGameCount;
    CountDownTimer countDownTimer;

    Button button, buttonWin, buttonLose, buttonStart, buttonDetect;

    IntroDialog introDialog;
    FitLogoDialog fitLogoDialog;
    GameChooseDialog gameChooseDialog;
    GameResultDialog gameResultDialog;

    String idolName;
    String idolHandType;
    HandView handViewSelf, handViewOpponent;

    String UserHandType;
    boolean gameResult;

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

        layoutGameCount = findViewById(R.id.layoutGameCount);
        countDownTimer = new CountDownTimer(5000, 1000) {
            ImageView imageGameCount = layoutGameCount.findViewById(R.id.imageGameCount);

            @Override
            public void onTick(long l) {
                switch ((int) Math.round((double)l / 1000)) {
                    case 5:
                    case 4:
                        layoutGameCount.setVisibility(View.VISIBLE);
                        imageGameCount.setImageResource(R.drawable.image_ready);
                        break;
                    case 3:
                        imageGameCount.setImageResource(R.drawable.image_three);
                        break;
                    case 2:
                        imageGameCount.setImageResource(R.drawable.image_two);
                        break;
                    case 1:
                        imageGameCount.setImageResource(R.drawable.image_one);
                        break;
                }
            }

            @Override
            public void onFinish() {
                layoutGameCount.setVisibility(View.INVISIBLE);
            }
        };

        introDialog = new IntroDialog(this);
        fitLogoDialog = new FitLogoDialog(this);
        gameChooseDialog = new GameChooseDialog(this);
        gameResultDialog = new GameResultDialog(this);

        // introDialog, fitLogoDialog 순서대로 표시, 끝나면 instructionDone이 true로 바뀜
        introDialog.create();
        introDialog.setOnDismissListener(view -> fitLogoDialog.show());
        introDialog.show();

        fitLogoDialog.setOnDismissListener(view -> gameFragment.setInstructionDone(true));

        gameChooseDialog.setOnDismissListener(dialogInterface -> {
            detectFragment.resumeDetection();
            countDownTimer.start();
        });

        handViewOpponent = (HandView)findViewById(R.id.handViewOpponent);
        handViewSelf = (HandView)findViewById(R.id.handViewSelf);

        // 디버깅용 버튼
//        button = findViewById(R.id.buttonGameChoose);
//        buttonWin = findViewById(R.id.buttonWin);
//        buttonLose = findViewById(R.id.buttonLose);
//        buttonStart = findViewById(R.id.buttonStart);
//        buttonDetect = findViewById(R.id.buttonDetect);
//
//        button.setOnClickListener(view -> gameChooseDialog.show());
//        buttonWin.setOnClickListener(view -> {
//            gameResultDialog.create();
//            gameResultDialog.setResult(true);
//            gameResultDialog.show();
//        });
//        buttonLose.setOnClickListener(view -> {
//            gameResultDialog.create();
//            gameResultDialog.setResult(false);
//            gameResultDialog.show();
//        });
//        buttonStart.setOnClickListener(view -> {
//            countDownTimer.start();
//        });
//        buttonDetect.setOnClickListener(view -> {
//            detectFragment.resumeDetection();
//        });
    }

    private void showHandView(boolean visible) {
        if (visible) {
            handViewOpponent.setVisibility(View.VISIBLE);
            handViewSelf.setVisibility(View.VISIBLE);
        }
        else {
            handViewOpponent.setVisibility(View.INVISIBLE);
            handViewSelf.setVisibility(View.INVISIBLE);
        }
    }

    public void showGameResult(int delay) {
        setGameResult();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            gameResultDialog.create();
            gameResultDialog.setResult(gameResult);
            gameResultDialog.show();
        }, delay);
    }

    private void setGameResult() {
        if (UserHandType.equals(idolHandType)) {
            //gameResult = ?;
        }
        else {
            switch(UserHandType) {
                case "rock":
                    if (idolHandType.equals("paper")) {
                        gameResult = false;
                    } else {
                        gameResult = true;
                    }
                    break;
                case "scissors":
                    if (idolHandType.equals("rock")) {
                        gameResult = false;
                    } else {
                        gameResult = true;
                    }
                    break;
                case "paper":
                    if (idolHandType.equals("scissors")) {
                        gameResult = false;
                    } else {
                        gameResult = true;
                    }
                    break;
            }
        }
    }

    @Override
    public void onMarkerFound(GameFragment.Idol idol) {
        // 마커가 인식됐을 때, 받아오는 클래스를 만듬 (아이돌 이름과 아이돌이 낼 손 모양)
        idolName = idol.getName();
        idolHandType = idol.getHandType();

        gameChooseDialog.show();
        gameChooseDialog.setOpponentName(idolName);

        // 아이돌의 이름과 손 모양을 세팅함
        handViewOpponent.setName(idolName);
        switch (idolHandType) {
            case "rock":
                handViewOpponent.setHandType(HandView.ROCK);
                break;
            case "scissors":
                handViewOpponent.setHandType(HandView.SCISSORS);
                break;
            case "paper":
                handViewOpponent.setHandType(HandView.PAPER);
                break;
        }
    }

    @Override
    public void onVideoStarted() {
        Log.i("onVideoStarted", "Dual");
        countDownTimer.start();
    }

    @Override
    public void onHandDetected(int handType) {
        detectFragment.stopDetection();

        switch(handType) {
            case 101:
                UserHandType = "rock";
                break;
            case 102:
                UserHandType = "scissors";
                break;
            case 103:
                UserHandType = "paper";
                break;
        }

        // 사용자의 손 모양이 인식되면 HandView를 보이도록 함
        handViewSelf.setHandType(handType).setName("YOU");
        showHandView(true);
        showGameResult(1000);
    }

    @Override
    public void onHandDisappeared() {
    }
}