package com.example.myapplication.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;
import com.example.myapplication.R;
import com.example.myapplication.util.GalleryAdapter;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyGallery.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyGallery#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyGallery extends Fragment {

    /*
    *
    * https://www.androstock.com/tutorials/create-a-photo-gallery-app-in-android-android-studio.html
    *
    */



    private OnFragmentInteractionListener mListener;
    private GridView gridView;
    private ArrayList<Bitmap> bitmapArrayList;

    public MyGallery() {
        // Required empty public constructor
    }

    public static MyGallery newInstance(String param1, String param2) {
        MyGallery fragment = new MyGallery();
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
        View view = inflater.inflate(R.layout.fragment_my_gallery, container, false);

            this.gridView = view.findViewById(R.id.galleryGridView);
            this.bitmapArrayList = new ArrayList<>();

            String dir = Environment.getExternalStorageDirectory()+ File.separator+"sample";
            File mFile = new File(dir);
            try {
                for(File  file: mFile.listFiles()){
                    if(file.isFile()) {
                        this.bitmapArrayList.add(urlImageToBitmap(file));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.gridView.setAdapter(new GalleryAdapter(getActivity(),this.bitmapArrayList));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);







    }

    private Bitmap urlImageToBitmap(File file) throws Exception{
        Bitmap result = null;
        URL uri = file.toURL();
        if(uri != null){
            result = BitmapFactory.decodeStream(uri.openConnection().getInputStream());
        }
        Toast.makeText(getContext(),file.getPath().toString(), Toast.LENGTH_SHORT).show();


        return result;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        void onFragmentInteraction(Uri uri);
    }
}
