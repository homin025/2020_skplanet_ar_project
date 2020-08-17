package com.example.armeeting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

public class IntroDialog extends Dialog {

    public Button buttonOk;

    public IntroDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_intro);

        buttonOk = findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(view -> dismiss());
    }
}
