package com.mapr.opc.streams;

/**
 * Created by chufe on 09/06/16.
 */
public class OpcItem {
    private String itemId;
    private String topic;
    private Boolean distinctTimeStamp;
    private Boolean distinctValue;
    private String timeFormat;
    private String lineFormat;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getTopic() {
        return topic;
    }

    public Boolean getDistinctTimeStamp() {
        return distinctTimeStamp;
    }

    public void setDistinctTimeStamp(Boolean distinctTimeStamp) {
        this.distinctTimeStamp = distinctTimeStamp;
    }

    public Boolean getDistinctValue() {
        return distinctValue;
    }

    public void setDistinctValue(Boolean distinctValue) {
        this.distinctValue = distinctValue;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getLineFormat() {
        return lineFormat;
    }

    public void setLineFormat(String lineFormat) {
        this.lineFormat = lineFormat;
    }
}
