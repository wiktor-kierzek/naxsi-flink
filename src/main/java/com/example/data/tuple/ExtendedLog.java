package com.example.data.tuple;

import lombok.Getter;

/**
 * Created by wiktor.kierzek@curency-one.com on 02.10.2017.
 */
@Getter
public class ExtendedLog extends ParsedLogEntry {
    private String varName, content;

    public ExtendedLog(String ip, String timestamp, String server, String uri, String request, String varName, String content) {
        super(ip, server, uri, request, timestamp);
        this.varName = varName;
        this.content = content;
    }
}