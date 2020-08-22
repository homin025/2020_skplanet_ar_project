package com.example.gammeeting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.gammeeting.util.Logger;
import com.example.gammeeting.view.HandView;

import java.util.ArrayList;

public class SingleCameraActivity extends AppCompatActivity implements GameFragment.GameEventListener {

    GameFragment gameFragment;

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;
    LinearLayout layoutGameCount;
    CountDownTimer countDownTimer;

    Button button, buttonWin, buttonLose, buttonStart;

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
        countDownTimer = new CountDownTimer(7000, 1000) {
            ImageView imageGameCount = layoutGameCount.findViewById(R.id.imageGameCount);

            @Override
            public void onTick(long l) {
                switch ((int) Math.round((double)l / 1000)) {
                    case 6:
                        imageGameCount.setImageResource(R.drawable.ready);
                        break;
                    case 5:
                        layoutGameCount.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        imageGameCount.setImageResource(R.drawable.three);
                        break;
                    case 2:
                        imageGameCount.setImageResource(R.drawable.two);
                        break;
                    case 1:
                        imageGameCount.setImageResource(R.drawable.one);
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
//        gameChooseDialog.setOnDismissListener(view ->
//                setLayoutVisibility(gameChooseDialog.getChoice()));

        gameChooseDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setLayoutVisibility(gameChooseDialog.getChoice());
                countDownTimer.start();
            }
        });

        button = findViewById(R.id.buttonGameChoose);
        buttonWin = findViewById(R.id.buttonWin);
        buttonLose = findViewById(R.id.buttonLose);
        buttonStart = findViewById(R.id.buttonStart);

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

        textViewTrackingImage = findViewById(R.id.textViewTrackingImage);

        handViewOpponent = (HandView)findViewById(R.id.handViewOpponent);
        handViewSelf = (HandView)findViewById(R.id.handViewSelf);
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

    private void setLayoutVisibility(int index) {
        for(int i=0; i<layouts.size(); i++)
            layouts.get(i).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);

        switch(index) {
            case 0:
                findViewById(R.id.buttonScissor).setOnClickListener(view -> {
                    UserHandType = "scissors";
                    setGameResult();

                    new Handler().postDelayed(() -> {
                        gameResultDialog.create();
                        gameResultDialog.setResult(gameResult);
                        gameResultDialog.show();
                    }, 1000);
                });
                findViewById(R.id.buttonRock).setOnClickListener(view -> {
                    UserHandType = "rock";
                    setGameResult();

                    new Handler().postDelayed(() -> {
                        gameResultDialog.create();
                        gameResultDialog.setResult(gameResult);
                        gameResultDialog.show();
                    }, 1000);
                });
                findViewById(R.id.buttonPaper).setOnClickListener(view -> {
                    UserHandType = "paper";
                    setGameResult();

                    new Handler().postDelayed(() -> {
                        gameResultDialog.create();
                        gameResultDialog.setResult(gameResult);
                        gameResultDialog.show();
                    }, 1000);
                });
                break;
            case 1:
                findViewById(R.id.buttonLeft).setOnClickListener(view -> {
                });
                findViewById(R.id.buttonLeft).setOnClickListener(view -> {
                });
                break;
            case 2:

                findViewById(R.id.buttonChoice1).setOnClickListener(view -> {
                });
                findViewById(R.id.buttonChoice2).setOnClickListener(view -> {
                });
                findViewById(R.id.buttonChoice3).setOnClickListener(view -> {
                });
                findViewById(R.id.buttonChoice4).setOnClickListener(view -> {
                });
        }
    }

    private void setHandViewVisibility(boolean visible) {
        if (visible) {
            handViewOpponent.setVisibility(View.VISIBLE);
            handViewSelf.setVisibility(View.VISIBLE);
        }
        else {
            handViewOpponent.setVisibility(View.INVISIBLE);
            handViewSelf.setVisibility(View.INVISIBLE);
        }
    }

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
}