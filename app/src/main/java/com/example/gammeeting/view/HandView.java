package com.example.gammeeting.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.hand_view_self, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HandView, 0, 0);
        boolean isOpponent = false;
        try {
            isOpponent = a.getBoolean(R.styleable.HandView_opponent, false);
        } catch (Exception e) {
            Log.e("HandView", e.getMessage());
            e.printStackTrace();
        } finally {
            a.recycle();
        }

        //inflate(context, isOpponent?R.layout.hand_view_opponent:R.layout.hand_view_self, this);

        textViewName = findViewById(R.id.textViewName);
        textViewHand = findViewById(R.id.textViewHand);
        imageViewHand = findViewById(R.id.imageViewHand);

        if(!isOpponent)
            textViewName.setText("YOU");
    }

    public HandView setName(String name) {
        textViewName.setText(name);
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

        textViewHand.setText(handText);
        imageViewHand.setImageResource(imgId);

        return this;
    }
}
