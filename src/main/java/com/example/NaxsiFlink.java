package com.example;

import com.example.data.Elasticsearch;
import com.example.data.RabbitMQ;
import com.example.data.tuple.FMTLog;
import com.example.data.tuple.NaxsiTuple;
import com.example.data.tuple.ParsedLogEntry;
import com.example.joins.SetContentInFindings;
import com.example.mappers.ExtractNaxsiMessage;
import com.example.mappers.ParseLogLine;
import com.example.mappers.ToJson;
import com.example.sinks.OpsGenieSink;
import com.example.sinks.OpsGenieTuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch5.ElasticsearchSink;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.elasticsearch.client.Requests;

/**
 * Created by wiktor.kierzek on 07.09.2017.
 */
@Slf4j
public class NaxsiFlink {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);

        SplitStream<NaxsiTuple> input = env
            .addSource(new RMQSource<>(
                    RabbitMQ.getConfig(),
                Settings.get(Settings.Key.RABBITMQ_NAXSI_QUEUE),
                false,
                new SimpleStringSchema()
            ), TypeInformation.of(String.class)).setParallelism(1).returns(String.class)
            .map(new ExtractNaxsiMessage()).setParallelism(5)
            .split((OutputSelector<NaxsiTuple>) value -> Lists.newArrayList(value.getLog()));

        DataStream<ParsedLogEntry> exlogStream =
            input.select("exlog")
                .map(new ParseLogLine())
                .filter(t -> t!=null);


        DataStream<ParsedLogEntry> fmtStream =
            input.select("fmt")
                .map(new ParseLogLine())
                .filter(t -> t!=null);

        fmtStream.coGroup(exlogStream)
            .where(new GetHashForTuple()).equalTo(new GetHashForTuple())
            .window(SlidingProcessingTimeWindows.of(Time.seconds(5), Time.seconds(1)))
            .apply(new SetContentInFindings()).map(new ToJson())
            .addSink(new ElasticsearchSink<>(
                    Elasticsearch.getConfig(),
                    Elasticsearch.getHosts(),
                    (ElasticsearchSinkFunction<String>) (element, ctx, indexer) ->
                        indexer.add(Requests.indexRequest()
                            .index(Settings.get(Settings.Key.ELASTIC_NAXSI_INDEX_NAME))
                            .type(Settings.get(Settings.Key.ELASTIC_NAXSI_INDEX_TYPE))
                            .source(element)
                    ))
            ).setParallelism(2);

        fmtStream.flatMap((ParsedLogEntry fmt, Collector<OpsGenieTuple> out) -> {
            for (FMTLog.Finding finding : ((FMTLog) fmt).getFindings()) {
                out.collect(new OpsGenieTuple(fmt.getIp() + finding.getType().toString(), fmt.getIp(), finding.getType().toString()));
            }
        }).returns(OpsGenieTuple.class).setParallelism(5)
                .keyBy("hash").reduce((t1, t2) -> t1)
                .keyBy("hash").window(SlidingProcessingTimeWindows.of(Time.seconds(60), Time.seconds(30)))
                .sum("count")
                .filter(t -> t.count > 20)
                .addSink(new OpsGenieSink());

        env.execute("Naxsi");
    }

    public static class GetHashForTuple implements KeySelector<ParsedLogEntry, String> {
        public String getKey(ParsedLogEntry tuple) throws Exception {
            return tuple == null ? "" : DigestUtils.md5Hex(tuple.getTimestamp() + tuple.getIp() + tuple.getRequest()
            );
        }
    }








}
