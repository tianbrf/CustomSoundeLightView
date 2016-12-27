package com.brf.bq.customsoundelightview.utils;

/**
 * Created by Administrator on 2016/11/16.
 */

public class TimeUtils {
    /**
     * 时间转换
     * @param position
     * @return
     */
    public static CharSequence parseTime(int position){
        return android.text.format.DateFormat.format("mm:ss",position);
    }
}
