package com.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * Created by _sn on 07.09.2017.
 */
@Slf4j
public class ParseLogLine implements MapFunction<String, NaxsiFlink.NaxsiTuple> {

    public NaxsiFlink.NaxsiTuple map(String s) throws Exception {
        log.info("---------------- PARSER ----------");
        log.info(s);

        return new NaxsiFlink.NaxsiTuple(s.contains("fmt")?"fmt":"exlog", s);
    }
}
