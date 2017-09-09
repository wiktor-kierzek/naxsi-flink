package com.example;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.shaded.com.google.common.collect.Lists;
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
        run();
    }

    public static boolean run() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);

        RMQConnectionConfig rabbitMqConfig = new RMQConnectionConfig.Builder()
                .setUri("amqp://flink:uGPxSmAWRuu5AtSx@51.255.199.51:5672").build();

        Map<String, String> elasticConfig = new HashMap<>();
        elasticConfig.put("cluster.name", "my-cluster-name");
        elasticConfig.put("bulk.flush.max.actions", "1");

        List<InetSocketAddress> transportAddresses = new ArrayList<>();
        transportAddresses.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9300));

        TypeInformation<Tuple2<String, String>> tpinf = new TypeHint<Tuple2<String, String>>(){}.getTypeInfo();

        DataStreamSink<NaxsiTuple> input = env
                .addSource(new RMQSource<>(
                        rabbitMqConfig,
                        "naxsi",
                        false,
                        new SimpleStringSchema()
                ), TypeInformation.of(String.class)).setParallelism(1).returns(String.class)
                .map(new ParseLogLine())//.setParallelism(2).returns(new TupleTypeInfo(Tuple2.class))
        .print();
               // .split((OutputSelector<NaxsiTuple>) value -> Lists.newArrayList(value.log));

        //DataStream<NaxsiTuple> ex = input.select("exlog");
/*
        DataStream<NaxsiTuple> fmt = input.select("fmt")
                .join(ex)
                .where(new GetHashForTuple()).equalTo(new GetHashForTuple())
                .window(TumblingEventTimeWindows.of(Time.seconds(20)))
                .apply((JoinFunction<NaxsiTuple, NaxsiTuple, NaxsiTuple>) (o, o2) -> {
                    log.info("LOG");
                    log.info(o.message);
                    log.info(o2.message);
                    return new NaxsiTuple(o.log, o2.message); // TODO: Do join
                });
*/

        /** TODO:
         * - add Elastic sink
         *
         * https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/connectors/elasticsearch.html
         */

        env.execute("Naxsi example");

        return true;
    }

    public static class GetHashForTuple implements KeySelector<NaxsiTuple, String> {

        public String getKey(NaxsiTuple tuple) throws Exception {
            log.info(tuple.message);
            //calculate hash
            return tuple.message;
        }

    }

    @AllArgsConstructor
    public static class NaxsiTuple implements Serializable {
        private String log;
        private String message;
    }
}
