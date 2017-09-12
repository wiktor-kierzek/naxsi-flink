package com.example.mappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.functions.MapFunction;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by _sn on 10.09.2017.
 */
public class ParseLogLine implements MapFunction<ExtractNaxsiMessage.NaxsiTuple,ParseLogLine.ParsedLogEntry> {

    private static Pattern IP_REGEX = Pattern.compile("ip=([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})&");
    private static Pattern SERVER_REGEX = Pattern.compile("server=([^&]*)(?=&)");
    private static Pattern URI_REGEX = Pattern.compile("uri=([^&]*)(?=&)");
    private static Pattern REQUEST_REGEX = Pattern.compile("request:\\s\\\\?\"([^\",]+)");

    private static Pattern CONTENT_EXLOG_REGEX = Pattern.compile("content=(.*),\\s?client");
    private static Pattern VARNAME_EXLOG_REGEX = Pattern.compile("var_name=([^&]*)(?=&)");

    private static Pattern FMT_LINE_REGEX = Pattern.compile("NAXSI_FMT:\\s(.+)(?=,\\sclient)");

    public static SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    public ParsedLogEntry map(ExtractNaxsiMessage.NaxsiTuple log) throws Exception {
        switch (log.getLog()) {
            case "fmt":
                try {
                    return parseFMT(log.getMessage());
                } catch (Throwable e) {
                    System.out.println("Could not parse FMT: "+log.getMessage());
                }
            case "exlog":
                try {
                    return parseEX(log.getMessage());
                } catch (Throwable e) {
                    System.out.println("Could not parse EXLOG: "+log.getMessage());
                }
        }
        return null;
    }

    private FMTLog parseFMT(String line) throws ParseException {
        List<FMTLog.Finding> findings = new LinkedList<>();

        String[] events = getValue(FMT_LINE_REGEX, line).split("&zone");
        for(int i=1;i<events.length;i++) {
            String[] eventValues = events[i].split("&");

            findings.add(
                new FMTLog.Finding(
                    FMTLog.Finding.TYPE.getType(Integer.parseInt(eventValues[1].split("=")[1])),
                    eventValues[0].split("=")[1],
                    eventValues[1].split("=")[1],
                    eventValues[2].split("=")[1],
                    null)
            );
        }

        return new FMTLog(
            getValue(IP_REGEX, line),
            getTimestamp(line),
            getValue(SERVER_REGEX, line),
            getValue(URI_REGEX, line),
            getValue(REQUEST_REGEX, line),
            findings
        );
    }

    private ExtendedLog parseEX(String line) throws ParseException {
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

    private String getTimestamp(String line) throws ParseException {
        return line.substring(0, 19);
    }

    private String getValue(Pattern pattern, String line) {
        Matcher matcher = pattern.matcher(line);
        matcher.find();
        return matcher.group(1);
    }

    @AllArgsConstructor @Getter
    public abstract static class ParsedLogEntry implements Serializable {
        protected String ip, server, uri, request, timestamp;
    }

    @Getter
    public static class FMTLog extends ParsedLogEntry {
        private List<Finding> findings;

        FMTLog(String ip, String timestamp, String server, String uri, String request, List<Finding> findings) {
            super(ip, server, uri, request, timestamp);
            this.findings = findings;
        }

        @AllArgsConstructor @Getter
        public static class Finding {

            public enum TYPE {
                Error, SQLi, RFI, Traversal, XSS, Evading, Uploads;

                static TYPE getType(int id) {
                    if(id<1000) {
                        return Error;
                    }

                    id = (id-1000) / 100;

                    switch (id) {
                        case 0: return SQLi;
                        case 1: return RFI;
                        case 2: return Traversal;
                        case 3: return XSS;
                        case 4: return Evading;
                        case 5: return Uploads;
                    }
                    return Error;
                }
            }

            private TYPE type;
            private String zone, id, varName;
            @Setter private String content;
        }
    }

    @Getter
    public static class ExtendedLog extends ParsedLogEntry {
        private String varName, content;

        ExtendedLog(String ip, String timestamp, String server, String uri, String request, String varName, String content) {
            super(ip, server, uri, request, timestamp);
            this.varName = varName;
            this.content = content;
        }
    }
}
