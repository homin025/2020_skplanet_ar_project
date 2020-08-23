package com.example.gammeeting.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
        String inflaterService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflaterService);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.HandView, 0, 0);
        boolean isOpponent = false;
        try {
            isOpponent = typedArray.getBoolean(R.styleable.HandView_opponent, false);
        } catch (Exception e) {
            Log.e("HandView", e.getMessage());
            e.printStackTrace();
        } finally {
            View view = layoutInflater.inflate(isOpponent?R.layout.handview_opponent:R.layout.handview_self, this, false);
            addView(view);
        }

        textViewName = (TextView)findViewById(R.id.textViewName);
        textViewHand = (TextView)findViewById(R.id.textViewHand);
        imageViewHand = (ImageView)findViewById(R.id.imageViewHand);

        setType(typedArray);
    }

    private void setType(TypedArray typedArray) {
        textViewName.setText(typedArray.getString(R.styleable.HandView_textviewname));
        textViewHand.setText(typedArray.getString(R.styleable.HandView_textviewhand));
        imageViewHand.setImageResource(typedArray.getResourceId(R.styleable.HandView_imageviewhand, R.drawable.ic_vector_rock_unclicked));
        typedArray.recycle();
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
                imgId = R.drawable.ic_vector_rock_clicked;
                break;
            case SCISSORS:
                handText = "가위";
                imgId = R.drawable.ic_vector_scissors_clicked;
                break;
            case PAPER:
                handText = "보";
                imgId = R.drawable.ic_vector_paper_clicked;
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
