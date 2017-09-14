package com.example.sinks;

import lombok.Getter;

import java.io.Serializable;

public class OpsGenieTuple implements Serializable {
    @Getter
    public String hash, ip, type;
    public int count = 1;

    public OpsGenieTuple() {}

    public OpsGenieTuple(String hash, String ip, String type) {
        this.hash = hash;
        this.type = type;
        this.ip = ip;
    }
}