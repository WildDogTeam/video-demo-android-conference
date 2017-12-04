package com.wilddog.wilddogroom.Intercepter;


public interface IMonitorRecord {
    /**
     *
     * @param tvName      名称
     * @param tvNum       数值
     * @param isGoodValue 该性能参数的好坏
     */
    void addOneRecord(String tvName, String tvNum, boolean isGoodValue);

}
