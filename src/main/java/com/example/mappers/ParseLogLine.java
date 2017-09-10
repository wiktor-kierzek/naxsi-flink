package com.example.mappers;

import lombok.AllArgsConstructor;
import org.apache.flink.api.common.functions.MapFunction;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by _sn on 10.09.2017.
 */
public class ParseLogLine implements MapFunction<ExtractNaxsiMessage.NaxsiTuple,ParseLogLine.ParsedLogEntry> {

    private static Pattern IP_REGEX = Pattern.compile("ip=([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})&");
    private static Pattern SERVER_REGEX = Pattern.compile("server=([^&]*)(?=&)");
    private static Pattern URI_REGEX = Pattern.compile("uri=([^&]*)(?=&)");
    private static Pattern REQUEST_REGEX = Pattern.compile("request:\\s\"([^\",]*)(?=\",\\s)");

    private static Pattern CONTENT_EXLOG_REGEX = Pattern.compile("content=(.*),\\s?client");
    private static Pattern VARNAME_EXLOG_REGEX = Pattern.compile("var_name=([^&]*)(?=&)");


    public ParsedLogEntry map(ExtractNaxsiMessage.NaxsiTuple log) throws Exception {
        switch (log.getLog()) {
            case "fmt": return parseFMT(log.getMessage());
            case "exlog": return parseEX(log.getMessage());
        }
        return null;
    }

    /*
    2017/09/09 17:14:50 [error] 16048#0: *3045261 NAXSI_FMT: ip=77.65.91.38&server=naxsi.kierzek.pl&uri=/&total_processed=5&total_blocked=4&zone0=ARGS&id0=1000&var_name0=v&zone1=ARGS&id1=1007&var_name1=v&zone2=ARGS&id2=1015&var_name2=v, client: 77.65.91.38, server: naxsi.kierzek.pl, request: \"GET /?v=32%20UNION%20ALL%20SELECT%20NULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL%2CNULL--%20XwUM HTTP/1.1\", host: \"naxsi.kierzek.pl\"
     */
    private FMTLog parseFMT(String line) {

    }

    private ExtendedLog parseEX(String line) {
        return new ExtendedLog(
            getValue(IP_REGEX, line),
            getTimestamp(line),
            getValue(SERVER_REGEX, line),
            getValue(URI_REGEX, line),
            getValue(REQUEST_REGEX, line),
            getValue(VARNAME_EXLOG_REGEX, line),
            getValue(CONTENT_EXLOG_REGEX, line)
        );
    }

    private String getTimestamp(String line) {
        return line.substring(0, 19);
    }

    private String getValue(Pattern pattern, String line) {
        return pattern.matcher(line).group(1);
    }

    @AllArgsConstructor
    public abstract static class ParsedLogEntry implements Serializable {
        protected String ip, timestamp, server, uri, request;
    }

    public static class FMTLog extends ParsedLogEntry {
        private List<Finding> findings;

        public FMTLog(String ip, String timestamp, String server, String uri, String request, List<Finding> findings) {
            super(ip, timestamp, server, uri, request);
            this.findings = findings;
        }

        @AllArgsConstructor
        public static class Finding {
            private String zone, id, type, varName;
        }
    }

    public static class ExtendedLog extends ParsedLogEntry {
        private String varName, content;

        public ExtendedLog(String ip, String timestamp, String server, String uri, String request, String varName, String content) {
            super(ip, timestamp, server, uri, request);
            this.varName = varName;
            this.content = content;
        }
    }
}
