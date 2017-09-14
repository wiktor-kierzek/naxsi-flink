package com.example.sinks;

import com.example.Settings;
import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.alert.CreateAlertRequest;
import com.ifountain.opsgenie.client.model.alert.CreateAlertResponse;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

/**
 * Created by wiktor.kierzek@curency-one.com on 13.09.2017.
 */
public class OpsGenieSink implements SinkFunction<OpsGenieTuple> {

    private static OpsGenieClient client;

    static {
        client = new OpsGenieClient();
    }

    @Override
    public void invoke(OpsGenieTuple tuple) throws Exception {

        String message = String.format("%s attack by ip: %s", tuple.type, tuple.ip);

        CreateAlertRequest request = new CreateAlertRequest();
        request.setApiKey(Settings.get("opsgenie.api.key"));
        request.setMessage(message);
        request.setSource("flink");
        request.setTeams(Collections.singletonList(Settings.get("opsgenie.api.team")));

        try {
            CreateAlertResponse response = client.alert().createAlert(request);
            response.getAlertId();
        } catch (OpsGenieClientException | IOException | ParseException e) {
            System.err.println("Could not create OpsGenie alert");
            e.printStackTrace();
        }
    }
}
