package com.example;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;

/**
 * Created by _sn on 07.09.2017.
 */
public class ParseLogLine implements MapFunction<String, Tuple> {

    public Tuple map(String s) throws Exception {
        return null;
    }
}
