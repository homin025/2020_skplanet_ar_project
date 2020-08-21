package com.example.gammeeting.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gammeeting.R;

public class HandView extends ConstraintLayout {
    public static final int ROCK = 101;
    public static final int SCISSORS = 102;
    public static final int PAPER = 103;

    TextView textViewName, textViewHand;
    ImageView imageViewHand;

    public HandView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.handview_opponent, this, true);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HandView, 0, 0);
        boolean isOpponent = false;
        try {
            isOpponent = typedArray.getBoolean(R.styleable.HandView_opponent, false);
        } catch (Exception e) {
            Log.e("HandView", e.getMessage());
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }

        LayoutInflater.from(context).inflate(isOpponent?R.layout.handview_opponent :R.layout.handview_self, this, true);

        textViewName = findViewById(R.id.textViewName);
        textViewHand = findViewById(R.id.textViewHand);
        imageViewHand = findViewById(R.id.imageViewHand);

        if(!isOpponent)
            textViewName.setText("YOU");
    }

    public HandView setName(String name) {
        textViewName.setText(name);

        ((Activity)getContext()).runOnUiThread(() -> textViewName.setText(name));

        return this;
    }

    public HandView setHandType(int type) {
        String handText;
        int imgId;
        switch(type) {
            case ROCK:
                handText = "바위";
                imgId = R.drawable.ic_vector_rock;
                break;
            case SCISSORS:
                handText = "가위";
                imgId = R.drawable.ic_vector_scissors;
                break;
            case PAPER:
                handText = "보";
                imgId = R.drawable.ic_vector_paper;
                break;
            default:
                handText = "";
                imgId = -1;
                break;
        }

        if(imgId == -1) {
            Log.e("HandView", "Invalid hand type");
            return this;
        }

        ((Activity)getContext()).runOnUiThread(() -> {
            textViewHand.setText(handText);
            imageViewHand.setImageResource(imgId);
        });

        return this;
    }
}