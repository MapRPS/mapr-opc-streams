package com.mapr.opc.streams;

import java.util.List;

/**
 * Created by chufe on 10/06/16.
 */
public class OpcConfig {
    private String kafkaBatchSize;
    private String kafkaProducerType;
    private String hostname;
    private String domain;
    private String user;
    private String password;
    private String progId;
    private String clsId;
    private Integer fetchIntervalInMs;
    private Integer threads;
    private boolean distinctTimeStamp;
    private boolean distinctValue;
    private String timeFormat;
    private String lineFormat;
    private List<OpcItem> items;

    public String getKafkaBatchSize() {
        return kafkaBatchSize;
    }

    public void setKafkaBatchSize(String kafkaBatchSize) {
        this.kafkaBatchSize = kafkaBatchSize;
    }

    public String getKafkaProducerType() {
        return kafkaProducerType;
    }

    public void setKafkaProducerType(String kafkaProducerType) {
        this.kafkaProducerType = kafkaProducerType;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getLineFormat() {
        return lineFormat;
    }

    public void setLineFormat(String lineFormat) {
        this.lineFormat = lineFormat;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProgId() {
        return progId;
    }

    public void setProgId(String progId) {
        this.progId = progId;
    }

    public String getClsId() {
        return clsId;
    }

    public void setClsId(String clsId) {
        this.clsId = clsId;
    }

    public Integer getFetchIntervalInMs() {
        return fetchIntervalInMs;
    }

    public void setFetchIntervalInMs(Integer fetchIntervalInMs) {
        this.fetchIntervalInMs = fetchIntervalInMs;
    }

    public boolean isDistinctTimeStamp() {
        return distinctTimeStamp;
    }

    public void setDistinctTimeStamp(boolean distinctTimeStamp) {
        this.distinctTimeStamp = distinctTimeStamp;
    }

    public boolean isDistinctValue() {
        return distinctValue;
    }

    public void setDistinctValue(boolean distinctValue) {
        this.distinctValue = distinctValue;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public List<OpcItem> getItems() {
        return items;
    }

    public void setItems(List<OpcItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OpcConfig{" +
                "hostname='" + hostname + '\'' +
                ", domain='" + domain + '\'' +
                ", user='" + user + '\'' +
                ", password=*****" +
                ", progId='" + progId + '\'' +
                ", clsId='" + clsId + '\'' +
                ", fetchIntervalInMs=" + fetchIntervalInMs +
                ", distinctTimeStamp=" + distinctTimeStamp +
                ", distinctValue=" + distinctValue +
                ", timeFormat='" + timeFormat + '\'' +
                ", lineFormat='" + lineFormat + '\'' +
                ", items=" + items +
                '}';
    }
}
