package com.brf.bq.customsoundelightview.utils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.brf.bq.customsoundelightview.widget.VerticalSeekBar;

/**
 * Created by Administrator on 2016/11/16.
 */

public class SoundCotroler {
//    增大声音
    public static void soundUp(Context context, float yDetla, int totleSize, VerticalSeekBar seekBar){
        AudioManager audioManager= ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量
        int currentSound=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
        seekBar.setprogress(currentSound);

        int add = (int)((yDetla / (float) totleSize) * maxVolume);
//        yDetla为负值
        int change = currentSound - add;
        seekBar.setprogress(change);
        Log.e("Sound", "soundUp: 改变值"+change);
    }

    public static void soundDown(Context context, float yDetla, int totleSize, VerticalSeekBar seekBar) {
        AudioManager audioManager= ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        int currentSound=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量

        int add = (int)((yDetla / (float) totleSize) * maxVolume);
//        yDetla为正值
        int change= Math.max(currentSound-add,0);
        seekBar.setprogress(change);
    }

    public static void setSound(Context context, int change) {
        AudioManager audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, change, AudioManager.FLAG_PLAY_SOUND);
    }
}
