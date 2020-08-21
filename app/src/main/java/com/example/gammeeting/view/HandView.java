package com.example.gammeeting.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gammeeting.R;

public class HandView extends ConstraintLayout {

    public static final int ROCK = 101;
    public static final int SCISSORS = 102;
    public static final int PAPER = 103;

    TextView textViewName, textViewHand;
    ImageView imageViewHand;

    public HandView(Context context) {
        super(context);
        initView(context, null);
    }

    public HandView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
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
        Toast.makeText(context, "" + isOpponent, Toast.LENGTH_SHORT).show();

        ViewGroup viewGroup;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null)
            return;

        viewGroup = (ViewGroup)layoutInflater.inflate(isOpponent ? R.layout.hand_opponent_view : R.layout.hand_self_view, this, true);
        Toast.makeText(context, ""+viewGroup, Toast.LENGTH_SHORT).show();

        textViewName = viewGroup.findViewById(R.id.textViewName);
        textViewHand = viewGroup.findViewById(R.id.textViewHand);
        imageViewHand = viewGroup.findViewById(R.id.imageViewHand);

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
