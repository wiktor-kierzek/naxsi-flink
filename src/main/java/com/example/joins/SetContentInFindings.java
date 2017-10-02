package com.example.joins;

import com.example.data.tuple.ExtendedLog;
import com.example.data.tuple.FMTLog;
import com.example.data.tuple.ParsedLogEntry;
import com.example.mappers.ParseLogLine;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.util.Collector;

public class SetContentInFindings implements CoGroupFunction<ParsedLogEntry, ParsedLogEntry, FMTLog> {
    @Override
    public void coGroup(Iterable<ParsedLogEntry> fmtIterable, Iterable<ParsedLogEntry> exlogIterable, Collector<FMTLog> collector) throws Exception {
        for (ParsedLogEntry fmt : fmtIterable) {
            if(fmt == null) {
                continue;
            }
            if (fmt.getRequest().startsWith("GET")) {
                setFindingsContentsInGET(fmt);
            } else if(fmt.getRequest().startsWith("POST")) {
                setFindingsContentsInPOST(exlogIterable, (FMTLog) fmt);
            }

            collector.collect((FMTLog) fmt);
        }
    }

    private void setFindingsContentsInPOST(Iterable<ParsedLogEntry> exlogIterable, FMTLog fmt) {
        for (FMTLog.Finding finding : fmt.getFindings()) {
            for (ParsedLogEntry parsedExlog : exlogIterable) {
                ExtendedLog exlog = ((ExtendedLog) parsedExlog);
                if(exlog == null) {
                    continue;
                }
                if (finding.getVarName().equals(exlog.getVarName())) {
                    finding.setContent(exlog.getContent());
                }
            }
        }
    }

    private void setFindingsContentsInGET(ParsedLogEntry fmt) {
        String[] variables = fmt.getRequest()
            .substring(fmt.getRequest().indexOf("/?") + 2, fmt.getRequest().lastIndexOf(" "))
            .split("&");

        for (String variable : variables) {
            String[] values = variable.split("=");
            ((FMTLog) fmt).getFindings().stream()
                    .filter(finding -> values[0].equals(finding.getVarName()))
                    .forEach(finding -> finding.setContent(values[1]));
        }
    }
}
