package com.brf.bq.customsoundelightview.utils;

import android.content.Context;
import android.provider.Settings;

import com.brf.bq.customsoundelightview.widget.VerticalSeekBar;

/**
 * Created by Administrator on 2016/11/16.
 */

public class LightCotroler {
//    增大亮度
    public static void LiaghtUp(Context context, float yDetla, int screenH, VerticalSeekBar seekBar){
        //获取当前的亮度
        int currentLight = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        //最大255
        int add= ((int) (255 * 2 * yDetla / screenH));
        int change= Math.min(currentLight-add,255);
        seekBar.setMax(255);
        seekBar.setprogress(change);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,change);
    }
//降低亮度
    public static void lightDown(Context context, float yDetla, int screenH,VerticalSeekBar seekBar) {
        //获取当前的亮度

        int currentLight = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        //最大255
        int add= ((int) (255 * 2 * yDetla / screenH));
        int change= Math.max(currentLight-add,0);
        seekBar.setMax(255);
        seekBar.setprogress(change);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,change);
    }

    public static void setlight(Context context, int change, VerticalSeekBar seekBar) {
        seekBar.setprogress(change);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, change);
    }

}
