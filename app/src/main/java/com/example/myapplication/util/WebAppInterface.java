package com.example.myapplication.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.Toast;

public class WebAppInterface {
    Context mContext;
    private String sendclicked;
    String who;
    String what;
    public Button messbtn;
    Button messagearea;

    public WebAppInterface(Context context){
        mContext = context;
        messbtn = new Button(mContext);

    }




    @JavascriptInterface
    public void clicked(String who){
        this.who = who;
        messbtn.performClick();
    }

    public String getWho() {
        return who;
    }

    @JavascriptInterface
    public void clickmessagearea() {messagearea.performClick();}


    @JavascriptInterface
    public void setClickedMessage(String who, String what){
        this.who = who;
        this.what = what;

    }

    @JavascriptInterface
    public void showToast(String toast){
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }


    public String[] getClickedMessage(){
        return new String[]{who, what};
    }

    public String getSomeString(){
        return "string";
    }

    public Button getMessbtn() {
        return messbtn;
    }

    public Button getMessagearea() {
        return messagearea;
    }
}
