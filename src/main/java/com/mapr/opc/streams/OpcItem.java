package com.mapr.opc.streams;

/**
 * Created by chufe on 09/06/16.
 */
public class OpcItem {
    private String itemId;
    private String topic;

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

    @Override
    public String toString() {
        return "OpcItem{" +
                "itemId='" + itemId + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}
