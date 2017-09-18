package com.example.mappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.flink.api.common.functions.MapFunction;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Created by _sn on 10.09.2017.
 */
public class ExtractNaxsiMessage implements MapFunction<String,ExtractNaxsiMessage.NaxsiTuple> {

    public NaxsiTuple map(String s) throws Exception {
        int beginIndex = s.indexOf("message\":\"")+10;
        int endIndex = s.lastIndexOf("\",\"type\":");

        String message = s.substring(beginIndex, endIndex < 0 ? s.length() : endIndex);
        //type nie powinien byc enumem?
        String type = message.contains("NAXSI_FMT")?"fmt":message.contains("NAXSI_EXLOG")?"exlog":"other"; // te magiczne stringi bym wyciagnal do stalych

        return new NaxsiTuple(type, message); //zamiast konstruktora zrobilbym buildera(lombok ma anotacje @Builder). Tutaj mamy dwa parametry stringowe, co jak podbije ktos wersje lomboka i nagle generowany konstruktor bedzie lykał parametry w innej kolejności?
    }

    //wyciagnalbym do osobnej klasy, tj. pliku
    @AllArgsConstructor @Getter
    public static class NaxsiTuple implements Serializable {
        private String log; //tutaj bym zrobil enuma-w koncu jest skonczony zbior wartosci type
        private String message;
    }
}

