package com.example;

import com.example.joins.SetContentInFindings;
import com.example.mappers.ExtractNaxsiMessage;
import com.example.mappers.ParseLogLine;
import com.example.mappers.ToJson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch5.ElasticsearchSink;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.elasticsearch.client.Requests;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiktor.kierzek on 07.09.2017.
 */
@Slf4j
public class NaxsiFlink {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);

        RMQConnectionConfig rabbitMqConfig = new RMQConnectionConfig.Builder()
            .setUri(Settings.get("amqp.uri")).build();

        Map<String, String> elasticConfig = new HashMap<>();
        elasticConfig.put("cluster.name", Settings.get("elastic.cluster.name"));
        elasticConfig.put("bulk.flush.max.actions", "100");

        List<InetSocketAddress> transportAddresses = new ArrayList<>();
        transportAddresses.add(new InetSocketAddress(InetAddress.getByName(
            Settings.get("elastic.host")),
            Integer.parseInt(Settings.get("elastic.port")
            )));

        SplitStream<ExtractNaxsiMessage.NaxsiTuple> input = env
            .addSource(new RMQSource<>(
                rabbitMqConfig,
                Settings.get("amqp.queue.name"),
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
                .map(new ParseLogLine());


        DataStreamSink<String> joinedStreams =
            fmtStream.coGroup(exlogStream)
                .where(new GetHashForTuple()).equalTo(new GetHashForTuple())
                .window(SlidingProcessingTimeWindows.of(Time.seconds(30), Time.seconds(5)))
                .apply(new SetContentInFindings())
            .map(new ToJson())
            .addSink(new ElasticsearchSink<>(elasticConfig, transportAddresses,
                    (ElasticsearchSinkFunction<String>) (element, ctx, indexer) ->
                            indexer.add(Requests.indexRequest()
                                            .index(Settings.get("elastic.index.name"))
                                            .type(Settings.get("elastic.index.type"))
                                            .source(element)
                            ))).setParallelism(2);

        env.execute("Naxsi");
    }

    public static class GetHashForTuple implements KeySelector<ParseLogLine.ParsedLogEntry, String> {
        public String getKey(ParseLogLine.ParsedLogEntry tuple) throws Exception {
            return tuple == null ? "" : DigestUtils.md5Hex(tuple.getTimestamp() + tuple.getIp() + tuple.getRequest()
            );
        }
    }






}
