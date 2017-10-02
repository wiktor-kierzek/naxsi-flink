package com.example.data.tuple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by wiktor.kierzek on 02.10.2017.
 */
@Getter
public class FMTLog extends ParsedLogEntry {
    private List<Finding> findings;

    public FMTLog(String ip, String timestamp, String server, String uri, String request, List<Finding> findings) {
        super(ip, server, uri, request, timestamp);
        this.findings = findings;
    }

    @AllArgsConstructor
    @Getter
    public static class Finding {

        public enum TYPE {
            Error, SQLi, RFI, Traversal, XSS, Evading, Uploads;

            public static TYPE getType(int id) {
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
        @Setter
        private String content;
    }
}