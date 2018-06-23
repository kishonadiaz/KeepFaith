package com.example.myapplication.fragments;

import android.app.ActivityManager;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.myapplication.R;

public class ExitButtonView extends Service {
    private WindowManager mWindowManager;
    private View mFloatingView;
    ActivityManager activityManager;
    MotionEvent motionEvent;
    WindowManager.LayoutParams params;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mFloatingView = LayoutInflater.from(this).inflate(R.layout.exit_button, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 90;
        params.y = 100;


        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mWindowManager.addView(mFloatingView, params);


        final View collapsedView = mFloatingView.findViewById(R.id.exit_collapse_view);


    }
}
