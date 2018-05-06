package com.example.myapplication;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CHANNEL_ID = 001;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        final NotificationSystem notifacationSystem = new NotificationSystem(getBaseContext(),"KeepFaith");

        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }

        Button btn = this.findViewById(R.id.send_notifications);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sendNotification(view);
                notifacationSystem.sendNotification(FloatingViewService.class,R.drawable.ic_android_circle,R.drawable.ic_android_circle);
                finish();
            }
        });

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        Toast.makeText(activity, ""+Calendar.AM_PM, Toast.LENGTH_LONG).show();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 17);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),broadcast);



    }

    private void initializeView() {
        findViewById(R.id.notify_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //startService(new Intent(MainActivity.this , ExitButtonView.class));
                startService(new Intent(MainActivity.this, FloatingViewService.class));
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
                Toast.makeText(this,
                        "Here",
                        Toast.LENGTH_SHORT).show();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void sendNotification(View view){

        Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.ic_android_circle);

        Intent intent = new Intent(this, FloatingViewService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,"001")
                        .setSmallIcon(R.drawable.ic_android_circle)
                        .setContentTitle("My notification")
                        .setContentText("Hello World")
                        .setLargeIcon(bm)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setLights(Color.parseColor("PURPLE"),1000,1500)
                        .setVibrate(new long[]{1000,1000,1000,1000,1000})
                        .setSound(alarmsound)
                        .setPriority(NotificationCompat.PRIORITY_MAX);
                        finish();
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);


        assert mNotificationManager != null;
        mNotificationManager.notify(001,mBuilder.build());
    }
}
