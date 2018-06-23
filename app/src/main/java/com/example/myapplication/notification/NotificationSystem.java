package com.example.myapplication.notification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.v4.app.NotificationCompat;


import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class NotificationSystem extends NotificationCompat.Builder{

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Context mContext;
    private static  String CHANNEL_ID;

    @SuppressLint("ServiceCast")
    public NotificationSystem(@NonNull Context context, @NonNull String channelId) {
        super(context, channelId);
        builder = this;
        mContext = context;
        CHANNEL_ID = channelId;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public void sendNotification(Class cl,int sr, int r){

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),r);



        Intent intent = new Intent(mContext,cl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getService(mContext, 0, intent, 0);

        Uri alarmsound =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        setSmallIcon(sr);
        setContentTitle("My notification");
        setContentText("Hello World");
        setLargeIcon(bm);
        setContentIntent(pendingIntent).build();
        setAutoCancel(true);
        setLights(Color.parseColor("PURPLE"),1000,1500);
        setVibrate(new long[]{1000,1000,1000,1000,1000});
        setSound(alarmsound);
        setPriority(NotificationCompat.PRIORITY_MAX);


        //NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(mContext);


//        assert mNotificationManager != null;
//        mNotificationManager.notify(001,this.build());
        //Notification_Channel();
        assert notificationManager != null;
        notificationManager.notify(001,this.build());

    }

    private void Notification_Channel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "KeepFaith",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }
    }


}
