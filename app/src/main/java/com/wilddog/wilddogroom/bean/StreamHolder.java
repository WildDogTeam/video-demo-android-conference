package com.wilddog.wilddogroom.bean;


import com.wilddog.video.base.core.Stream;

/**
 * Created by fly on 17-9-13.
 */

public class StreamHolder {
    private Long timeStamp;
    private Stream stream;

    public StreamHolder(long timeStamp, Stream stream) {
        this.timeStamp = timeStamp;
        this.stream = stream;
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
