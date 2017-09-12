package com.example.joins;

import com.example.mappers.ParseLogLine;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.util.Collector;

public class SetContentInFindings implements CoGroupFunction<ParseLogLine.ParsedLogEntry, ParseLogLine.ParsedLogEntry, ParseLogLine.FMTLog> {
    @Override
    public void coGroup(Iterable<ParseLogLine.ParsedLogEntry> fmtIterable, Iterable<ParseLogLine.ParsedLogEntry> exlogIterable, Collector<ParseLogLine.FMTLog> collector) throws Exception {
        for (ParseLogLine.ParsedLogEntry fmt : fmtIterable) {
            if(fmt == null) {
                continue;
            }
            if (fmt.getRequest().startsWith("GET")) {
                setFindingsContentsInGET(fmt);
            } else if(fmt.getRequest().startsWith("POST")) {
                setFindingsContentsInPOST(exlogIterable, (ParseLogLine.FMTLog) fmt);
            }

            collector.collect((ParseLogLine.FMTLog) fmt);
        }
    }

    private void setFindingsContentsInPOST(Iterable<ParseLogLine.ParsedLogEntry> exlogIterable, ParseLogLine.FMTLog fmt) {
        for (ParseLogLine.FMTLog.Finding finding : fmt.getFindings()) {
            for (ParseLogLine.ParsedLogEntry parsedExlog : exlogIterable) {
                ParseLogLine.ExtendedLog exlog = ((ParseLogLine.ExtendedLog) parsedExlog);
                if(exlog == null) {
                    continue;
                }
                if (finding.getVarName().equals(exlog.getVarName())) {
                    finding.setContent(exlog.getContent());
                }
            }
        }
    }

    private void setFindingsContentsInGET(ParseLogLine.ParsedLogEntry fmt) {
        String[] variables = fmt.getRequest()
            .substring(fmt.getRequest().indexOf("/?") + 2, fmt.getRequest().lastIndexOf(" "))
            .split("&");

        for (String variable : variables) {
            String[] values = variable.split("=");
            for (ParseLogLine.FMTLog.Finding finding : ((ParseLogLine.FMTLog) fmt).getFindings()) {
                if (values[0].equals(finding.getVarName())) {
                    finding.setContent(values[1]);
                }
            }
        }
    }
}
