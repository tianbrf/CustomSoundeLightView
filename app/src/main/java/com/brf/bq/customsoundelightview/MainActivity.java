package com.brf.bq.customsoundelightview;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.brf.bq.customsoundelightview.utils.DisplayUtil;
import com.brf.bq.customsoundelightview.utils.LightCotroler;
import com.brf.bq.customsoundelightview.utils.SoundCotroler;
import com.brf.bq.customsoundelightview.utils.TimeUtils;
import com.brf.bq.customsoundelightview.viewInterface.MovieDetailActivityViewImp;
import com.brf.bq.customsoundelightview.widget.CustomViedoView;
import com.brf.bq.customsoundelightview.widget.VerticalSeekBar;

public class MainActivity extends AppCompatActivity implements Handler.Callback, MovieDetailActivityViewImp, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private String path = "http://qiniu.vmovier.vmoviercdn.com/584a7a927d157.mp4";
    private String request_url = "http://app.vmoiver.com/50649?qingapp=app_new";
    private CustomViedoView videoView;
    private WebView webView;
    private VerticalSeekBar vSeekbar_linght;
    private VerticalSeekBar vSeekBar_sound;
    private SeekBar seekBar;
    private CheckBox play_iv;
    private TextView currentTime;
    private TextView totalTime;
    private CheckBox full;

    //更新进度
    private Handler handler;
    private static final int UPDATE_PROGRESS = 100;//what标记 用来更新播放进度
    public static final int UPDATE_PROGRESS_SOUND = 200;//更新声音进度
    public static final int UPDATE_PROGRESS_LIGHT = 300;//更新亮度进度

    public static final int CLOSE_CONTROLLER = 1;//用来自动关闭控制器
    public static final int CLOSE_CONTROLLER_SOUND = 2;//用来自动关闭声音控制器
    public static final int CLOSE_CONTROLLER_LIGHT = 3;//用来自动关闭亮度控制器

    private float startX;//按下的x坐标
    private float startY;
    private float lastX;//x的最后的坐标
    private float lastY;
    private int threhould = 10;//滑动的临界值

    private int screenW;//屏幕宽高
    private int screenH;

    private boolean isLandScape = true;
    private boolean isPlay = false;
    private LinearLayout controller;
    private LinearLayout sound_controller;
    private LinearLayout light_controller;
    private int oldHeight;
    private ImageView back_iv;
    private boolean isAdd;

    private int mSoundSbSize;
    private AudioManager mAudioManager;
    private int mMaxSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //获取屏幕宽高
        screenH = getResources().getDisplayMetrics().heightPixels;
        screenW = getResources().getDisplayMetrics().widthPixels;
    }

    public void initView() {
        handler = new Handler(this);
        back_iv = ((ImageView) findViewById(R.id.back));
        videoView = ((CustomViedoView) findViewById(R.id.videoView_movieDetail));
        webView = ((WebView) findViewById(R.id.webView_movieDetail));
        controller = ((LinearLayout) findViewById(R.id.controller));
        sound_controller = ((LinearLayout) findViewById(R.id.sound_controller));
        light_controller = (LinearLayout) findViewById(R.id.light_controller);
        vSeekbar_linght = ((VerticalSeekBar) findViewById(R.id.vertical_Seekbar_light));
        vSeekBar_sound = ((VerticalSeekBar) findViewById(R.id.vertical_Seekbar_sound));
        seekBar = ((SeekBar) findViewById(R.id.seekBar));
        play_iv = ((CheckBox) findViewById(R.id.play));
        currentTime = ((TextView) findViewById(R.id.my_video_controller_current));
        totalTime = ((TextView) findViewById(R.id.my_video_controller_total));
        full = ((CheckBox) findViewById(R.id.my_video_controller_fullScreen));

        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        int currentSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
        mMaxSound = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量
        vSeekBar_sound.setMax(mMaxSound);
        vSeekBar_sound.setProgress(currentSound);

        mSoundSbSize = DisplayUtil.dp2px(this, 80);

        setData();
        setLister();
    }

    public void setData() {
        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);

        webView.loadUrl(request_url);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });

    }

    @Override
    public void setLister() {
        setVideoViewListener();
        seekBar.setOnSeekBarChangeListener(this);
        vSeekbar_linght.setOnSeekBarChangeListener(this);
        vSeekBar_sound.setOnSeekBarChangeListener(this);
        play_iv.setOnCheckedChangeListener(this);
        full.setOnCheckedChangeListener(this);
    }
    private void setVideoViewListener() {
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //TODO 手指按下 播放和播放进度控制器显示  但声音和亮度的控制器隐藏
                        //记录手指按下的位置
                        startX = x;
                        startY = y;
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE://移动
                        if (isLandScape) {
                            float xDelta = x - lastX;
                            float yDelta = y - lastY;
                            if (Math.abs(xDelta) > Math.abs(yDelta)) {
                                showOrHideControll(true, false);
                                if (xDelta > 0) {//快进
                                    goForward(xDelta);
                                } else if (xDelta < 0) {//倒退
                                    goBack(xDelta);
                                }
                            } else if (Math.abs(yDelta) > threhould) {//大于设定的临界值
                                if (x < screenW / 2) {//改变音量
                                    //TODO 改变音量 将音量控制器显示出来 其他控制器隐藏
                                    showOrHideControll(false, false);
                                    showOrHideLinghtControll(false);
                                    showOrHideSoundControll(true);
                                    Message message_sound = Message.obtain();
                                    message_sound.what = UPDATE_PROGRESS_SOUND;
                                    message_sound.arg1 = (int) yDelta;
                                    if (yDelta > 0) {//音量减小
                                        //TODO 将手势移动的位置，给vertical_Seekbar_sound设置
                                        message_sound.obj = !isAdd;
                                    } else {//音量增加
                                        message_sound.obj = isAdd;
                                    }
                                    handler.sendMessage(message_sound);
                                } else {//改变亮度
                                    //TODO 改变亮度 将亮度控制器显示出来 其他控制器隐藏
                                    showOrHideControll(false, false);
                                    showOrHideLinghtControll(true);
                                    showOrHideSoundControll(false);
                                    Message message_light = Message.obtain();
                                    message_light.what = UPDATE_PROGRESS_LIGHT;
                                    message_light.arg1 = (int) yDelta;
                                    if (yDelta > 0) {//降低亮度
                                        LightCotroler.lightDown(MainActivity.this, yDelta, screenW, vSeekbar_linght);
                                        message_light.obj = !isAdd;
                                    } else {//增加亮度
                                        LightCotroler.LiaghtUp(MainActivity.this, yDelta, screenW, vSeekbar_linght);
                                        message_light.obj = isAdd;
                                    }
                                    handler.sendMessage(message_light);
                                }
                            }
                        }
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        //单击
                        if (Math.abs(x - startX) < threhould && Math.abs(y - startY) < threhould) {
                            //控制器 线性布局 显示隐藏
                            showOrHideLinghtControll(false);
                            showOrHideSoundControll(false);
                            //================
                            //暂时没有起到隐藏的作用
                            if (vSeekBar_sound.getVisibility()==View.VISIBLE)
                            handler.sendEmptyMessageDelayed(CLOSE_CONTROLLER_SOUND,2000);
                            if (vSeekbar_linght.getVisibility()==View.VISIBLE)
                            handler.sendEmptyMessageDelayed(CLOSE_CONTROLLER_LIGHT,2000);
                            //==================
                            changeState(controller, play_iv);
                        }
                        break;
                }
                return true;//true 代表自身处理  false代表系统处理
            }
        });
    }

    private void changeState(View ...views){
        for (int i = 0, size = views.length; i < size; i++) {
            View view = views[i];
            boolean isController = view == controller;
            if(view.getVisibility() == View.VISIBLE) {
                if(isController) {
                    showOrHideControll(false, true);
                } else {
                    view.setVisibility(View.INVISIBLE);
                }
            } else {
                if(isController) {
                    showOrHideControll(true, true);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void goBack(float xDelta) {
        int currentPos = videoView.getCurrentPosition();
        int total = videoView.getDuration();
        int add = ((int) (total * 0.3 * xDelta / screenH));
        int change = Math.min(currentPos + add, total);
        videoView.seekTo(change);
        handler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    @Override
    public void goForward(float xDelta) {
        int currentPos = videoView.getCurrentPosition();
        int total = videoView.getDuration();
        int add = ((int) (total * 0.3 * xDelta / screenH));
        int change = Math.max(currentPos + add, 0);
        videoView.seekTo(change);
        handler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    @Override
    public void showOrHideControll(boolean isVisibility, boolean show) {
        if (!isVisibility) {
            if (controller.getVisibility() != View.INVISIBLE) {
                controller.setVisibility(View.INVISIBLE);
                showOrHideLinghtControll(false);
                showOrHideSoundControll(false);
                if(show) {
                    Animation outAnima = AnimationUtils.loadAnimation(MainActivity.this, R.anim.out_controller);
                    controller.startAnimation(outAnima);
                }
            }
        } else {
            if (controller.getVisibility() != View.VISIBLE) {
                controller.setVisibility(View.VISIBLE);
                if(show) {
                    Animation inAnima = AnimationUtils.loadAnimation(MainActivity.this, R.anim.in_controller);
                    controller.startAnimation(inAnima);
                }
            }
        }
    }

    @Override
    public void showOrHideSoundControll(boolean isVisiblity) {
        if (isVisiblity) {
            if (sound_controller.getVisibility() != View.VISIBLE)
                sound_controller.setVisibility(View.VISIBLE);
        } else {
            if (sound_controller.getVisibility() != View.INVISIBLE)
                sound_controller.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showOrHideLinghtControll(boolean isVisiblity) {
        if (isVisiblity) {
            if (light_controller.getVisibility() != View.VISIBLE)
                light_controller.setVisibility(View.VISIBLE);
        } else {
            if (light_controller.getVisibility() != View.INVISIBLE)
                light_controller.setVisibility(View.INVISIBLE);
        }
    }

    //TODO SeekBar 的进度控制
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekBar:
                if (fromUser) {
                    videoView.seekTo(progress);
                    currentTime.setText(TimeUtils.parseTime(progress));
                }
                break;
            case R.id.vertical_Seekbar_sound:
                //TODO 将vertical_Seekbar_sound的进度值给系统声音
                SoundCotroler.setSound(this, progress);
                break;
            case R.id.vertical_Seekbar_light:
                //TODO 将vertical_Seekbar_sound的进度值给系统声音
                if (fromUser) {
                    LightCotroler.setlight(this, progress, vSeekbar_linght);
                }
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekBar:
                videoView.pause();
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekBar:
                videoView.start();
                break;
        }
    }

    //TODO 控制开始播放、暂停、全屏、竖屏
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.play://开始暂停
                if (b) {
                    videoView.start();//开始播放
                    handler.sendEmptyMessage(UPDATE_PROGRESS);//发送消息 更新进度
                    int duration = videoView.getDuration();//获取总的事件
                    seekBar.setMax(duration);//设置最大
                    totalTime.setText(TimeUtils.parseTime(duration));
                    play_iv.setVisibility(View.INVISIBLE);
                } else {
                    videoView.pause();
                    //将更新进度的移除
                    handler.removeMessages(UPDATE_PROGRESS);
                    play_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.my_video_controller_fullScreen://横屏竖屏
                //添加全屏的标记
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if (b) {
                    //记录原高度
                    oldHeight = videoView.getHeight();
                    //设置方向
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    //设置高度全屏
                    ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                    layoutParams.height = layoutParams.MATCH_PARENT;
                    videoView.setLayoutParams(layoutParams);
                } else {
                    //移除全屏的标记
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    //设置方向
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    //设置高度全屏
                    ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                    layoutParams.height = oldHeight;
                    videoView.setLayoutParams(layoutParams);
                }
                break;
        }
    }

    //TODO 使用Handler 更新各种进度及其他操作
    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case UPDATE_PROGRESS://更新
                //获取当前进度
                int currentPosition = videoView.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                currentTime.setText(TimeUtils.parseTime(currentPosition));
                handler.sendEmptyMessage(UPDATE_PROGRESS);
                break;
            case UPDATE_PROGRESS_SOUND:
                int yDelta_sound = message.arg1;
                boolean add = (boolean) message.obj;
                if(add) {
                    SoundCotroler.soundUp(MainActivity.this, yDelta_sound, mSoundSbSize, vSeekBar_sound);
                } else {
                    SoundCotroler.soundDown(MainActivity.this, yDelta_sound, mSoundSbSize, vSeekBar_sound);
                }
//                updateSound(yDelta_sound,flag);
                break;
            case UPDATE_PROGRESS_LIGHT:
                int yDelta_light = message.arg1;
                boolean flag_light = (boolean) message.obj;
//                updateLight(yDelta_light,flag_light);
                break;
            case CLOSE_CONTROLLER_SOUND:
                showOrHideSoundControll(false);
                break;
            case CLOSE_CONTROLLER_LIGHT:
                showOrHideLinghtControll(false);
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                handler.removeMessages(CLOSE_CONTROLLER_SOUND);
                showOrHideSoundControll(true);
                int currentSound1 = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
                int up = currentSound1 + (int)((float)mMaxSound / 7f);
                int change1 = Math.min(up, mMaxSound);
                vSeekBar_sound.setprogress(change1);
                handler.sendEmptyMessageDelayed(CLOSE_CONTROLLER_SOUND, 2000);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                handler.removeMessages(CLOSE_CONTROLLER_SOUND);
                showOrHideSoundControll(true);
                int currentSound2 = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
                int down = currentSound2 - (int)((float)mMaxSound / 7f);
                int change2 = Math.max(down, 0);
                vSeekBar_sound.setprogress(change2);
                handler.sendEmptyMessageDelayed(CLOSE_CONTROLLER_SOUND, 2000);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateSound(int yDelta_sound, boolean isAdd) {
        AudioManager audioManager = ((AudioManager) this.getSystemService(Context.AUDIO_SERVICE));
        int currentSound = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量
        int add = ((int) (2 * maxVolume * yDelta_sound / screenH));
        int change = -1;
        if (isAdd) {
// yDetla为负值
            change = Math.min(currentSound - add, maxVolume);
        } else {
            //        yDetla为正值
            change = Math.max(currentSound - add, 0);
        }
        vSeekBar_sound.setprogress(change);

    }

    private void updateLight(int yDelta_light, boolean isAdd) {
        //获取当前的亮度
        int currentLight = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        //最大255
        int add = ((int) (255 * 2 * yDelta_light / screenH));
        int change = -1;
        if (isAdd) {
            change = Math.min(currentLight - add, 255);
        } else {
            change = Math.max(currentLight - add, 0);
        }
        seekBar.setProgress(change);
    }
}
