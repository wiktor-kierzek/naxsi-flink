package com.example.mappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.flink.api.common.functions.MapFunction;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Created by _sn on 10.09.2017.
 */
public class ExtractNaxsiMessage implements MapFunction<String,ExtractNaxsiMessage.NaxsiTuple> {

    public NaxsiTuple map(String s) throws Exception {
        int beginIndex = s.indexOf("message\":\"")+10;
        int endIndex = s.lastIndexOf("\",\"type\":");

        String message = s.substring(beginIndex, endIndex < 0 ? s.length() : endIndex);
        String type = message.contains("NAXSI_FMT")?"fmt":message.contains("NAXSI_EXLOG")?"exlog":"other";

        return new NaxsiTuple(type, message);
    }

    @AllArgsConstructor @Getter
    public static class NaxsiTuple implements Serializable {
        private String log;
        private String message;
    }
}

