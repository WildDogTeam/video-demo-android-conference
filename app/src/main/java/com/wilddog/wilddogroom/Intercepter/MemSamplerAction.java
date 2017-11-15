package com.wilddog.wilddogroom.Intercepter;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MemSamplerAction extends BaseSamplerAction {
    private IMonitorRecord mMonitorRecord;
    private final static String USED_MEM = "Memory";
    private final static String MAX_MEM = "max";
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public MemSamplerAction(IMonitorRecord addMonitorRecord) {
        super(addMonitorRecord);
        this.mMonitorRecord = addMonitorRecord;
    }

    @Override
    public void doSamplerAction() {
        if (mMonitorRecord == null) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMonitorRecord.addOneRecord(USED_MEM, getUseSize() + "M", true);
            }
        });
    }

    /**
     * 获取当前应用可用总内存 单位M
     *
     * @return
     */
    private long getTotalMemory() {
        return Runtime.getRuntime().maxMemory() >> 20;
    }

    /**
     * 获取当前应用所占内存 单位M
     *
     * @return
     */
    private long getUseSize() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) >> 20;
    }

    /**
     * 获取当前手机总内存大小
     *
     * @return
     */
    public String getPhoneTotalRAM() {

        RandomAccessFile reader = null;
        String load;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
            }
            reader.close();
            totRam = Double.parseDouble(value);
            lastValue = twoDecimalForm.format(totRam).concat(" KB");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lastValue;
    }
}
