package com.mapr.opc.streams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chufe on 10/06/16.
 */
public class OpcConfig {
    private Map<String, String> kafka;
    private String hostname;
    private String domain;
    private String user;
    private String password;
    private String progId;
    private String clsId;
    private Integer fetchIntervalInMs;
    private Integer threads;
    private Boolean distinctTimeStamp = Boolean.FALSE;
    private Boolean distinctValue = Boolean.FALSE;
    private String timeFormat;
    private String lineFormat;
    private List<OpcItem> items;

    public Map<String, String> getKafka() {
        return kafka;
    }

    public void setKafka(Map<String, String> kafka) {
        this.kafka = kafka;
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

    public List<OpcItem> getItems() {
        return items;
    }

    public void setItems(List<OpcItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OpcConfig{" +
                "kafka=" + kafka +
                ", hostname='" + hostname + '\'' +
                ", domain='" + domain + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", progId='" + progId + '\'' +
                ", clsId='" + clsId + '\'' +
                ", fetchIntervalInMs=" + fetchIntervalInMs +
                ", threads=" + threads +
                ", distinctTimeStamp=" + distinctTimeStamp +
                ", distinctValue=" + distinctValue +
                ", timeFormat='" + timeFormat + '\'' +
                ", lineFormat='" + lineFormat + '\'' +
                ", items=" + items +
                '}';
    }
}
