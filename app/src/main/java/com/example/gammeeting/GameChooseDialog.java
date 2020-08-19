package com.example.gammeeting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class GameChooseDialog extends Dialog{

    int choice;

    public Button buttonGame1, buttonGame2, buttonGame3;
    public TextView textViewOpponent, textViewOther;

    public GameChooseDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_choose);

        textViewOpponent = findViewById(R.id.textViewOpponent);
        buttonGame1 = findViewById(R.id.buttonGame1);
        buttonGame2 = findViewById(R.id.buttonGame2);
        buttonGame3 = findViewById(R.id.buttonGame3);
        textViewOther = findViewById(R.id.textViewOther);

        buttonGame1.setOnClickListener(view -> { choice = 0; dismiss(); });
        buttonGame2.setOnClickListener(view -> { choice = 1; dismiss(); });
        buttonGame3.setOnClickListener(view -> { choice = 2; dismiss(); });
        textViewOther.setOnClickListener(view -> cancel());
    }

    public void setOpponentName(String name) {
        String text = getContext().getString(R.string.armeeting_game_choose);
        textViewOpponent.setText(name + text);
    }

    public int getChoice() { return choice; }
}
