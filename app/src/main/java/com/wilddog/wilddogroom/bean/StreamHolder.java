package com.wilddog.wilddogroom.bean;


import com.wilddog.video.base.core.Stream;

/**
 * Created by fly on 17-9-13.
 */

public class StreamHolder {
    private Long timeStamp;
    private Stream stream;
    private boolean isLocal =false;

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StreamHolder(boolean isLocal, long timeStamp, Stream stream) {
        this.isLocal = isLocal;
        this.timeStamp = timeStamp;
        this.stream = stream;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }
}
