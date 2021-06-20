package cn.iwgang.countdownview;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;




public class AlreadyTime {
    private static final int MSG = 1;
    private long mMillisInPast;
    private long mCountDownInterval;
    private OnAlreadyTimeListener onAlreadyTimeListener;
    Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == MSG) {
                mMillisInPast = mMillisInPast + mCountDownInterval;
                long listenerBefore = SystemClock.elapsedRealtime();
                onAlreadyTimeListener.onAlreadyTime(mMillisInPast);
                long passListener = SystemClock.elapsedRealtime() - listenerBefore;
                handler.sendMessageDelayed(handler.obtainMessage(MSG), mCountDownInterval - passListener);


            }
        }
    };

    interface OnAlreadyTimeListener {
        public void onAlreadyTime(Long mills);
    }

    public void setOnAlreadyTimeListener(OnAlreadyTimeListener onAlreadyTimeListener) {
        this.onAlreadyTimeListener = onAlreadyTimeListener;
    }
    private AlreadyTime(){};
    public AlreadyTime(long millisInPast, long countDownInterval) {
        //目标时间减去现在的时间。是负数
        this.mMillisInPast = millisInPast;
        this.mCountDownInterval = countDownInterval;

    }

    public void start() {
        //  Message message =Message.obtain();
        handler.sendMessage(handler.obtainMessage(MSG));
    }
    public void  stop(){
        handler.removeMessages(MSG);
    }
}
