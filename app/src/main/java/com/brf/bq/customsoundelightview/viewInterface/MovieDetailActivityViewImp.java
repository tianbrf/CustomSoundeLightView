package com.brf.bq.customsoundelightview.viewInterface;

/**
 * Created by tian on 2016/12/13.
 */

public interface MovieDetailActivityViewImp {
    void initView();
    void setData();
    void setLister();
    void goBack(float xDelta);
    void goForward(float xDelta);
    void showOrHideControll(boolean isVisiblity, boolean show);
    void showOrHideSoundControll(boolean isVisiblity);
    void showOrHideLinghtControll(boolean isVisiblity);
}
