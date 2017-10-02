package com.example.sinks;

import com.example.Settings;
import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.alert.CreateAlertRequest;
import com.ifountain.opsgenie.client.model.alert.CreateAlertResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

@Slf4j
public class OpsGenieSink implements SinkFunction<OpsGenieTuple> {

    private static OpsGenieClient client;

    static {
        client = new OpsGenieClient();
    }

    @Override
    public void invoke(OpsGenieTuple tuple) throws Exception {

        String message = String.format("%s attack by ip: %s", tuple.type, tuple.ip);

        System.out.println(message);

        CreateAlertRequest request = new CreateAlertRequest();
        request.setApiKey(Settings.get(Settings.Key.OPSGENIE_API_KEY));
        request.setMessage(message);
        request.setSource("flink");
        request.setTeams(Collections.singletonList(Settings.get(Settings.Key.OPSGENIE_API_TEAM)));

        try {
            CreateAlertResponse response = client.alert().createAlert(request);
            response.getAlertId();
        } catch (OpsGenieClientException | IOException | ParseException e) {
           log.error("Could not create OpsGenie alert", e);
        }
    }
}
