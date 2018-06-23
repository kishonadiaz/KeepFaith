package com.example.myapplication.notification;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.myapplication.util.FloatingViewService;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "com.example.notification.channelId";


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context,FloatingViewService.class);



        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Toast.makeText(context, "here", Toast.LENGTH_SHORT).show();

        NotificationSystem notifacationSystem = new NotificationSystem(context,CHANNEL_ID);
//
//
        notifacationSystem.sendNotification(FloatingViewService.class, R.drawable.ic_launcher_round,R.drawable.ic_launcher_round);
//        notifacationSystem.setContentIntent(pendingIntent).build();
    }

}
