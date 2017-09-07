package com.example;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by _sn on 07.09.2017.
 */
public class NaxsiFlink {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);

        final RMQConnectionConfig rabbitMqConfig = new RMQConnectionConfig.Builder()
            .setUri("amqp://flink:uGPxSmAWRuu5AtSx@51.255.199.51:5672").build();

        Map<String, String> elasticConfig = new HashMap<>();
        elasticConfig.put("cluster.name", "my-cluster-name");
        elasticConfig.put("bulk.flush.max.actions", "1");

        List<InetSocketAddress> transportAddresses = new ArrayList<>();
        transportAddresses.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9300));

        final SplitStream<Tuple> input = env
            .addSource(new RMQSource<String>(
                rabbitMqConfig,
                "naxsi",
                true,
                new SimpleStringSchema()
            )).setParallelism(1)
            .map(new ParseLogLine()).setParallelism(2)
            .split(new OutputSelector<Tuple>() {
                public Iterable<String> select(Tuple value) {
                    return Lists.newArrayList(value.getField(0).toString());
                }
            });

        final DataStream<Tuple> ex = input.select("exlog");

        final DataStream<Tuple> fmt = input.select("fmt")
            .join(ex)
            .where(new GetHashForTuple()).equalTo(new GetHashForTuple())
            .window(TumblingEventTimeWindows.of(Time.seconds(20)))
            .apply(new JoinFunction<Tuple, Tuple, Tuple>() {
                public Tuple join(Tuple o, Tuple o2) throws Exception {
                    return null;
                }
            });


        /** TODO:
         * - add Elastic sink
         *
         * https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/connectors/elasticsearch.html
         */


    }

    private static class GetHashForTuple implements KeySelector<Tuple, String>  {

        public String getKey(Tuple tuple) throws Exception {
            //calculate hash
            return null;
        }
    }
}
