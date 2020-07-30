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

public class SingleCameraActivity extends AppCompatActivity {

    LinearLayout layoutGame1, layoutGame2, layoutGame3;
    ArrayList<LinearLayout> layouts;

    Button button;

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


        IntroDialog introDialog = new IntroDialog(this);
        FitLogoDialog fitLogoDialog = new FitLogoDialog(this);
        GameChooseDialog gameChooseDialog = new GameChooseDialog(this);

        introDialog.show();
        introDialog.setOnDismissListener(view -> fitLogoDialog.show());

        // AR 마커를 인식하면 GameChooseDialog 표시
        gameChooseDialog.setOnDismissListener(view ->
                setLayoutVisibility(gameChooseDialog.getChoice()));

        button = findViewById(R.id.buttonGameChoose);
        button.setOnClickListener(view -> gameChooseDialog.show());
    }

    private void setLayoutVisibility(int index) {
        for(int i=0; i<layouts.size(); i++)
            layouts.get(i).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);
    }
}