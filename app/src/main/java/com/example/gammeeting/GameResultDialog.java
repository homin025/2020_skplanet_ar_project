package com.example.gammeeting;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class GameResultDialog extends Dialog {

    ImageView imageView;
    TextView textViewTitle, textViewDescription;
    Button button;

    public GameResultDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_result);

        imageView = findViewById(R.id.imageView);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        button = findViewById(R.id.button);
    }

    public void setResult(boolean win) {
        Resources res = getContext().getResources();

        if(win) {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.win));
            textViewTitle.setText(res.getString(R.string.result_title_win));
            textViewDescription.setText(res.getString(R.string.result_description_win));
            button.setText(res.getString(R.string.result_button_win));
        }
        else {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.lose));
            textViewTitle.setText(res.getString(R.string.result_title_lose));
            textViewDescription.setText(res.getString(R.string.result_description_lose));
            button.setText(res.getString(R.string.result_button_lose));
        }
        button.setOnClickListener(view->dismiss());
    }
}
