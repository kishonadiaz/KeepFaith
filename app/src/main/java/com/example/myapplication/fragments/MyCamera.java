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

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG =  "dialog";


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String TAG = "Camera";

    private static final int STATE_PREVIEW = 0;

    private static final int STATE_WAITING_LOCK = 1;


    private static final int STATE_WAITING_PRECAPTURE = 2;

    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    private static final int STATE_PICTURE_TAKEN = 4;

    private static final int MAX_PREVIEW_WIDTH = 1920;

    private static final int MAX_PREVIEW_HEIGHT = 1080;


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            =new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private String mCameraId;

    private AutoFitTextureView mTextureView;

    private  CameraCaptureSession mCaptureSession;

    private CameraDevice mCameraDevice;

    private Size mPreviewSize;

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            FragmentActivity activity = getActivity();
            if(null != activity){
                activity.finish();
            }

        }
    };

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private ImageReader mImageReader;

    private File mFile;

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            mBackgroundHandler.post(new ImageSaver(imageReader.acquireNextImage(),mFile));
        }
    };

    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CaptureRequest mPreviewRequest;


    private int mState = STATE_PREVIEW;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private  boolean mFlashSupported;

    private int mSenorOrientation;

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }
    };





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


    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight,
                                          Size aspectRatio){
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();

        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for(Size option : choices){
            if(option.getHeight() == option.getWidth() * h/w){
                if(option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight){
                    bigEnough.add(option);
                }else
                {
                    notBigEnough.add(option);
                }
            }
        }

        if(bigEnough.size() > 0){
            return Collections.min(bigEnough, new CompareSizesByArea());
        }else if(notBigEnough.size() > 0){
            return Collections.max(notBigEnough, new CompareSizesByArea());
        }else{
            Log.e(TAG,"Couldn't find any suitable preview size");
            return choices[0];
        }
    }



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



    private OnFragmentInteractionListener mListener;


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
        //view.findViewById(R.id.info).setOnClickListener(this);
        mTextureView = view.findViewById(R.id.texture);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");

    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if(mTextureView.isAvailable()){
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        }else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }


    }


    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }


    private void requestCameraPermission(){
        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }else{
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                ErrorDialog.newInstance("here")
                        .show(getChildFragmentManager(),FRAGMENT_DIALOG);
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    }

    private void setUpCameraOutputs(int width, int height){
        FragmentActivity activity = getActivity();
        CameraManager manager =(CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);

        try{
            for(String cameraId: manager.getCameraIdList()){
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(map == null){
                    continue;
                }

                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener,mBackgroundHandler);

                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();

                mSenorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation){
                    case Surface.ROTATION_0:{}
                    case Surface.ROTATION_180:{
                        if(mSenorOrientation == 90 || mSenorOrientation == 270){
                            swappedDimensions = true;
                        }
                        break;
                    }
                    case Surface.ROTATION_90:{}
                    case Surface.ROTATION_270:{
                        if(mSenorOrientation == 0 || mSenorOrientation == 180){
                            swappedDimensions = true;
                        }
                        break;
                    }
                    default:
                        Log.e(TAG, "Displey rotation is invalid:" + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if(swappedDimensions){
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.x;
                    maxPreviewHeight = displaySize.y;
                }

                if(maxPreviewWidth > MAX_PREVIEW_WIDTH){
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if(maxPreviewHeight > MAX_PREVIEW_HEIGHT){
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight,maxPreviewWidth,
                        maxPreviewHeight, largest);

                int orientation = getResources().getConfiguration().orientation;
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getHeight());
                }else{
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(),mPreviewSize.getWidth());
                }

                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;


            }
        }catch (CameraAccessException e){
            e.printStackTrace();
        }catch (NullPointerException e){

            ErrorDialog.newInstance("error")
                    .show(getChildFragmentManager(),FRAGMENT_DIALOG);
        }
    }


    private void openCamera(int width, int height) {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width,height);
        configureTransform(width,height);
        FragmentActivity activity = getActivity();
        CameraManager manager =(CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);

        try{
            if(!mCameraOpenCloseLock.tryAcquire(2500,TimeUnit.MILLISECONDS)){
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }

    }

    private void closeCamera(){
        try {
            mCameraOpenCloseLock.acquire();
            if(null != mCaptureSession){
                mCaptureSession.close();
                mCaptureSession = null;
            }

            if(null != mCameraDevice){
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if(null != mImageReader){
                mImageReader.close();
                mImageReader = null;
            }
        }catch (InterruptedException e){
            throw  new RuntimeException("Interrupted while trying to lock camera closing.",e);
        }finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight){
        FragmentActivity activity = getActivity();
        if(null == mTextureView || null ==  mPreviewSize || null == activity){
            return;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0,0,viewWidth,viewHeight);
        RectF bufferRect = new RectF(0,0,mPreviewSize.getHeight(),mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if(Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation){
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
              viewHeight / mPreviewSize.getHeight(),
              viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 *(rotation -2), centerX, centerY);
        }else if(Surface.ROTATION_180 == rotation){
            matrix.postRotate(180,centerX,centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void takePicture(){
        lockFocus();
    }


    private void lockFocus(){
        try{
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(),
                    mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private  void runPrecaptureSequence(){
        try{
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(),mCaptureCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void captureStillPicture(){
        try{
            final  FragmentActivity activity = getActivity();
            if(null ==  activity || null == mCameraDevice){
                return;
            }

            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    //super.onCaptureCompleted(session, request, result);

                    showToast("Save: "+mFile);
                    Log.d(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation){
        return (ORIENTATIONS.get(rotation)+mSenorOrientation + 270)% 360;
    }

    private void unlockFocus(){
        try{
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(),
                    mCaptureCallback,
                    mBackgroundHandler);
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest,
                    mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
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


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.picture:{
                takePicture();
                break;
            }
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder){
        if(mFlashSupported){
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
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


    public static class ConfirmationDialog extends DialogFragment {

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
    }
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
