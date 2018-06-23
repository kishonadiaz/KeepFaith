package com.example.myapplication.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.util.WebAppInterface;

public class writePost extends Fragment {


    private  WebView webView;
    private  View v;
    private boolean javascriptInterfaceBroken =false;
    private OnFragmentInteractionListener mListener;
    static WebAppInterface webAppInterface;
    Button messbtn;
    FragmentActivity activity;
    public writePost writePost;

    public writePost() {
        // Required empty public constructor
    }


    public static writePost newInstance(String param1, String param2) {
        writePost fragment = new writePost();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        //activity = context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        writePost = this;
        if (getArguments() != null) {

        }


    }

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }


    public FragmentActivity getActivities() {
        return activity;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = this.getActivity();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_write_post, container, false);




        webView = v.findViewById(R.id.writewebview);
        webAppInterface = new WebAppInterface(v.getContext());

        messbtn = new Button(v.getContext());


        //webView.loadUrl("https://www.google.com");



        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);




        try{
            if("2.3".equals(Build.VERSION.RELEASE)){
                javascriptInterfaceBroken = true;
            }
        }catch (Exception e){

        }
        mListener.writeFragmentInteraction(webAppInterface,this);


        if(!javascriptInterfaceBroken){
            webView.addJavascriptInterface(webAppInterface,"android");
        }



        webView.loadUrl("file:///android_asset/www/pages/post.html");

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.setWebViewClient(new WebViewClient());


        return v;
    }




    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.writeFragmentInteractions(uri);
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static WebAppInterface getWebAppInterface() {
        return webAppInterface;
    }



    public interface OnFragmentInteractionListener {


        // TODO: Update argument type and name
        void writeFragmentInteractions(Uri uri);



        void  writeFragmentInteraction(WebAppInterface webAppInterface,writePost writePost);
    }


}
