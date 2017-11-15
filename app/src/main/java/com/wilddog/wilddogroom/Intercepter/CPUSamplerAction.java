package com.wilddog.wilddogroom.Intercepter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class CPUSamplerAction extends BaseSamplerAction {
    private int mPid;
    private long mLastCpuTime;//当前手机的CPU总时间
    private long mLastAppTime;//当前app的CPU耗时
    private RandomAccessFile procStatFile;
    private RandomAccessFile appStatFile;
    private IMonitorRecord mMonitorRecord;

    private static final int RATE_LIMIT = 50;//CPU占比阈值
    private static final String TAG = "CPUSampler";
    private static final String CPU_RATE = "cpu";
    private Handler handler=new Handler(Looper.getMainLooper());

    public CPUSamplerAction(IMonitorRecord addMonitorRecord) {
        super(addMonitorRecord);
        this.mMonitorRecord = addMonitorRecord;
    }

    @Override
    public void doSamplerAction() {
        if (mPid == 0) {
            mPid = android.os.Process.myPid();
        }

        try {
            if (procStatFile == null || appStatFile == null) {
                procStatFile = new RandomAccessFile("/proc/stat", "r");
                appStatFile = new RandomAccessFile("/proc/" + mPid + "/stat", "r");
            }

            //文件开头
            procStatFile.seek(0);
            appStatFile.seek(0);

            String proStatSrc = procStatFile.readLine();
            String appStatSrc = appStatFile.readLine();

            String[] proStats = null;
            String[] appStats = null;
            if (proStatSrc != null || appStatSrc != null) {
                proStats = proStatSrc.trim().split(" ");
                appStats = appStatSrc.trim().split(" ");
            }

            long cpuTime = 0L;
            long appTime = 0L;
            if (proStats != null && proStats.length >= 9) {
                for (int i = 2; i <= 8; i++) {
                    cpuTime += Long.valueOf(proStats[i]);
                }
            }

            if (appStats != null && appStats.length >= 15) {
                appTime = Long.valueOf(appStats[13]) + Long.valueOf(appStats[14]);
            }

            if (mLastCpuTime == 0 || mLastAppTime == 0) {
                mLastCpuTime = cpuTime;
                mLastAppTime = appTime;
                return;
            }

            double cpuRate = (double) (appTime - mLastAppTime) / (double) (cpuTime - mLastCpuTime);
            mLastCpuTime = cpuTime;
            mLastAppTime = appTime;

            if (mMonitorRecord != null) {
                final int cpuRateInt = (int) (cpuRate * 100);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMonitorRecord.addOneRecord(CPU_RATE, cpuRateInt + "%", cpuRateInt < RATE_LIMIT ? true : false);
                    }
                });

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "init randomfile failed");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "read file failed");
        }
    }
}
