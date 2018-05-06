package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;

public class GifMovieView extends View {


    private Movie mMovie;
    private InputStream mStream;

    public GifMovieView(Context context, InputStream stream) {
        super(context);

        mStream = stream;
        mMovie = Movie.decodeStream(mStream);

    }

    private  long mMovieStar;
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        final long now = SystemClock.uptimeMillis();

        if(mMovieStar == 0){
            mMovieStar = now;
        }

        final int reltime = (int)((now - mMovieStar) % mMovie.duration());

        mMovie.setTime(reltime);
        mMovie.draw(canvas,10,10);
        this.invalidate();

    }
}
