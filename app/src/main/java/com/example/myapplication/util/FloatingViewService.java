package com.example.myapplication.util;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.IBinder;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.notification.NotificationReceiver;

import java.util.Calendar;

/**
 * Created by simplecast on 2/4/2018.
 * Code has example code from androidhive.
 */

public class FloatingViewService extends Service {
    private WindowManager mWindowManager;
    private View mFloatingView;
    ActivityManager activityManager;
    MotionEvent motionEvent;
    WindowManager.LayoutParams params;
    int favcounter = 0;
    boolean isfav = false;
    boolean islike = false;

    public FloatingViewService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i= 0; i < 300; i++){
                    Toast.makeText(getApplicationContext(), "d", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
        Toast.makeText(this, "afaff", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();




        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //Add the view to the window.
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 90;
        params.y = 100;


        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout linearLayout = mFloatingView.findViewById(R.id.container);


        linearLayout.setGravity(Gravity.CENTER);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView text = new TextView(getBaseContext());

        text.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        text.setText("This world is a beautiful world that GOD Has Created");
        text.setBackgroundResource(R.drawable.border);
        text.setLayoutParams(layoutParams);

        WebView webView = new WebView(getBaseContext());
        webView.loadUrl("file:///android_asset/www/index.html");

        webView.getSettings().setJavaScriptEnabled(true);





        ViewGroup.LayoutParams ss = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        webView.setLayoutParams(ss);
        String htmlData = "";
        htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>" + htmlData;

        String html = "<!doctype html><html><head><style>body{background:darkgray}</style></head><body><img src='ic_rotating_earth.gif' style='width:100%;  ' /></body></html>";


        webView.loadDataWithBaseURL("file:///android_res/drawable/",html,"text/html","utf-8",null);

        linearLayout.addView(webView);



        mWindowManager.addView(mFloatingView, params);






        //The root element of the collapsed view layout
        final View collapsedView = mFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        final View expandedView = mFloatingView.findViewById(R.id.expanded_container);

        final View rootrootcontainer = mFloatingView.findViewById(R.id.outerrootcontainer);


        //Set the close button
        final ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);


        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window
                stopSelf();

            }
        });

        //Set the view while floating view is expanded.
        //Set the play button.

        final ImageView favButton = (ImageView) mFloatingView.findViewById(R.id.favicon);
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isfav){
                    favButton.setImageResource(R.drawable.fav_highlighted);
                    isfav = true;
                }else{
                    favButton.setImageResource(R.drawable.fav);
                    isfav = false;
                }
                favcounter++;
            }
        });

        final ImageView likeButton = (ImageView) mFloatingView.findViewById(R.id.likeicon);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!islike){
                    likeButton.setImageResource(R.drawable.like_highlighted);
                    islike = true;
                }else{
                    likeButton.setImageResource(R.drawable.like_unhighlighted);
                    islike = false;
                }

            }
        });




        //Set the close button
        ImageView closeButton = (ImageView) mFloatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);*/
                collapsedView.setVisibility(View.GONE);
                expandedView.setVisibility(View.GONE);
                stopSelf();
            }
        });

        //Open the application on thi button click
        ImageView openButton = (ImageView) mFloatingView.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the application  click.
                Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //close the service and remove view from the view hierarchy
                stopSelf();
            }
        });

        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);

                                /*RelativeLayout.LayoutParams rr = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                                rr.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                                rr.width = RelativeLayout.LayoutParams.MATCH_PARENT;*/
                                //FrameLayout.LayoutParams ff = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                                //ff.width = FrameLayout.LayoutParams.MATCH_PARENT;


                                //expandedView.setLayoutParams(rr);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),broadcast);
        //startService(new Intent(FloatingViewService.this, FloatingViewService.class));

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //expandedView.setBackgroundColor(Color.TRANSPARENT);
                Toast.makeText(FloatingViewService.this, "heress", Toast.LENGTH_SHORT).show();
            }
        },300);
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }





    @Override
    public void onDestroy() {
        super.onDestroy();
     if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }
}
