package com.wilddog.wilddogroom.Intercepter;


public class SamplerThread extends Thread {
    private ISamplerHandler mSamplerHandler;
    private float mSamplerPeriod;
    private volatile boolean mCanSampling = true;
    private final static float DEFAULT_PERIOD = 16.67f;

    public SamplerThread(ISamplerHandler handler, float samplerPeriod) {
        this.mSamplerHandler = handler;
        if (Float.compare(0f, samplerPeriod) == 0) {
            this.mSamplerPeriod = DEFAULT_PERIOD;
        } else {
            this.mSamplerPeriod = samplerPeriod;
        }
    }

    @Override
    public void run() {
        while (mCanSampling) {
            try {
                Thread.sleep((long) (mSamplerPeriod));
                if (mSamplerHandler != null) {
                    mSamplerHandler.doSamplerEvent();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopSampling() {
        mCanSampling = false;
    }

    public void startSampling() {
        start();
    }

    public boolean isSampling(){
        return isAlive();
    }


    public interface ISamplerHandler {
        void doSamplerEvent();
    }


    public void setSamplerHandler(ISamplerHandler mSamplerHandler) {
        this.mSamplerHandler = mSamplerHandler;
    }
}
