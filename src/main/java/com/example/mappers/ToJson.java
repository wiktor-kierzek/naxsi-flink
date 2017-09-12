package com.example.mappers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;

public class ToJson implements MapFunction<ParseLogLine.FMTLog, String> {

    private static Gson parser;

    static {
        parser = new Gson();
    }

    @Override
    public String map(ParseLogLine.FMTLog entry) throws Exception {
        JsonObject json = new JsonObject();

        json.addProperty("timestamp", entry.getTimestamp());
        json.addProperty("ip", entry.getIp());
        json.addProperty("uri", entry.getUri());
        json.addProperty("server", entry.getServer());
        json.addProperty("request", entry.getRequest());

        JsonArray jsonFindings = new JsonArray();
        for(ParseLogLine.FMTLog.Finding finding: entry.getFindings()) {
            JsonObject jsonFinding = new JsonObject();
            jsonFinding.addProperty("type", finding.getType().toString());
            jsonFinding.addProperty("id", finding.getId());
            jsonFinding.addProperty("zone", finding.getZone());
            jsonFinding.addProperty("var_name", finding.getVarName());
            jsonFinding.addProperty("content", finding.getContent());

            jsonFindings.add(jsonFinding);
        }

        json.add("findings", jsonFindings);

        return parser.toJson(json);
    }


}
