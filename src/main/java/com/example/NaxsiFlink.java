//nazwa paczki do zmiany panie "example" ;)
package com.example;

//Bardzo duzo nieuzywanych importow - do usuniecia! :)

import com.example.joins.SetContentInFindings;
import com.example.mappers.ExtractNaxsiMessage;
import com.example.mappers.ParseLogLine;
import com.example.mappers.ToJson;
import com.example.sinks.OpsGenieSink;
import com.example.sinks.OpsGenieTuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
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
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
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

    /*  1. metoda jest za duza, proponuje wydzielic male factory albo buildery
        2. zla praktyka jest na metodzie main robienie throws Exception:
            brak kontroli wyjatkow, niektore pewnie mozna failsafeowac i nie konczyc dzialania programu
        3. idealnie jakby w mainie bylo tylko proste ustawienie enva np.
            env = new StreamExecutionEnvironmentBuilder()
                .withCheckpoiting(1000m EXCACTLY_ONCE)
                .withSource(...)
                ...with fmtStream(na ktorego tez bym buildera wydzielil), itd.
                .build();
           i na koncu
           env.execute();

           Takie podejscie oszczedziloby sporo czasu podczas wgryzania sie w kod i na pewno odplaci sie przy rozwoju aplikacji

     */
    public static void main(String[] args) throws Exception {
        //zrobilbym buildera tj StreamExecutionEnvironmentBuilder, troche nizej wytlumaczone dlaczego
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //wartosc interval bym wyciagnal do propertiesow
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);

        /*magiczne stringi typu "amqp.uri" bym wyciagnal do enumow:
        public enum SettingKey {
            URI("amqp.uri"),
            QUEUE_NAME("amqp.queue.name"),
            itd....;

            private String value;

            AmqpSettings(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return this.value;
            }
        }
        zamiast Settings.get("amqp.uri") by bylo Settings.get(SettingKey.URI) - niweluje niebezpieczenstwo literowek i robi ORDNUNG!!! :D
        */
        RMQConnectionConfig rabbitMqConfig = new RMQConnectionConfig.Builder()
                .setUri(Settings.get("amqp.uri")).build();

        //tak samo z magic stringami od elastica:
        //zamiast elasticConfig.put("cluster.name, Settings.get("elastic.cluster.name")
        //->
        // elasticConfig.put(ElasticSettings.CLUSTER_NAME, Settings.get(SettingKey.ELASTIC_CLUSTER_NAME
        Map<String, String> elasticConfig = new HashMap<>();
        elasticConfig.put("cluster.name", Settings.get("elastic.cluster.name"));
        elasticConfig.put("bulk.flush.max.actions", "100");

        //wyciagnalbym to do prostego utila/serwisu
        List<InetSocketAddress> transportAddresses = new ArrayList<>();
        transportAddresses.add(new InetSocketAddress(InetAddress.getByName(
                Settings.get("elastic.host")),
                Integer.parseInt(Settings.get("elastic.port")
                )));

        //jw. to ma sporo logiki, wyciagnalbym do buildera, o ktorym mowilem na poczatku metody - bedzie latwiej testowac
        SplitStream<ExtractNaxsiMessage.NaxsiTuple> input = env
                .addSource(new RMQSource<>(
                        rabbitMqConfig,
                        Settings.get("amqp.queue.name"),
                        false,
                        new SimpleStringSchema()
                ), TypeInformation.of(String.class))
                .setParallelism(1) //dlaczego 1, source rabbita nie jest parallel?
                .returns(String.class)
                .map(new ExtractNaxsiMessage()).setParallelism(5) // to 5 bym wyciagnal do propertiesow, jak projekt bedzie rosl trudniej bedzie znajdowac takie magic numbery
                .split((OutputSelector<ExtractNaxsiMessage.NaxsiTuple>) value -> Lists.newArrayList(value.getLog()));

        DataStream<ParseLogLine.ParsedLogEntry> exlogStream =
                input.select("exlog") //magiczny string - wyciagnal bym do stalej
                        .map(new ParseLogLine())
                        .filter(t -> t != null);


        DataStream<ParseLogLine.ParsedLogEntry> fmtStream =
                input.select("fmt")
                        //tutaj mamy identyczny kod tylko dla innej wartosci outputName - wyciagnac do metody prywatnej
                        /*
                        private DataStream<ParseLogLine.ParsedLogEntry> getStreamForOutputName(SplitStream input, String name){
                            return input.select(name)
                                        .map(new ParseLogLine())
                                        .filter(t -> t != null);
                        }
                         */
                        .map(new ParseLogLine()) // czy trzeba tworzyc za kazdym razem nowa instancje ParseLogLine? Pattern jest thread safe, wiec ta klasa jesli nic nie przeoczylem rowniez
                        .filter(t -> t != null);

        //to cale inlineowe flatmap function do wyciagniecia do osobnej klasy
        fmtStream.flatMap((ParseLogLine.ParsedLogEntry fmt, Collector<OpsGenieTuple> out) -> {
            for (ParseLogLine.FMTLog.Finding finding : ((ParseLogLine.FMTLog) fmt).getFindings()) {
                out.collect(new OpsGenieTuple(fmt.getIp() + finding.getType().toString(), fmt.getIp(), finding.getType().toString()));
            }
        }).returns(OpsGenieTuple.class)
                .setParallelism(5) // magiczne 5 bym do propertiesow wyciagnal
                .keyBy("hash").reduce((t1, t2) -> t1) //znowu magic string -> wyciagnalbym do stalej
                .keyBy("hash").window(SlidingProcessingTimeWindows.of(Time.seconds(60), Time.seconds(30))) // te czasu imo powinny byc w propertiesach
                .sum("count")
                .filter(t -> t.count > 20) // tak samo jak ta 20 :)
                .addSink(new OpsGenieSink());

        fmtStream.coGroup(exlogStream)
                .where(new GetHashForTuple()).equalTo(new GetHashForTuple())
                .window(SlidingProcessingTimeWindows.of(Time.seconds(5), Time.seconds(1))) // jw. do propertiesow
                .apply(new SetContentInFindings()).map(new ToJson())
                .addSink(new ElasticsearchSink<>(elasticConfig, transportAddresses,
                        (ElasticsearchSinkFunction<String>) (element, ctx, indexer) ->
                                indexer.add(Requests.indexRequest()
                                        .index(Settings.get("elastic.index.name"))
                                        .type(Settings.get("elastic.index.type"))
                                        .source(element)
                                ))).setParallelism(2);

        env.execute("Naxsi");
    }

    //do wydzielenia do osobnej klasy
    public static class GetHashForTuple implements KeySelector<ParseLogLine.ParsedLogEntry, String> {
        public String getKey(ParseLogLine.ParsedLogEntry tuple) throws Exception {
            //nie wiem do czego ta md5ka bedzie uzywana ale md5 w 2017 wyglada dziwnie ;)
            return tuple == null ? "" : DigestUtils.md5Hex(tuple.getTimestamp() + tuple.getIp() + tuple.getRequest()
            );
        }
    }


}
