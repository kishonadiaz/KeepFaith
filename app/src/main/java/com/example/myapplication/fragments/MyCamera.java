package com.example.myapplication.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.util.AutoFitTextureView;
import com.example.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MyCamera extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback{















    private void showToast(final String text){
        final Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity,text,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




    private OnFragmentInteractionListener mListener;

    public MyCamera() {
        // Required empty public constructor
    }
    public static MyCamera newInstance(){
        return new MyCamera();
    }


    public static MyCamera newInstance(String param1, String param2) {
        MyCamera fragment = new MyCamera();
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
        return inflater.inflate(R.layout.fragment_my_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.picture).setOnClickListener(this);
        view.findViewById(R.id.info).setOnClickListener(this);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();



    }


    @Override
    public void onPause() {

        super.onPause();
    }


    private void requestCameraPermission(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);



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










    @Override
    public void onClick(View view) {

    }



    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }


   /* public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Here")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }*/
}





/*
class Preview extends ViewGroup implements SurfaceHolder.Callback{
    private final String TAG ="Preview";

    SurfaceView mSurfaceview;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportPreviewSizes;
    Camera camera;
    boolean mSurfaceCreated = false;


    public Preview(Context context) {
        super(context);

        mSurfaceview = new SurfaceView(context);
        addView(mSurfaceview);

        mHolder = mSurfaceview.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void setCamera(Camera camera){
        this.camera = camera;

        if(this.camera != null) {
            mSupportPreviewSizes = this.camera.getParameters().getSupportedPreviewSizes();
            if (mSurfaceCreated) requestLayout();
        }
    }

    public void switchCamera(Camera camera){
        setCamera(camera);

        try {
            this.camera.setPreviewDisplay(mHolder);
        }catch (IOException e){

        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        final int width = resolveSize(getSuggestedMinimumWidth(),widthMeasureSpec);
//
//        final int height = resolveSize(getSuggestedMinimumHeight(),heightMeasureSpec);
//
//        setMeasuredDimension(width,height);
//
//        if(mSupportPreviewSizes != null){
//            mPreviewSize = getOptimalPreviewSize(mSupportPreviewSizes,width,height);
//        }
//
//        if(this.camera != null){
//            Camera.Parameters parameters = this.camera.getParameters();
//            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//
//            this.camera.setParameters(parameters);
//        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            this.camera.setPreviewDisplay(surfaceHolder);
            //this.camera.startPreview();
        }catch(IOException e){

        }
        if(mPreviewSize == null) requestLayout();
        mSurfaceCreated = true;


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        if(this.camera != null){
            this.camera.stopPreview();
        }

    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h){
        final double ASPECT_TOLERANCE =  0.1;
        double targetRatio = (double) w/ h;
        if(sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for(Size size: sizes){
            double ratio = (double)size.width / size.height;
            if(Math.abs(ratio - targetRatio)> ASPECT_TOLERANCE)
                continue;
            if(Math.abs(size.height - targetHeight) < minDiff){
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if(optimalSize == null){
            minDiff = Double.MAX_VALUE;
            for(Size size : sizes){
                if(Math.abs(size.height - targetHeight) < minDiff){
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {

        Camera.Parameters parameters = this.camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        this.camera.setParameters(parameters);
        this.camera.startPreview();




//        if(mHolder.getSurface() == null){
//            return;
//
//        }
//
//        try{
//            this.camera.stopPreview();
//        }catch (Exception e){
//
//        }
//
//
//        try{
//            this.camera.setPreviewDisplay(mHolder);
//            this.camera.startPreview();
//        }catch (Exception e){
//
//        }

    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b) {
        if(getChildCount() > 0){
            final View child = getChildAt(0);

            final int width = r -l;
            final int height = b- t;

            int previewWidth = width;
            int previewHeight = height;
            if(mPreviewSize != null){
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            if(width * previewHeight >height * previewWidth){
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth)/ 2, 0, (width + scaledChildWidth)/ 2, height);
            }else{
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0,(height - scaledChildHeight) / 2, width,(height + scaledChildHeight)/ 2);
            }
        }
    }
}
*/
