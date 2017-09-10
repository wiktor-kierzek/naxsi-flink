package com.example;

import com.example.mappers.ExtractNaxsiMessage;
import com.example.mappers.ParseLogLine;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.operators.DataSink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by _sn on 07.09.2017.
 */
@Slf4j
public class NaxsiFlink {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);

        RMQConnectionConfig rabbitMqConfig = new RMQConnectionConfig.Builder()
            .setUri("amqp://flink:uGPxSmAWRuu5AtSx@51.255.199.51:5672").build();

        Map<String, String> elasticConfig = new HashMap<>();
        elasticConfig.put("cluster.name", "elastic");
        elasticConfig.put("bulk.flush.max.actions", "1");

        List<InetSocketAddress> transportAddresses = new ArrayList<>();
        transportAddresses.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9300));

        SplitStream<ExtractNaxsiMessage.NaxsiTuple> input = env
            .addSource(new RMQSource<>(
                rabbitMqConfig,
                "naxsi",
                false,
                new SimpleStringSchema()
            ), TypeInformation.of(String.class)).setParallelism(1).returns(String.class)
            .map(new ExtractNaxsiMessage()).setParallelism(5)
            .split((OutputSelector<ExtractNaxsiMessage.NaxsiTuple>) value -> Lists.newArrayList(value.getLog()));

        DataStream<ParseLogLine.ParsedLogEntry> exlogStream =
            input.select("exlog")
            .map(new ParseLogLine());

        DataStream<ParseLogLine.ParsedLogEntry> fmtStream =
            input.select("fmt")
                .map(new ParseLogLine())
                .join(exlogStream)
                .where(new GetHashForTuple()).equalTo(new GetHashForTuple())
                .window(TumblingEventTimeWindows.of(Time.seconds(20)))
                .apply((JoinFunction<ParseLogLine.ParsedLogEntry, ParseLogLine.ParsedLogEntry, ParseLogLine.ParsedLogEntry>) (fmt, exlog) -> {
                    log.info("LOG");
                    log.info(fmt.toString());
                    log.info(exlog.toString());

                    // TODO: przepisz wartości z EXLOGa do findings w FMT

                    return fmt; // TODO: Do join
                });


        /** TODO:
         * - add Elastic sink
         *
         * https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/connectors/elasticsearch.html
         */

        env.execute("Naxsi example");
    }

    public static class GetHashForTuple implements KeySelector<ParseLogLine.ParsedLogEntry, String> {

        public String getKey(ParseLogLine.ParsedLogEntry tuple) throws Exception {
           // log.info(tuple.getMessage());
            //calculate hash
            return "fwfw";
        }

    }






}
