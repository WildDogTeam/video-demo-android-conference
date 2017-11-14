package com.wilddog.wilddogroom.Intercepter;

import java.util.ArrayList;
import java.util.List;


public class SamplerIntercepter implements SamplerThread.ISamplerHandler {
    private List<ISamplerAction> mActionList = new ArrayList<>();

    public SamplerIntercepter(IMonitorRecord addMonitorRecord) {
        mActionList.add(new MemSamplerAction(addMonitorRecord));
        mActionList.add(new CPUSamplerAction(addMonitorRecord));
    }

    @Override
    public void doSamplerEvent() {
        if (mActionList == null) {
            return;
        }

        for (ISamplerAction action : mActionList) {
            action.doSamplerAction();
        }
    }

}
