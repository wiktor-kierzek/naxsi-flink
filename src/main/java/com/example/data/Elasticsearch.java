package com.example.data;

import com.example.Settings;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.com.google.common.collect.ImmutableMap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wiktor.kierzek on 02.10.2017.
 */
@Slf4j
public class Elasticsearch {

    public static Map<String, String> getConfig() {
        return ImmutableMap.of(
                "cluster.name", Settings.get(Settings.Key.ELASTIC_CLUSTER_NAME),
                "bulk.flush.max.actions", Settings.get(Settings.Key.ELASTIC_MAX_BULK_SIZE)
        );
    }

    public static List<InetSocketAddress> getHosts() {
        int port = Integer.parseInt(Settings.get(Settings.Key.ELASTIC_PORT));

        return Stream.of(Settings.get(Settings.Key.ELASTIC_HOSTS).split(","))
                .map(host -> new InetSocketAddress(host, port))
                .collect(Collectors.toList());

    }
}
