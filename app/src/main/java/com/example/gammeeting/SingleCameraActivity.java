package com.example.gammeeting;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    String idolName;
    String idolHandType;

    IntroDialog introDialog;
    FitLogoDialog fitLogoDialog;
    GameChooseDialog gameChooseDialog;
    GameResultDialog gameResultDialog;
    TextView textViewTrackingImage;

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
        gameChooseDialog.setOnDismissListener(view ->
                setLayoutVisibility(gameChooseDialog.getChoice()));

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
    }

    private void setLayoutVisibility(int index) {
        for(int i=0; i<layouts.size(); i++)
            layouts.get(i).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);
    }

    public void onMarkerFound(GameFragment.Idol idol) {
        idolName = idol.getName();
        idolHandType = idol.getHandType();

        textViewTrackingImage.setText(idolName);

        gameChooseDialog.show();
        gameChooseDialog.setOpponentName(idolName);
    }
}