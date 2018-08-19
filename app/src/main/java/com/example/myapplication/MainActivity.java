package com.example.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;


import android.support.v13.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.myapplication.fragments.MyCamera;
import com.example.myapplication.fragments.MyGallery;
import com.example.myapplication.fragments.fav;
import com.example.myapplication.fragments.postmessages;
import com.example.myapplication.fragments.preferences;
import com.example.myapplication.fragments.topPost;
import com.example.myapplication.fragments.userPost;
import com.example.myapplication.fragments.writePost;
import com.example.myapplication.notification.NotificationReceiver;
import com.example.myapplication.notification.NotificationSystem;
import com.example.myapplication.util.FloatingViewService;
import com.example.myapplication.util.Pager;
import com.example.myapplication.util.WebAppInterface;

import java.util.Calendar;
@SuppressLint("SDCardPath")
public class MainActivity extends FragmentActivity implements TabLayout.OnTabSelectedListener,userPost.OnFragmentInteractionListener,topPost.OnFragmentInteractionListener,
writePost.OnFragmentInteractionListener,fav.OnFragmentInteractionListener,preferences.OnFragmentInteractionListener,postmessages.OnFragmentInteractionListener
,MyGallery.OnFragmentInteractionListener,MyCamera.OnFragmentInteractionListener{
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CHANNEL_ID = 001;
    private Activity activity;

    private TabLayout tabLayout;

    private FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    //This is our viewPager
    private ViewPager viewPager;

    //TabPage
    public Pager adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
            //initializeView();
        }

        isWriteStoragePermissionGranted();
        isReadStoragePermissionGranted();

        writePost writePost = (writePost) getSupportFragmentManager().findFragmentByTag("writepage");
        if(writePost !=null){
            writePost.setActivity(this);
        }else{
            try {
                writePost.setActivity(this);
            }catch (Exception e){

            }
        }



        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Toast.makeText(activity, ""+hour, Toast.LENGTH_LONG).show();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),broadcast);



        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        //Creating our pager adapter
        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);


        viewPager.arrowScroll(View.FOCUS_RIGHT);
        viewPager.arrowScroll(View.FOCUS_RIGHT);

        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);

        tabLayout.setupWithViewPager(viewPager);





    }






//    private void initializeView() {
//        findViewById(R.id.notify_me).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                //startService(new Intent(MainActivity.this , ExitButtonView.class));
//                startService(new Intent(MainActivity.this, FloatingViewService.class));
//                finish();
//            }
//        });
//    }


    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("write","Permission is granted2");
                return true;
            } else {

                Log.v("write","Permission is revoked2");
                requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("write","Permission is granted2");
            return true;
        }
    }

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("read","Permission is granted1");
                return true;
            } else {

                Log.v("read","Permission is revoked1");
                requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("read","Permission is granted1");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                //Log.d(TAG, "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                   // downloadPdfFile();
                }else{
                   // progress.dismiss();
                }
                break;

            case 3:
                //Log.d(TAG, "External storage1");
//                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
//                   // Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
//                    //resume tasks needing this permission
//                    //SharePdfFile();
//                }else{
//                    //progress.dismiss();
//                }
                break;
        }
    }

    public  Pager getAdapter() {
        return adapter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                //initializeView();
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

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

        //Toast.makeText(activity, webApp, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void writeFragmentInteractions(Uri uri) {

    }

    @Override
    public void writeFragmentInteraction(final WebAppInterface webAppInterface, final writePost writePost) {
        webAppInterface.messbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postmessages postmessages = new postmessages();
                MyCamera camera = new MyCamera();
                MyGallery gallery = new MyGallery();
                fragmentManager = getFragmentManager();


                Toast.makeText(activity,webAppInterface.getWho(), Toast.LENGTH_SHORT).show();


                if(fragmentManager != null){
                    fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.setCustomAnimations(R.animator.slide_up,R.animator.slide_down);
                    fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            Toast.makeText(activity, "List", Toast.LENGTH_SHORT).show();

                        }
                    });

                    //fragmentTransaction.hide(writePost);



                    if(webAppInterface.getWho().equals("messages")) {

                        fragmentTransaction.add(Fragment.instantiate(activity,postmessages.class.getName()),"Messginf");

                        fragmentTransaction.show(postmessages);
                        //fragmentTransaction.hide(writePost);
                        fragmentTransaction.replace(R.id.writepost,Fragment.instantiate(activity,postmessages.class.getName()));
                        fragmentTransaction.addToBackStack("writepage");
                    }
                    else if(webAppInterface.getWho().equals("camera")){
                        fragmentTransaction.add(Fragment.instantiate(activity,MyCamera.class.getName()),"Messginf");
                        //fragmentTransaction.hide(writePost);
                        fragmentTransaction.show(camera);
                        fragmentTransaction.replace(R.id.writepost,Fragment.instantiate(activity,MyCamera.class.getName()));
                        fragmentTransaction.addToBackStack("writepage");
                    }
                    else if(webAppInterface.getWho().equals("gallery")){
                        fragmentTransaction.add(Fragment.instantiate(activity,MyGallery.class.getName()),"Messginf");
                        //fragmentTransaction.hide(writePost);
                        fragmentTransaction.show(gallery);
                        fragmentTransaction.replace(R.id.writepost,Fragment.instantiate(activity,MyGallery.class.getName()));
                        fragmentTransaction.addToBackStack("writepage");
                    }


                    fragmentTransaction.commit();
                }


            }
        });
    }

    @Override
    public void postFragmentInteraction(Uri uri) {

    }

    @Override
    public void postFragmentInteraction(WebAppInterface webAppInterface, postmessages postmessage) {
        
        webAppInterface.messbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Hereaasdfasdfajsdfafliasfliogasdfliosdfal;ojksdf", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
