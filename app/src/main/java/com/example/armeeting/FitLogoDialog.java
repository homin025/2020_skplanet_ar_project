package com.example.armeeting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

public class FitLogoDialog extends Dialog {

    public Button buttonOk;

    public FitLogoDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fit_logo);

        getWindow().setBackgroundDrawable(null);
        new Handler(Looper.getMainLooper()).postDelayed(() -> dismiss(), 2500);
    }
}
