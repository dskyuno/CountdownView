package cn.iwgang.countdownview;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 使用android.os.CountDownTimer的源码
 * 1. 对回调onTick做了细小调整，已解决最后1秒不会倒计时到0，要等待2秒才回调onFinish
 * 2. 添加了一些自定义方法
 * Created by iWgang on 15/10/18.
 * https://github.com/iwgang/CountdownView
 */
public abstract class CustomCountDownTimer {
    private static final int MSG = 1;
    private final long mMillisInFuture;
    private final long mCountdownInterval;
    private long mStopTimeInFuture;
    private long mPauseTimeInFuture;
    private boolean isStop = false;
    private boolean isPause = false;
    private boolean isCountDown = true;
    private AlreadyTime mAlreadyTime;

    /**
     * @param millisInFuture    总倒计时时间
     * @param countDownInterval 倒计时间隔时间
     */
    public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        // 解决秒数有时会一开始就减去了2秒问题（如10秒总数的，刚开始就8999，然后没有不会显示9秒，直接到8秒）
        if (countDownInterval > 1000) millisInFuture += 15;
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    private synchronized CustomCountDownTimer start(long millisInFuture) {
        isStop = false;
        if (millisInFuture <= 0) {
            onFinish();
            return this;
        }
        if (isCountDown) {
            mStopTimeInFuture = SystemClock.elapsedRealtime() + millisInFuture;
            mHandler.sendMessage(mHandler.obtainMessage(MSG));
        } else {
            mAlreadyTime = new AlreadyTime(millisInFuture, 1000);
            mAlreadyTime.setOnAlreadyTimeListener(new AlreadyTime.OnAlreadyTimeListener() {
                @Override
                public void onAlreadyTime(Long mills) {
                    //  Log.e("正计时", "" + mills / 1000 + "秒");
                    onTick(mills,true);
                }
            });
            mAlreadyTime.start();

        }


        return this;
    }

    /**
     * 开始倒计时
     */
    public synchronized final void start() {
        start(mMillisInFuture);
    }

    /**
     * 开始倒计时
     */
    public synchronized final void start(Boolean isCountDown) {
        this.isCountDown = isCountDown;
        start(mMillisInFuture);

    }

    /**
     * 停止倒计时
     */
    public synchronized final void stop() {
        isStop = true;
        mHandler.removeMessages(MSG);
        if(mAlreadyTime!=null)mAlreadyTime.stop();
    }

    /**
     * 暂时倒计时
     * 调用{@link #restart()}方法重新开始
     */
    public synchronized final void pause() {
        if (isStop) return;

        isPause = true;
        mPauseTimeInFuture = mStopTimeInFuture - SystemClock.elapsedRealtime();
        mHandler.removeMessages(MSG);
    }

    /**
     * 重新开始
     */
    public synchronized final void restart() {
        if (isStop || !isPause) return;

        isPause = false;
        start(mPauseTimeInFuture);
    }

    /**
     * 倒计时间隔回调
     *
     * @param millisUntilFinished 剩余毫秒数
     */
    public abstract void onTick(long millisUntilFinished,boolean isAlready);

    /**
     * 倒计时结束回调
     */
    public abstract void onFinish();


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CustomCountDownTimer.this) {
                if (isStop || isPause) {
                    return;
                }

                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();
                if (millisLeft <= 0) {
                    mAlreadyTime = new AlreadyTime(0, 1000);
                    mAlreadyTime.setOnAlreadyTimeListener(new AlreadyTime.OnAlreadyTimeListener() {
                        @Override
                        public void onAlreadyTime(Long mills) {
                            onTick(mills,true);
                        }
                    });
                    mAlreadyTime.start();
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft,false);
                    long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();
                    while (delay < 0) delay += mCountdownInterval;
                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
