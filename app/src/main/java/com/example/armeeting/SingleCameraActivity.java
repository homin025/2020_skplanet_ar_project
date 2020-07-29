package com.example.armeeting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class SingleCameraActivity extends AppCompatActivity {

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;

    Button button, buttonWin, buttonLose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlecamera);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (null == savedInstanceState) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container_game, GameFragment.newInstance(), "fragment_game")
                    .commit();
        }

        layoutGame1 = findViewById(R.id.layoutGame1);
        layoutGame2 = findViewById(R.id.layoutGame2);
        layoutGame3 = findViewById(R.id.layoutGame3);

        layouts = new ArrayList<>();
        layouts.add(layoutGame1);
        layouts.add(layoutGame2);
        layouts.add(layoutGame3);

        // 임시용 게임 선택 다이얼로그 (작업본 곧 업로드)
        // 마커를 인식시키면 게임 선택하도록 하기
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        CharSequence items[] = new CharSequence[] {"가위바위보", "참참참", "스피드퀴즈"};
        adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setLayoutVisibility(i);
                dialogInterface.dismiss();
            }
        });
        adb.setTitle("게임을 선택하세요");

        button = findViewById(R.id.button);
        button.setOnClickListener(view -> adb.show());

        IntroDialog introDialog = new IntroDialog(this);
        FitLogoDialog fitLogoDialog = new FitLogoDialog(this);
        GameResultDialog gameResultDialog = new GameResultDialog(this);

        introDialog.show();
        introDialog.setOnDismissListener(dialogInterface -> fitLogoDialog.show());

        buttonWin = findViewById(R.id.buttonWin);
        buttonLose = findViewById(R.id.buttonLose);

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
}