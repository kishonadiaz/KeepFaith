package com.example.myapplication.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

public class postmessages extends Fragment {

    private static WebView webView;
    private static View v;
    private boolean javascriptInterfaceBroken =false;
    static WebAppInterface webAppInterface;
    Button messbtn;
    FragmentActivity activity;
    public com.example.myapplication.fragments.writePost writePost;

    private OnFragmentInteractionListener mListener;

    public postmessages() {
        // Required empty public constructor
    }

    public static postmessages newInstance(String param1, String param2) {
        postmessages fragment = new postmessages();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_postmessages, container, false);




        webView = v.findViewById(R.id.postMessageWeb);
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
        mListener.postFragmentInteraction(webAppInterface,this);


        if(!javascriptInterfaceBroken){
            webView.addJavascriptInterface(webAppInterface,"android");
        }



        webView.loadUrl("file:///android_asset/www/pages/messagearea.html");

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.setWebViewClient(new WebViewClient());

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void postFragmentInteraction(Uri uri);
        void postFragmentInteraction(WebAppInterface webAppInterface, postmessages postmessage);
    }
}
