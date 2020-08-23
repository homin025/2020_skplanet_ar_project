package com.example.gammeeting;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gammeeting.view.HandView;

import java.util.ArrayList;

public class SingleCameraActivity extends AppCompatActivity implements GameFragment.GameEventListener {

    GameFragment gameFragment;

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;
    LinearLayout layoutGameCount;
    CountDownTimer countDownTimer;

    ImageButton buttonScissors, buttonRock, buttonPaper;
    ImageButton buttonLeft, buttonRight;
    Button buttonChoice1, buttonChoice2, buttonChoice3, buttonChoice4;

    IntroDialog introDialog;
    FitLogoDialog fitLogoDialog;
    GameChooseDialog gameChooseDialog;
    GameResultDialog gameResultDialog;
    TextView textViewTrackingImage;

    String idolName;
    String idolHandType;
    HandView handViewSelf, handViewOpponent;

    String UserHandType;
    boolean gameResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlecamera);

        gameFragment = (GameFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentGame);

        layoutGame1 = findViewById(R.id.layoutGame1);
        layoutGame2 = findViewById(R.id.layoutGame2);
        layoutGame3 = findViewById(R.id.layoutGame3);

        layouts = new ArrayList<>();
        layouts.add(layoutGame1);
        layouts.add(layoutGame2);
        layouts.add(layoutGame3);

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

        // AR 마커를 인식하면 GameChooseDialog 표시
        gameChooseDialog.setOnDismissListener(dialogInterface -> {
                initGameUI(gameChooseDialog.getChoice());
                showGameUI(gameChooseDialog.getChoice());
                countDownTimer.start();
            });

            textViewTrackingImage = findViewById(R.id.textViewTrackingImage);

            handViewOpponent = (HandView)findViewById(R.id.handViewOpponent);
            handViewSelf = (HandView)findViewById(R.id.handViewSelf);

            // 디버깅용 버튼
//        button = findViewById(R.id.buttonGameChoose);
//        buttonWin = findViewById(R.id.buttonWin);
//        buttonLose = findViewById(R.id.buttonLose);
//        buttonStart = findViewById(R.id.buttonStart);
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
    }

    public void initGameUI(int index) {
        // 가위바위보
        buttonScissors = findViewById(R.id.buttonScissor);
        buttonScissors.setImageResource(R.drawable.ic_vector_scissors_unclicked);
        buttonScissors.setBackgroundResource(R.drawable.game_button_shape_unclicked);
        buttonScissors.setOnClickListener(view -> {
            UserHandType = "scissors";
            buttonScissors.setImageResource(R.drawable.ic_vector_scissors_clicked);
            buttonScissors.setBackgroundResource(R.drawable.game_button_shape_clicked);
            showGameResult(1000);
        });
        buttonRock = findViewById(R.id.buttonRock);
        buttonRock.setImageResource(R.drawable.ic_vector_rock_unclicked);
        buttonRock.setBackgroundResource(R.drawable.game_button_shape_unclicked);
        findViewById(R.id.buttonRock).setOnClickListener(view -> {
            UserHandType = "rock";
            buttonRock.setImageResource(R.drawable.ic_vector_scissors_clicked);
            buttonRock.setBackgroundResource(R.drawable.game_button_shape_clicked);
            showGameResult(1000);
        });
        buttonPaper = findViewById(R.id.buttonPaper);
        buttonPaper.setImageResource(R.drawable.ic_vector_paper_unclicked);
        buttonPaper.setBackgroundResource(R.drawable.game_button_shape_unclicked);
        findViewById(R.id.buttonPaper).setOnClickListener(view -> {
            UserHandType = "paper";
            buttonPaper.setImageResource(R.drawable.ic_vector_paper_clicked);
            buttonPaper.setBackgroundResource(R.drawable.game_button_shape_clicked);
            showGameResult(1000);
        });

        // 참참참
        findViewById(R.id.buttonLeft).setOnClickListener(view -> {
        });
        findViewById(R.id.buttonRight).setOnClickListener(view -> {
        });

        // 스피드퀴즈
        findViewById(R.id.buttonChoice1).setOnClickListener(view -> {
        });
        findViewById(R.id.buttonChoice2).setOnClickListener(view -> {
        });
        findViewById(R.id.buttonChoice3).setOnClickListener(view -> {
        });
        findViewById(R.id.buttonChoice4).setOnClickListener(view -> {
        });
    }

    private void showGameUI(int index) {
        for(int i=0; i<layouts.size(); i++)
            layouts.get(i).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);
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

        textViewTrackingImage.setText(idolName);

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
        Log.i("onVideoStarted", "Single");
        countDownTimer.start();
    }
}