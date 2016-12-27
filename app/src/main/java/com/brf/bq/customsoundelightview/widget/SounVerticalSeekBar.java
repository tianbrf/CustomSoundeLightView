package com.brf.bq.customsoundelightview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by Administrator on 2016/12/19.
 */

public class SounVerticalSeekBar extends SeekBar {
    private final int mMaxVolume;
    private AudioManager mAudioManager;

    public SounVerticalSeekBar(Context context) {
        this(context,null);
    }

    public SounVerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(),0);
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int i=0;
                i=getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(i);
                Log.i("Progress",getProgress()+"");
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    public void onchangeSize(int i){
        setProgress(i);
        Log.i("Progress",getProgress()+"");
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}
