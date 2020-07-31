package com.example.armeeting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

public class SingleCameraActivity extends AppCompatActivity implements GameFragment.GameEventListener {

    GameFragment gameFragment;

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;

    Button button, buttonWin, buttonLose;

    String idolName;

    IntroDialog introDialog;
    FitLogoDialog fitLogoDialog;
    GameChooseDialog gameChooseDialog;
    GameResultDialog gameResultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlecamera);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (null == savedInstanceState) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentGame, GameFragment.newInstance(), "fragment_game")
                    .commit();
        }
        gameFragment = (GameFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentGame);

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

        introDialog.show();
        introDialog.setOnDismissListener(view -> fitLogoDialog.show());

        // AR 마커를 인식하면 GameChooseDialog 표시
        gameChooseDialog.setOnDismissListener(view ->
                setLayoutVisibility(gameChooseDialog.getChoice()));

        button = findViewById(R.id.buttonGameChoose);
        buttonWin = findViewById(R.id.buttonWin);
        buttonLose = findViewById(R.id.buttonLose);

        button.setOnClickListener(view -> gameChooseDialog.show());
        buttonWin.setOnClickListener(view -> {
            gameResultDialog.setResult(true);
            gameResultDialog.show();
        });
        buttonLose.setOnClickListener(view -> {
            gameResultDialog.setResult(false);
            gameResultDialog.show();
        });
    }

    private void setLayoutVisibility(int index) {
        for(int i=0; i<layouts.size(); i++)
            layouts.get(i).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onMarkerFound(String name) {
        idolName = name;
        gameChooseDialog.show();
        gameChooseDialog.setOpponentName(idolName);
    }
}