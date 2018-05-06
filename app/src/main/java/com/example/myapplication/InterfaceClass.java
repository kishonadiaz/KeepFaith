package com.example.myapplication;



import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;

public class InterfaceClass {
    public static ArrayList<Integer> a;
    public interface myInterfaceListener{

        public void onOver(int a);

        public int onDone();

    }

    private myInterfaceListener listener;

    public InterfaceClass(){
        this.listener = null;
    }

    public void setObjectListener(myInterfaceListener listener){
        this.listener = listener;
    }


}
