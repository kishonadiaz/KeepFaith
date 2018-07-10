package com.example.myapplication.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.Toast;


public class AutoFitTextureView extends TextureView {


    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    Context context;

    public AutoFitTextureView(Context context) {
        super(context);

        this.context = context;
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context =context;
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setAspectRatio(int width, int height){
        if(width < 0 || height < 0){
            throw new IllegalArgumentException("Size cannot be negative");
        }

        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    public int getmRatioWidth(){
        return mRatioWidth;
    }

    public int getmRatioHeight(){
        return mRatioHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(0 == mRatioWidth || 0 == mRatioHeight){
            setMeasuredDimension(width,height);
        }else{



            if(width < height * mRatioHeight / mRatioWidth){
                //full screen
                /*setMeasuredDimension((height * mRatioHeight / mRatioWidth)/2,height );*/
                setMeasuredDimension(width,(width * mRatioHeight / mRatioWidth) );
            }else{
                //full screen
                /*setMeasuredDimension(width , (width *mRatioHeight / mRatioWidth)/2);*/
                setMeasuredDimension((height *mRatioHeight / mRatioWidth), height);
            }
        }

    }
}
